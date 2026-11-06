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
- Long-press actions: pin/unpin, app info, uninstall (user apps only), hide
- Hidden app management in settings
- Light, dark or system theme, six accent colours, adjustable icon size
- Works as the system default launcher

Not included (deliberately): widgets, icon packs, notification badges, weather,
AI integrations, backup, work profiles, folders, analytics.

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

## Project layout

```
app/src/main/java/dev/tom/cascade/
  data/       apps repository, preferences
  model/      AppEntry
  ui/         Compose screens and activity
  util/       pure sorting, sectioning and search logic
```

The sorting/search logic is pure Kotlin and unit-tested in
`app/src/test/java/dev/tom/cascade/util/AppListTest.kt`; the UI is plain
Compose with no framework beyond Activity Compose and Material 3.

## Licence

MIT. See [LICENSE](LICENSE).
