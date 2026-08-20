# Implementation Plan - Make Status Bar Icons White

The user wants the Android status bar icons (clock, battery, etc.) to be white. Currently, the app uses `enableEdgeToEdge()` in `MainActivity`, which defaults to "auto" mode. In light mode, this results in dark icons. Since the app uses a red toolbar that extends behind the status bar, white icons are preferred for better contrast.

## Proposed Changes

### [MainActivity](file:///C:/git/easy-pdf-reader/app/src/main/java/com/example/easy_pdf_reader/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///C:/git/easy-pdf-reader/app/src/main/java/com/example/easy_pdf_reader/MainActivity.kt)
- Update `enableEdgeToEdge()` to explicitly use `SystemBarStyle.dark(Color.TRANSPARENT)` for the status bar. This forces the system to treat the status bar as "dark", thus rendering "light" (white) icons.
- Add necessary imports for `androidx.activity.SystemBarStyle` and `android.graphics.Color`.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure the code builds correctly with the new imports and API usage.

### Manual Verification
- Deploy the app to a device or emulator.
- Observe the status bar icons at the top of the screen. They should be white/light-colored, providing good contrast against the red toolbar.
- Test in both Light and Dark system modes to ensure the icons remain white as requested.
