# Spray Days — project notes for Claude

Location-agnostic agricultural spraying-window forecast. A single self-contained
web page fetches a 7-day forecast for the user's chosen location, scores each
daytime hour (07:00–18:00) for spray suitability, and renders an interactive
report. Runs as a PWA (GitHub Pages), an Android WebView app, and a plain local
HTML file.

**Live app:** <https://dfur.github.io/spray-days/> (GitHub Pages, served
from the repo root of `master`; pushes redeploy in ~1 min).

## Stack / environment
- **One HTML file, no build step, no dependencies, no API key.** All fetch,
  scoring, and rendering is inline JS in `index.html`.
- Data source: [Open-Meteo](https://open-meteo.com) forecast + geocoding APIs
  (free, no key). These are the **only** network contacts — no analytics, CDNs,
  or fonts. Keep it that way; the page's CSP meta enforces it (update the CSP
  if an API host ever changes).
- macOS (Apple Silicon) for development; Android SDK for the APK.

## Files
- `index.html` — **the app and the single canonical scoring implementation**
  (`scoreHours()`). Everything else wraps or serves this file. Formerly
  `spray-windows-live.html`; the old bash/jq Mac pipeline (`Update Spray
  Forecast.command` + template + generated page) was retired in Jul 2026.
- `manifest.json`, `sw.js`, `icons/`, `.nojekyll` — PWA/Pages support. `sw.js`
  caches the app shell only (network-first); it must never cache Open-Meteo
  responses. Bump its `CACHE` name when changing cached assets.
- `android/` — Android WebView APK. `app/build.gradle.kts` copies `index.html`
  into assets at build time — never hand-edit the copied asset. Build from
  `android/` with JDK 17 + ANDROID_HOME set, then `./gradlew assembleDebug`;
  sideload with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- `Spray Forecast.command` — double-click convenience: opens local `index.html`.
- `PLAN-extended-hours-night-spraying.md` — agreed but unbuilt plan (Jun 2026)
  for season-aware hours, night spraying, 2,4-D mode. Read before any work on
  hours/inversion/profiles. Its file checklist was updated for the JS-only world.
- `PLAN-ios-pwa.md` — the iOS/PWA plan, implemented Jul 2026 (see status line).
- `README.md` — end-user instructions.

## Location handling
- **No hardcoded/default location.** `{name, lat, lon}` persists in
  localStorage (`sprayLocation_v1`); first run shows the picker and fetches
  nothing until the user chooses.
- Coordinates are rounded to 2 decimals (~1 km) before storing/sending; GPS
  fires only on explicit tap. Forecast cache is per-location
  (`sprayForecast_v2_<lat>,<lon>`), 6 h max age, stale allowed offline.
- API calls use `timezone=auto`, so all hours/sunrise/sunset are
  location-local and the scoring window works anywhere.
- Geocoder place names are third-party data: render via `textContent` only,
  never `innerHTML`.

## Scoring (general / herbicide profile)
Verdict per hour = GO / MARGINAL / NO-GO from: wind (ideal 3–15 km/h), Delta T
(ideal 2–8; = dry-bulb − wet-bulb), precipitation (rain within 2 h = NO-GO),
inversion (light wind + clear sky near sunrise/sunset), frost (≤2 °C), dew
(RH ≥93% & T<8), fog (visibility <1 km = NO-GO). Full table in `README.md`.
Thresholds live in `scoreHours()` in `index.html`; keep the legend (same file)
and README table in sync when retuning.

## Conventions
- Keep it dependency-free and offline-capable (localStorage forecast cache +
  service-worker shell cache).
- If adding a second crop profile (e.g. orchard/horticulture), parameterise the
  thresholds rather than forking the page.
- Test changes by serving the folder locally (any static server) and checking:
  first-run location onboarding (no forecast request before choosing), search +
  GPS, 84 hourly cells (12 h × 7 days), offline stale banner. Then rebuild the
  APK if the HTML changed.
- Publishing = `git push` (Pages redeploys). The public repo history starts at
  the Jul 2026 squashed snapshot; full pre-publish history is in the local-only
  branch `local-history`.
