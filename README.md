# Cascade

A small, open-source Android home-screen launcher: one vertical alphabetical
list of your apps, favourites pinned above it, and search the moment you need it.
No accounts, no ads, no paywalls, no network permission.

Cascade is an independent project inspired by the *workflow* of Niagara
Launcher. It contains no code, assets, strings, source files or resources from
any third-party launcher.

## Features

- Vertical A–Z app list with sticky letter headers and a `#` bucket
- Favourites: long-press any app and pin it; no limit, shown as full rows above the A–Z list
- Wave alphabet rail on the right: drag to jump, empty letters are dimmed
- Configurable bottom button: tap and swipe-up each run Search, the voice
  assistant, or any app you choose
- Instant search: tap the search field, pull down past the top of the list,
  or tap the bottom button
- Optional contact search and a “search the web” row in the search overlay
- Pop-up folders: long-press an app to create a folder or move it into one;
  the folder sits in the alphabetical list with a 2×2 preview and opens a
  4-column popup
- Long-press actions: pin/unpin, app info, uninstall (user apps only), hide
- Optional notification dots: a dot on apps and favourites with active
  notifications, and their titles, text and relative time in the long-press
  sheet — tap to open, X to dismiss; previews can be hidden
- Hidden app management in settings
- Icon packs: use an installed ADW icon pack, with the system icon as fallback
- Per-app icons: long-press any app and pick a different icon from the pack
- Large clock at the top of the list, following the system 12/24-hour setting
- Weather for a city you choose on the date line, and an optional daily KJV
  verse underneath
- App widgets hosted under the clock; add or remove them in settings
- Light, dark or system theme, six accent colours, adjustable icon size
- Works as the system default launcher

Not included (deliberately): notification history or replies,
AI integrations, backup, work profiles, analytics.

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

## Icon packs

Settings → **Icon pack** lists the ADW-format icon packs installed on your
device (the same standard most launchers use). Pick one and every app, favourite
and folder preview switches; pick **System icons** to switch back. Apps the pack
does not cover keep their normal icon, and a missing icon falls back to a letter
tile rather than a blank space. Packs are read only, locally, through Android's
package APIs: no new permissions and no network access.

If an app is missing from the pack, long-press it and choose **Choose icon** to
pick any icon the pack ships. The choice applies to the list, favourites, folder
previews and folder popups, and is remembered across restarts. **Reset to pack
icon** in the same dialog returns the app to the pack's own icon, or to its
normal icon when the pack has no entry. Choices are stored locally; switching
packs leaves them stored but they only apply when the pack has a drawable with
that name.

## Clock and widgets

The home screen starts with a clock: the time in the system's 12- or 24-hour
format, today's date underneath, updated every minute.

Settings → **Header** adds two optional things to the date line:

- **Show weather** — pick a city once, and the current temperature and a small
  weather glyph sit at the right of the date. Data comes from Open-Meteo; the
  reading is refreshed at most every 30 minutes, cached on the device, and
  hidden if the request fails. Celsius, or Fahrenheit where the system locale
  uses it. No location permission is requested.
- **Show a daily Bible verse** — King James Version text under the date, one
  verse per day, off by default. The verses are bundled in the app and need no
  network.

Settings → **Widgets** → **Add widget** opens the system widget picker; the
widget appears under the clock, above favourites. **Remove** takes it off
again and releases its binding. Widget ids are stored locally, and a widget
whose app is uninstalled disappears. Widgets are not resized or reordered in
this version.

## Search

Typing filters apps instantly. Settings → **Search** → **Include contacts**
adds matching contacts under the app results; tapping one opens the dialer.
The permission is requested only when you switch it on, contacts are held in
memory and never written to disk, and revoking the permission turns the
switch off again.

A "Search the web for …" row at the bottom hands the query to the system
search handler, or to DuckDuckGo in the browser when there is none. Cascade
does not use the network for search; its only request is the weather lookup
below, and only when you turn it on.

## Project layout

```
app/src/main/java/dev/tom/cascade/
  data/          apps repository, contacts repository, preferences
  iconpack/      icon pack discovery and mapping
  model/         app, folder and contact models
  notifications/ listener, in-memory store, notification model
  ui/            Compose screens and activity
  util/          pure sorting, sectioning and search logic
```

The sorting/search logic is pure Kotlin and unit-tested in
`app/src/test/java/dev/tom/cascade/util/AppListTest.kt`; the UI is plain
Compose with no framework beyond Activity Compose and Material 3.

## Licence

MIT. See [LICENSE](LICENSE).
