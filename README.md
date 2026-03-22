# Mihon Desktop Extensions

JVM-compiled extensions for [Mihon Desktop](https://github.com/AltairCardinal/mihon), built from [keiyoushi/extensions-source](https://github.com/keiyoushi/extensions-source).

## Adding to Mihon Desktop

In Mihon Desktop → More → Extension Repos → Add:

```
https://raw.githubusercontent.com/AltairCardinal/extensions-desktop/repo
```

## How it works

GitHub Actions clones `extensions-source` weekly, applies JVM patches (replacing Android APIs with JVM shims), compiles each extension to a self-contained JAR, and publishes to the `repo` branch.

## Build status

See [Actions](../../actions) for the latest build.

## Technical notes

- Extensions that use `android.graphics.*` (image stitching) compile but image operations fall back to `java.awt`
- Extensions depending on WebView / JavaScript engine are not supported
- Each JAR is self-contained (android-compat shims included)
