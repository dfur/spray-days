# Spray Windows

On-demand agricultural spraying-window forecast. Pick a location, get a 7-day
hourly report: every daytime hour (07:00–18:00) scored GO / MARGINAL / NO-GO
for spray suitability, with the reasons flagged.

**Live app: <https://dfur.github.io/spray-forecast/>**

Data comes from [Open-Meteo](https://open-meteo.com) (free, no API key, no
account). The app talks to nothing else — no analytics, no trackers. The only
data that leaves your device is the coordinates of the location you choose,
rounded to ~1 km, sent to Open-Meteo to fetch the forecast.

## Using it

Open the URL above in any browser. First run asks for a location — search for
a town, or tap **Use my location**. Your choice is remembered on the device;
tap the location name in the header any time to change it.

**Install as an app:**

- **iPhone / iPad:** open the URL in Safari → Share → **Add to Home Screen**.
- **Android:** either sideload the APK (built from `android/` with
  `./gradlew assembleDebug`) or use Chrome → menu → **Add to Home screen**.
- **Mac:** double-click `Spray Forecast.command`, or just bookmark the URL.

The app keeps working with no signal: the page shell is cached, and the last
forecast for your location is shown with a "stale" banner until you can refresh.

## Scoring (general / herbicide profile)

| Factor | GO (ideal) | MARGINAL | NO-GO |
|---|---|---|---|
| Wind speed | 3–15 km/h | 15–20, or <3 | >20 |
| Delta T | 2–8 | 8–10, or <2 | >10 |
| Precipitation | dry, none next 2 h | ≥60% chance | rain within 2 h |
| Inversion | none | dawn/dusk edge | wind <6 km/h + clear sky near sunrise/sunset |
| Frost | air >2 °C | — | ≤2 °C |
| Dew | RH <93% | RH ≥93% & T <8 °C | — |
| Fog | visibility ≥1 km | — | visibility <1 km |

Delta T = dry-bulb − wet-bulb temperature (the standard Australian metric).

## Files

| File | What it is |
|---|---|
| `index.html` | The whole app — fetching, scoring, rendering. Single source of truth. |
| `manifest.json`, `sw.js`, `icons/` | PWA bits: install metadata, offline shell cache, app icon. |
| `android/` | Android WebView wrapper around the same `index.html`. |

## Customising

Everything lives in `index.html`:

- **Thresholds** — the `scoreHours()` function (wind, Delta T, rain, inversion,
  frost, dew, fog rules). Update the legend section in the same file to match.
- **Hours** — the 07–18 window is in `scoreHours()` and the grid's hours array.

Publishing a change: push to GitHub and Pages redeploys in about a minute.
The Android app bundles the HTML, so it needs an APK rebuild
(`cd android && ./gradlew assembleDebug`).

⚠️ Forecast-based guidance only — always confirm conditions with an on-site
weather meter and follow the product label. Forecasts can't reliably detect
inversions.
