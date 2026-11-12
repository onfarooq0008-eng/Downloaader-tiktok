# Marq — TikTok downloader for Android

Native Kotlin + Jetpack Compose port of the Marq web app (same look, same flow).

## What it does
- Paste a TikTok link (or **Share → Marq** from the TikTok app; `vm.tiktok.com` links open in Marq too)
- Choose **with logo** / **without logo**, download video, save the sound, or save photo-post slides
- Files go to `Movies/Marq`, `Music/Marq`, `Pictures/Marq` (MediaStore — no storage permission needed)
- Shelf of the last 30 fetched posts, stored on-device

The web app's server pieces are ported to the phone: the tikwm lookup (`TikTokApi.kt`)
and the media proxy (now a direct download in `Downloader.kt`), so **no backend is needed**.

## Build
**Android Studio:** File → Open this folder → Run ▶ (needs JDK 17, Android SDK 35).
**Command line:** `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
**No Android Studio:** push this folder to a GitHub repo; the *Build APK* workflow produces the APK under Actions → Artifacts.

Requires Android 10+ (minSdk 29).

## Notes
- Lookup relies on the third-party tikwm.com API (same as the web app); if it changes or rate-limits, edit `TikTokApi.lookup`.
- Release builds are signed with the debug key for convenience — add your own signing config before publishing.
- Only download content you have the right to keep.
