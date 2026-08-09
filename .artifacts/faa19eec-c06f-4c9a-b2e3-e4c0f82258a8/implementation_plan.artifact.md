# Implementation Plan - PDF Reader Features

Implement core PDF reader features including opening documents, intent handling, page navigation, search, sharing, and lifecycle management.

## User Review Required

> [!IMPORTANT]
> The app will request `takePersistableUriPermission` for opened documents. This ensures the document remains accessible after device reboots or app restarts if the provider supports it.

## Proposed Changes

### Android Manifest
#### [MODIFY] [AndroidManifest.xml](file:///C:/Git/easy-pdf-reader/app/src/main/AndroidManifest.xml)
- Add intent filter for `ACTION_VIEW` with `application/pdf` MIME type.
- Set `launchMode="singleTop"` to handle `onNewIntent`.

### UI Components
#### [MODIFY] [MainActivity.kt](file:///C:/Git/easy-pdf-reader/app/src/main/java/com/example/easy_pdf_reader/MainActivity.kt)
- **Open Document:** Use `ActivityResultContracts.OpenDocument` for `application/pdf`.
- **Intent Handling:** Process `ACTION_VIEW` in `onCreate` and `onNewIntent`.
- **Loading State:** Add a progress indicator in `activity_main.xml` and show/hide it during loading.
- **Go to Page:** Replace the simple `Toast` error with an inline error in the dialog. Use `MaterialAlertDialogBuilder`.
- **Share:** Implement robust sharing using `ACTION_SEND`, `EXTRA_STREAM`, and `ClipData`.
- **Lifecycle:** Save `currentDocumentUri` in `onSaveInstanceState` and restore it correctly.

#### [MODIFY] [activity_main.xml](file:///C:/Git/easy-pdf-reader/app/src/main/res/layout/activity_main.xml)
- Add a `CircularProgressIndicator` for loading state.

### PDF Logic
#### [MODIFY] [MyPdfViewerFragment.kt](file:///C:/Git/easy-pdf-reader/app/src/main/java/com/example/easy_pdf_reader/MyPdfViewerFragment.kt)
- Expose search activation and handle any search-related configurations if needed.
- Ensure `PdfView` is ready before scrolling.

## Verification Plan

### Automated Tests
- **Unit Tests:** Test URI validation and intent handling logic.
- **UI Tests:** Test opening a document, "Go to page" dialog validation, and search activation.

### Manual Verification
- Deploy to a device.
- Open a PDF from the Files app ("Open with").
- Open a PDF via the "Open document" menu.
- Verify "Go to page" with valid and invalid inputs.
- Verify text search is case-insensitive.
- Verify sharing works with Google Drive or other apps.
- Verify rotation doesn't lose the open document.
