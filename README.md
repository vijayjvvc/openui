# OpenUI

[![](https://jitpack.io/v/rowanlabs/openui.svg)](https://jitpack.io/#rowanlabs/openui)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

Small, dependency-free Android UI components written in pure Java.

| Component | What it does |
|---|---|
| `OceanWaveView` | Rounded card with a live animated ocean wave, outer border, icon circle and a TextView-like label |
| `MoreAppsView` | "More apps" list fetched from your API, cached in SharedPreferences, shown instantly on the next launch |

- Min SDK 24
- No third-party libraries (no Glide, no Retrofit)
- Everything is configurable from XML

## Installation

**1.** Add JitPack to `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**2.** Add the dependency to your app's `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.rowanlabs.openui:sdk:1.0.0'
}
```

The `INTERNET` permission needed by `MoreAppsView` is merged into your manifest automatically.

## OceanWaveView

```xml
<com.rowan.android.uiclasses.OceanWaveView
    android:layout_width="160dp"
    android:layout_height="240dp"
    app:waveColor="#3DB67F"
    app:waveLevel="0.35"
    app:cornerRadius="60dp"
    app:outerBorderColor="#3DB67F"
    app:outerBorderWidth="2dp"
    app:icon="@drawable/ic_my_app"
    app:iconTint="#FFFFFF"
    app:iconBorderColor="#FFFFFF"
    app:iconBorderWidth="2dp"
    app:text="Clean speaker"
    app:textColor="#222222"
    app:textSize="16sp"
    app:textMaxLines="2"
    app:textEllipsize="end" />
```

| Attribute | Description |
|---|---|
| `waveColor` | Wave color |
| `waveLevel` | Fill level, 0 to 1 (default 0.35) |
| `waveAmplitude` | Wave height |
| `waveSpeed` | Animation speed (1 = normal) |
| `cornerRadius` | Card corner radius |
| `containerColor` | Color above the water (default transparent) |
| `outerBorderColor`, `outerBorderWidth` | Outer border (default width 0 = none) |
| `icon`, `iconTint`, `iconSize` | Icon inside the circle (default: arrow) |
| `iconCircleSize`, `iconBorderColor`, `iconBorderWidth` | Icon circle |
| `text`, `textColor`, `textSize` | Label |
| `textFontFamily` | `@font/xxx` or a family name such as `sans-serif-medium` |
| `textFontStyle` | `normal`, `bold`, `italic`, `boldItalic` |
| `textMaxLines` | Max lines (default 1, 0 or less = unlimited) |
| `textEllipsize` | `none`, `start`, `middle`, `end` |
| `textAlign` | `left`, `center`, `right` |
| `textLineSpacingExtra`, `textLineSpacingMultiplier` | Line spacing |
| `textLetterSpacing` | Letter spacing in em |
| `textUppercase` | Force uppercase |
| `textPaddingTop`, `textPaddingHorizontal` | Label padding (defaults 5dp / 12dp) |
| `textPosition` | `top` or `aboveWave` |

From code:

```java
view.setWaveLevel(0.6f, true);      // animated
view.setWaveColor(0xFF2196F3);
view.setText("Next");
view.setAnimating(false);           // pause
```

## MoreAppsView

```xml
<com.rowan.android.uiclasses.MoreAppsView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:rowan_apiUrl="https://example.com/more_apps.json"
    app:rowan_listCount="4" />
```

How it loads:

1. Shows the last saved list immediately (SharedPreferences).
2. No saved list yet? Shows a pulsing skeleton.
3. Calls the API in the background and updates the list only if the data changed.
4. If the call fails, the saved list stays on screen.

Expected JSON: a bare array, or an object with the array under `apps`, `data`, `items` or `results`.

```json
[
  {
    "name": "Super Cleaner",
    "description": "Free up space in one tap",
    "icon": "https://example.com/icons/cleaner.png",
    "package": "com.example.supercleaner",
    "url": "https://play.google.com/store/apps/details?id=com.example.supercleaner"
  }
]
```

`description` and `url` are optional. If `url` is missing, the Play Store is opened with `package`.
Only `http`, `https` and `market` links are opened.

All `MoreAppsView` attributes use the `rowan_` prefix so they never clash with attributes from the
Android framework, AndroidX, Material or your own app.

| Attribute | Description |
|---|---|
| `rowan_apiUrl` | JSON endpoint (use https) |
| `rowan_listCount` | Max rows to show, also the skeleton row count (0 = all) |
| `rowan_hideWhenEmpty` | Hide the view when there is nothing to show (default true) |
| `rowan_excludeOwnApp` | Hide the running app from the list (default true) |
| `rowan_itemBackgroundColor` | Row background color |
| `rowan_itemBorderColor`, `rowan_itemBorderWidth` | Row border |
| `rowan_itemCornerRadius` | Row corner radius |
| `rowan_itemSpacing`, `rowan_itemPadding` | Row spacing and inner padding |
| `rowan_logoSize`, `rowan_logoCornerRadius` | App logo size and corners |
| `rowan_skeletonColor` | Placeholder / loading color |
| `rowan_appTitleColor`, `rowan_appTitleSize` | Title |
| `rowan_appDescriptionColor`, `rowan_appDescriptionSize` | Description |
| `rowan_appDescriptionMaxLines` | Max description lines (default 2) |

From code:

```java
view.setApiUrl("https://example.com/more_apps.json");
view.setListCount(3);
view.setHeader("Authorization", "Bearer TOKEN");
view.setOnAppClickListener(item -> false);   // return true to handle the click yourself
        view.refresh();
```

## Debugging

```java
RowanUi.setDebug(true);   // Logcat tag: RowanUi
```

## Contributing

Issues and pull requests are welcome.

1. Fork the repo and create a branch.
2. Run the `app` module (the sample) to try your change.
3. Open a pull request and describe what changed.

## License

```
Copyright 2026 rowanlabs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```