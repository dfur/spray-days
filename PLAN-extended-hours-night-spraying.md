# Plan: Season-aware hours, night spraying & 2,4-D mode

*Drafted 12 Jun 2026. Status: **not started** — design agreed in conversation, coding deferred.*

## Why

Two seasonal weaknesses found in the current implementation, plus two feature
requests:

1. **Hard-coded day window.** Scoring and rendering both assume 07:00–18:00.
   Right for Shepparton mid-winter, but in January daylight runs ~06:10–20:45,
   so ~4 h of sprayable daylight is silently discarded — often the *best*
   summer hours (early morning, before heat / high Delta T).
2. **Fixed-clock inversion logic.** The dawn/dusk check is `hr <= 9 || hr >= 16`.
   Correct in June; in January it wrongly flags mid-afternoon as "dusk edge"
   and misses the real inversion-risk period around 20:00–21:00.
3. **Night spraying switch (requested).** In summer, daytime is sometimes too
   hot / Delta T too high from very early to very late; night is the only
   workable option. Wanted: a toggle that scores and shows all 24 hours.
4. **2,4-D switch (requested).** A mode reflecting APVMA 2,4-D restrictions,
   which are stricter than the general profile.

## Phase 1 — sun-relative day window (fixes #1 and #2)

- Replace the hard-coded `7..18` hour filter with a window derived from the
  API's per-day `sunrise`/`sunset` (already fetched and displayed):
  e.g. **sunrise → sunset** rounded outward to whole hours, optionally with a
  configurable buffer (`DAY_BUFFER_H`, default 0).
- Inversion / dawn-dusk logic becomes sun-relative: "within N hours of
  sunrise/sunset" (suggest N = 2) instead of fixed clock hours.
- Grid rows become dynamic: union of all 7 days' windows, so winter shows
  ~12 rows and summer ~15–16. Hours outside a given day's window render as
  blank cells (as missing hours do today).
- Update the CLAUDE.md test note: "84 cells" is no longer fixed — it becomes
  (rows × 7) with blanks allowed.

## Phase 2 — night spraying toggle (#3)

- UI: a toggle chip in the header (`🌙 Night hours: off/on`), persisted in
  `localStorage`. Off = Phase 1 behaviour; on = score and show all 24 hours.
- Night-specific scoring (applies sunset→sunrise):
  - **Inversion is the dominant factor**: light wind (< ~6–8 km/h) + low cloud
    (< ~40%) at night → `INVERSION` → NO-GO. This will correctly mark most
    clear calm nights red; the surviving GO hours (breezy or overcast nights)
    are the legitimate windows.
  - Frost / dew / fog rules unchanged (they'll just fire more often at night —
    correct).
  - Delta T at night is structurally low; keep the DT_LOW → MARGINAL rule.
- Rendering: dim night cells (e.g. reduced saturation / darker background
  tint) so day vs night reads at a glance; add sunrise/sunset separator lines
  in each day column if cheap to do.
- `bestWindows()` currently can't join a run across midnight
  (`cur.d === r.d` check) — allow a 23:00 → 00:00 continuation into the next
  day's date.
- Disclaimer addition: night spraying is label-dependent; many labels restrict
  or prohibit it. Forecasts cannot detect inversions — physical on-site checks
  matter even more at night.
- Bump `CACHE_KEY` (`sprayForecast_v1` → `v2`) — cached data shape changes
  (24 h vs 12 h).

## Phase 3 — 2,4-D mode toggle (#4)

A second toggle (`⚗️ 2,4-D mode`), implemented by **parameterising thresholds,
not forking the scoring** (per CLAUDE.md convention). Differences from the
general profile, based on the APVMA 2,4-D label restrictions — **verify
against the current label text before coding, these change**:

- Wind must be **3–20 km/h measured at the site** (label allows up to 20, but
  keep our 15–20 band MARGINAL rather than GO — forecast ≠ on-site reading).
  Wind < 3 km/h becomes **NO-GO** (not MARGINAL) — label prohibition.
- **No application between sunset and sunrise** → night hours are always
  NO-GO in this mode, even with the night toggle on (the toggles interact:
  night cells render but are forced red with a "2,4-D: no night application"
  flag).
- Inversion → NO-GO (already the case in the general profile).
- Non-weather label requirements we can only surface as a note, not score:
  very coarse (VC) or larger droplets, downwind buffer zones, record-keeping.
  Add a one-line reminder under the legend when the mode is on.

## Implementation order & file checklist

**Updated Jul 2026 (PWA convergence):** the bash/jq Mac path was retired — the
JS `scoreHours()` in `index.html` (formerly `spray-windows-live.html`) is now
the single canonical scoring implementation, and both toggles are runtime
features on the one page. The old options (a)/(b)/(c) below are moot: what was
"(c) move scoring to shared JS" effectively happened as part of the iOS/PWA
work, with none of the duplication downsides.

Touched files when this goes ahead:

- `index.html` — `scoreHours()`, render loop (dynamic rows, night dimming),
  header toggles, legend, disclaimer, `CACHE_KEY` bump.
- `README.md` — scoring table, new toggles.
- `CLAUDE.md` — test note (cell count no longer fixed at 84), profile docs.
- Android: no Kotlin changes expected; rebuild APK to pick up the HTML.
- PWA: push to GitHub → Pages redeploys automatically.
