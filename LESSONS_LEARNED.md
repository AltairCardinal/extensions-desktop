# Mihon Desktop 扩展加载失败教训总结

本文档记录了将 Android 扩展适配到 Mihon Desktop（JVM）过程中遇到的所有实际错误，分析其根因，并给出正确的修复方式。每个问题都来自真实的 manhuagui 扩展全链路测试。

---

## 1. `ServiceConfigurationError` 导致扩展加载失败

### 现象
```
安装扩展失败
java.util.ServiceConfigurationError: eu.kanade.tachiyomi.source.Source:
  Provider eu.kanade.tachiyomi.extension.zh.manhuagui.Manhuagui
  could not be instantiated
```

### 根因
`ServiceConfigurationError` 继承自 `Error`，不是 `Exception`。原代码：
```kotlin
try { ... } catch (e: Exception) { ... }
```
`Error` 不是 `Exception` 的子类，所以异常穿透了 catch 块，导致整个加载流程崩溃。

### 修复
```kotlin
// 错误
catch (e: Exception) { ... }

// 正确
catch (e: Throwable) { ... }
```

### 教训
在任何需要"吞掉所有失败"的 ClassLoader / ServiceLoader 调用处，必须捕获 `Throwable`，不能只捕获 `Exception`。

---

## 2. ClassLoader 约束冲突：`rx.Observable loader constraint violation`

### 现象
```
loader constraint violation: when resolving method
  rx.Observable ... loaded from instance of URLClassLoader
  used as type rx.Observable ... loaded from instance of AppClassLoader
```

### 根因
JVM 类加载器约束规则：**同一个类在方法调用的两端（调用者和被调用者）必须由同一个 ClassLoader 加载**。

`rx.Observable` 被 extension fat JAR 和父类加载器双方各自加载了一份，JVM 认为这是两个不同的类型，从而抛出约束冲突。

### 根本设计
Mihon Desktop 使用 child-first `ExtensionClassLoader`，扩展可以使用自带的 `desktop-api` 中的 `HttpSource`/`Injekt`。但所有跨越 app↔extension 边界的共享类型必须强制从父类加载器加载（`mustLoadFromParent`）。

### 修复
在 `ExtensionClassLoader.mustLoadFromParent()` 中添加 `rx.*`：
```kotlin
name.startsWith("rx.") ||        // RxJava 1.x
name.startsWith("io.reactivex.") // RxJava 2.x
```

### 教训
凡是在 extension API 方法签名中出现的类型（参数、返回值），都必须加入 `mustLoadFromParent`，否则 JVM 会在类型检查时报约束冲突。检查方式：看 `source-api` 的所有接口方法签名。

---

## 3. `lateinit property url has not been initialized`

### 现象
```
kotlin.UninitializedPropertyAccessException:
  lateinit property url has not been initialized
  at ... getMangaDetails()
```

### 根因
扩展的约定：`getMangaDetails()` 返回一个新的 `SManga` 对象，**只填充 thumbnail、author、description 等详情字段，不填充 url 和 title**（这两个字段在调用前就已知）。

错误代码直接用返回值替换原 manga：
```kotlin
manga = source.getMangaDetails(manga) // 丢失了 url/title
```

### 修复
```kotlin
internal fun applyMangaDetails(original: SManga, details: SManga): SManga = details.also { d ->
    // 如果 getMangaDetails 没有设置 url/title，保留原始值
    if (d.url.isBlank()) d.url = original.url
    if (d.title.isBlank()) d.title = original.title
}

manga = applyMangaDetails(manga, source.getMangaDetails(manga))
```

### 教训
永远不要直接替换 manga/chapter 对象。始终用 `applyMangaDetails` 模式，将详情字段合并回原始对象，保留 url/title 等标识字段。

---

## 4. `Serializer for class 'Comic' is not found`（序列化插件未启用）

### 现象
```
kotlinx.serialization.SerializationException:
  Serializer for class 'Comic' is not found.
  Mark the class as @Serializable or provide the serializer explicitly.
```
注意：`Comic` 类已经标注了 `@Serializable`，但运行时找不到。

### 根因（两处同时缺失）

#### 4a. `common.gradle` 未应用序列化编译器插件
`kotlinx.serialization` 需要 **Kotlin 编译器插件**在编译期生成 `$serializer` 字节码。仅有运行时依赖是不够的。

```groovy
// 错误：只有运行时，没有编译器插件
apply plugin: 'org.jetbrains.kotlin.jvm'
// 缺少 ↓
apply plugin: 'org.jetbrains.kotlin.plugin.serialization'
```

#### 4b. 根 `build.gradle.kts` buildscript 未声明插件 classpath
`apply plugin: 'org.jetbrains.kotlin.plugin.serialization'` 依赖于 buildscript classpath 中声明插件。

```kotlin
// 错误：缺少 serialization classpath
buildscript {
    dependencies {
        classpath(libs.gradle.kotlin)
        // 缺少 ↓
        classpath(libs.gradle.serialization)
    }
}
```

### 修复
两处同时修复：
- `patches/common-jvm.gradle` 添加 `apply plugin: 'org.jetbrains.kotlin.plugin.serialization'`
- `scripts/patch.sh` 生成的 `build.gradle.kts` 添加 `classpath(libs.gradle.serialization)`

### 陷阱：`patches/common-jvm.gradle` vs 实际生效的 `common.gradle`
`patch.sh` 第 2 步执行：
```bash
cp "$REPO_ROOT/patches/common-jvm.gradle" "$EXT_SRC/common.gradle"
```
所以应该编辑 `patches/common-jvm.gradle`（模板），而不是直接编辑 `extensions-source/common.gradle`（构建时被覆盖）。同理，`desktop-api/Page.kt` 有两个副本，`patch.sh` 从外层拷贝进来——必须修改外层文件。

### 教训
1. kotlinx.serialization 需要**两件事**同时满足：运行时 JAR + 编译器插件。缺一不可。
2. 了解 `patch.sh` 的覆盖顺序，所有修改必须在模板/源头文件中进行，不能直接改被覆盖的目标文件。

---

## 5. `Page.<init> android.net.Uri NoSuchMethodException`

### 现象
```
java.lang.NoSuchMethodException:
  eu.kanade.tachiyomi.source.model.Page.<init>(
    int, String, String, android.net.Uri, int,
    DefaultConstructorMarker)
```

### 根因
`Page` 类在两处定义，签名不一致：

| 位置 | `uri` 类型 |
|------|-----------|
| `source-api/Page.kt`（父 ClassLoader，运行时） | `Any?` |
| `desktop-api/Page.kt`（子 ClassLoader，编译期） | `android.net.Uri?` |

`@Serializable` 的 `Page$serializer` 在编译期使用 `desktop-api` 的签名生成字节码，调用的构造函数是 `Page(int, String, String?, android.net.Uri?, DefaultConstructorMarker)`。但运行时 `Page` 类来自父 ClassLoader（`source-api`），其构造函数是 `Page(int, String, String?, Any?, DefaultConstructorMarker)`。签名不匹配，`NoSuchMethodException`。

### 修复
将 `desktop-api/Page.kt` 中的 `uri: android.net.Uri?` 改为 `uri: Any?`，与 `source-api/Page.kt` 对齐：
```kotlin
// 修复前
@Transient var uri: android.net.Uri? = null

// 修复后
@Transient var uri: Any? = null  // 与 source-api 对齐；android.net.Uri 赋值通过 Any 兼容
```

### 陷阱：Gradle 构建缓存掩盖变更
修改 `Page.kt` 后，构建仍然使用缓存结果（`FROM-CACHE`），生成的 JAR 不包含修复。必须强制重新编译：
```bash
./gradlew --no-build-cache --rerun-tasks :src:zh:manhuagui:jar
```

### 教训
1. `desktop-api` 中所有与 `source-api` 共享的数据类型，**必须保持完全相同的构造函数签名**。任何差异都会在 `@Serializable` 生成的代码中暴露，且只在运行时报错。
2. 修改 Kotlin 源文件后如果怀疑缓存未更新，用 `--no-build-cache --rerun-tasks` 强制重建。

---

## 6. 图片全黑（Coil 绕过 source 的 OkHttp 客户端）

### 现象
阅读器能正确显示页面数量，但每一页显示纯黑。网络请求未携带 `Referer` 头，CDN 拒绝返回图片内容（返回空响应或 403）。

### 根因
Coil 3 在 JVM 上默认使用 Java 标准库的 `HttpURLConnection` 加载网络图片，**完全绕过了 source 的 OkHttp 客户端**。而 manhuagui 等源在 `headersBuilder()` 中设置了 `Referer` 头，图片 CDN 依赖这个头进行防盗链验证。

### 修复
在加载页面图片之前，通过 **source 自己的 OkHttp 客户端**预下载所有图片到临时文件，然后让 Coil 加载本地 `file://` URI（本地文件不需要任何 HTTP 头）。

#### 访问 source 客户端的障碍
`HttpSource` 在子 ClassLoader 中，app 无法直接 cast 到它（会触发 ClassCastException）。解决方案：**通过反射访问**。`OkHttpClient` 和 `Headers` 类在 `mustLoadFromParent` 列表中，确保两侧使用同一个 ClassLoader，类型可以安全 cast。

```kotlin
class SourcePageFetcher(source: CatalogueSource, fallbackClient: OkHttpClient) {
    val client: OkHttpClient = runCatching {
        source.javaClass.getMethod("getClient").invoke(source) as OkHttpClient
    }.getOrDefault(fallbackClient)

    private val headers: Headers? = runCatching {
        source.javaClass.getMethod("getHeaders").invoke(source) as Headers
    }.getOrNull()

    suspend fun fetchToFile(page: Page, destDir: File): String? {
        val imageUrl = page.imageUrl?.takeIf { it.isNotBlank() } ?: return null
        // ... 下载到临时文件，返回 file:// URI
    }
}
```

### 教训
1. 在 JVM 上，Coil 不会自动使用 OkHttp。凡是需要自定义 HTTP 头（Referer、Cookie 等）的图片请求，必须绕过 Coil 的默认图片加载。
2. 当 ClassLoader 隔离阻止直接类型 cast 时，反射是访问跨边界对象方法的正确手段——前提是保证公共类型（OkHttpClient、Headers）在 `mustLoadFromParent` 中统一加载。

---

## 综合架构要点

### ClassLoader 隔离策略
```
┌─────────────────────────────────────┐
│ App ClassLoader (parent)             │
│  - source-api (Page, SManga, etc.)   │
│  - OkHttp, RxJava, Coroutines        │
│  - android-compat stubs              │
│  - Coil, Injekt (app version)        │
└────────────────┬────────────────────┘
                 │ delegates if mustLoadFromParent
┌────────────────▼────────────────────┐
│ ExtensionClassLoader (child-first)   │
│  - Extension classes                 │
│  - desktop-api (HttpSource, etc.)    │
│  - Extension-bundled libs            │
└─────────────────────────────────────┘
```

**`mustLoadFromParent` 必须包含**：所有跨边界出现在方法签名中的类型——包括 source model 类型、OkHttp、RxJava、Coroutines、android stubs。

### patch.sh 文件覆盖顺序（易出错）
```
extensions-desktop/
├── android-compat/          ← patch.sh 步骤 1 复制到 extensions-source/
├── desktop-api/             ← patch.sh 步骤 1b 复制到 extensions-source/
│   └── ...Page.kt           ← 必须在这里修改，不能改 extensions-source/ 副本
├── patches/
│   ├── common-jvm.gradle    ← patch.sh 步骤 2 复制为 extensions-source/common.gradle
│   └── ...                  ← 所有模板都在这里修改
└── scripts/
    └── patch.sh             ← 步骤 9 用 heredoc 生成 build.gradle.kts（需含 serialization）
```

### 编译器插件 vs 运行时依赖
kotlinx.serialization 需要**同时**：
1. **编译期**：`apply plugin: 'org.jetbrains.kotlin.plugin.serialization'`（生成 `$serializer` 字节码）
2. **buildscript classpath**：`classpath(libs.gradle.serialization)`（使插件可用）
3. **运行时**：`implementation(libs.kotlin.json)` 或 `implementation(libs.kotlin.protobuf)`

缺少任何一个，`@Serializable` 类在运行时都会抛出 `SerializationException`。
