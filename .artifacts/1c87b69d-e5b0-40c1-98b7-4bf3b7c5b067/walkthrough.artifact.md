# Walkthrough - White Status Bar Icons

I have updated the application to ensure the status bar icons (clock, battery, etc.) are white, providing better contrast against the red toolbar.

## Changes

### [MainActivity](file:///C:/git/easy-pdf-reader/app/src/main/java/com/example/easy_pdf_reader/MainActivity.kt)

Updated the `enableEdgeToEdge` configuration to explicitly request a dark status bar style. In Android's edge-to-edge API, `SystemBarStyle.dark` tells the system the background is dark, which triggers the use of light (white) icons.

```kotlin
enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
)
```

## Verification Results

### Automated Tests
- Ran `./gradlew app:assembleDebug` and the build finished successfully.

### Manual Verification
> [!IMPORTANT]
> Please deploy the app to your device to verify the change. The status bar icons should now be white regardless of whether the system is in Light or Dark mode.
