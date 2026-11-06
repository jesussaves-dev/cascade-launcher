# Cascade

A small, open-source Android home-screen launcher: one vertical alphabetical
list of your apps, favourites pinned on top, and search the moment you need it.
No accounts, no ads, no paywalls, no network permission.

Cascade is an independent project inspired by the *workflow* of Niagara
Launcher. It contains no code, assets, strings, source files or resources from
any third-party launcher.

## Features

- Vertical A–Z app list with sticky letter headers and a `#` bucket
- Favourites row: long-press any app and pin it, up to eight apps
  (two columns on tablets and foldables)
- Wave alphabet rail on the right: drag to jump, empty letters are dimmed
- Configurable bottom button: tap and swipe-up each run Search, the voice
  assistant, or any app you choose
- Instant search: tap the search field, pull down past the top of the list,
  or tap the bottom button
- Pop-up folders: long-press an app to create a folder or move it into one;
  the folder sits in the alphabetical list with a 2×2 preview and opens a
  4-column popup
- Long-press actions: pin/unpin, app info, uninstall (user apps only), hide
- Optional notification dots: a dot on apps and favourites with active
  notifications, and their titles, text and relative time in the long-press
  sheet — tap to open, X to dismiss; previews can be hidden
- Hidden app management in settings
- Light, dark or system theme, six accent colours, adjustable icon size
- Works as the system default launcher

Not included (deliberately): widgets, icon packs, notification history or
replies, weather, AI integrations, backup, work profiles, analytics.

## Build

Requirements: JDK 17 and Android SDK with platform 36.

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk   # any JDK 17
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`. Install with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```bash
./gradlew testDebugUnitTest
```

## Set as default launcher

Open Cascade, tap the settings gear, then **Set Cascade as default**. On some
devices you can also use Settings → Apps → Default apps → Home app.

## Folders

Long-press any app and choose **Add to folder** — pick an existing folder or
name a new one. The app leaves the A–Z list, and the folder takes its place
alphabetically. Tap a folder for its 4-column popup; tap an app to launch it.
Long-press an app inside the popup to remove it, or long-press the folder row
to rename or delete it (deleting returns its apps to the list). Folders and
their contents are stored locally in app preferences; no database, no network.

## Optional: notification dots

Cascade reads notifications only through Android's notification listener.
Nothing leaves the device and no notification content is stored on disk.
Turn it on with **Settings → Notifications → Open**, enable Cascade, and come
back. The **Show notification previews** switch hides sender names and message
text, leaving dots and times only. Revoke access at any time; dots disappear.

## Project layout

```
app/src/main/java/dev/tom/cascade/
  data/          apps repository, preferences
  model/         app and folder models
  notifications/ listener, in-memory store, notification model
  ui/            Compose screens and activity
  util/          pure sorting, sectioning and search logic
```

The sorting/search logic is pure Kotlin and unit-tested in
`app/src/test/java/dev/tom/cascade/util/AppListTest.kt`; the UI is plain
Compose with no framework beyond Activity Compose and Material 3.

## Licence

MIT. See [LICENSE](LICENSE).
