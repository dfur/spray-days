# Plan: iPhone / iPad support (PWA first, native shell maybe later)

*Drafted 12 Jun 2026. Status: **Phase A done, 14 Jul 2026** (with revisions:
Pages served from repo root, `spray-windows-live.html` renamed `index.html`,
selectable location with no default, jq path retired — JS scoring now
canonical). Phase B (native shell) remains contingent on the PWA disappointing.*

## Decision summary

Goal: run the spray tool on iPhone + iPad, and share it with at least one other
person's device for testing.

- iOS has no APK-style sideloading (in Australia). Free Apple ID installs
  expire after 7 days and require a cable to this Mac; sharing remotely
  effectively requires the paid Apple Developer Program (US$99/yr) + TestFlight.
- The Android app is just a WebView around `spray-windows-live.html`, which is
  fully self-contained. So **Phase A: host that page and use iOS
  "Add to Home Screen" (PWA)** — costs nothing, works on phone + tablet, and
  sharing = sending a URL. **Phase B (optional, only if PWA disappoints):
  native WKWebView shell via the paid program + TestFlight.**

## Phase A — PWA on GitHub Pages

### A0. Pre-flight (state when drafted)

- Repo is **local-only**: no `origin` remote, and `gh` CLI is **not installed**.
- GitHub Pages on a free account requires a **public** repo. Options:
  (a) make this whole repo public (it contains nothing sensitive — check
  the original analysis `.md` before deciding), or (b) create a separate
  tiny public repo holding only the published page + icons. Ask David.
- Install `gh` via `brew install gh` (or do repo creation in the web UI).

### A1. Publish the page

- Create GitHub repo, push `master`.
- Serve the live page via Pages. Cleanest: a `docs/` folder on `master`
  with `index.html` = copy of `spray-windows-live.html` (same pattern as the
  Android assets copy — add a build/copy step or symlink-style convention so
  `spray-windows-live.html` stays the single source of truth; **do not fork it**).
- Enable Pages (Settings → Pages → deploy from `master` `/docs`).
- Resulting URL: `https://<user>.github.io/<repo>/` — obscure but public;
  acceptable for a weather tool with no secrets.

### A2. Make it a proper PWA

All edits in `spray-windows-live.html` (Android picks them up on next APK
build; they're harmless in the WebView):

- `<meta name="viewport" ... , viewport-fit=cover>` and
  `<meta name="apple-mobile-web-app-capable" content="yes">`,
  `<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">`,
  `<meta name="theme-color" content="#1f6b3b">`.
- Safe-area: the header already pads by `--inset-top` (injected by Android).
  Add an iOS fallback: `--inset-top: env(safe-area-inset-top, 0px)` as the
  default on `:root`, which the Android JS injection simply overrides.
  Also consider `env(safe-area-inset-bottom)` for the home-indicator area.
- `manifest.json` (name, icons, `display: standalone`, theme/background
  colours) + `<link rel="manifest">`. Note iOS largely ignores the manifest
  and uses `apple-touch-icon` — provide both:
  - `apple-touch-icon` 180×180 PNG
  - manifest icons 192×192 + 512×512 (maskable)
  - Icon design: green field/spray theme to match the header gradient; can
    derive from the Android launcher icon (`android/.../ic_launcher_fg.xml`).
- Optional but recommended: a small **service worker** that caches the app
  shell (the HTML itself) so it opens with no signal; the existing
  localStorage forecast cache then provides stale data. Keep it minimal —
  network-first for the HTML so updates propagate, cache-first only as
  fallback.

### A3. Test & share

- iPhone + iPad: open URL in Safari → Share → Add to Home Screen. Verify:
  full-screen launch, title clear of the notch/Dynamic Island, grid layout
  in portrait (phone) and the wide layout (tablet landscape), tooltip
  placement, refresh button, airplane-mode behaviour (stale banner).
- Tester: just send the URL with the same instructions.
- Regression-check Android APK after the meta/manifest edits (rebuild +
  reinstall; the inset injection must still win over the env() fallback).

### A4. Update flow (document in README when built)

- Edit `spray-windows-live.html` → copy lands in `docs/` → `git push` →
  Pages redeploys (~1 min). Android still needs an APK rebuild, as now.

## Phase B — native iOS shell (only if needed)

Triggers to bother: PWA offline behaviour proves inadequate, Apple changes
home-screen-app behaviour, or app-store-grade polish is wanted.

- Join Apple Developer Program (US$99/yr).
- Xcode project: SwiftUI `WKWebView` wrapper, universal (iPhone+iPad),
  loading the bundled HTML exactly like the Android `WebViewAssetLoader`
  setup; reuse the `--inset-top` injection idea via
  `WKUserScript`/`safeAreaInsets` (or just rely on the env() CSS).
- Distribute to self + testers via **TestFlight** (90-day builds, invite by
  link/email). No cables, no UDID wrangling.
- Keep `spray-windows-live.html` as the single shared page for Android, iOS
  and PWA — the shells stay dumb.

## Open decisions for David

1. Public repo: whole project public, or separate minimal publish repo?
2. Repo name (affects the URL).
3. Happy with a service worker, or keep it simplest-possible first?
4. Icon: reuse Android art or design something new?

## Kickoff prompt (paste this when resuming)

> Read PLAN-ios-pwa.md and implement Phase A. Answers to the open
> decisions: [public repo? yes/no/separate], repo name [—], service worker
> [yes/no], icon [reuse Android / new]. Install gh with brew if needed —
> I'll authenticate when you ask. Keep spray-windows-live.html as the single
> source of truth (no forked copy), make the safe-area CSS work for both the
> Android inset injection and iOS env(), and rebuild + reinstall the Android
> APK at the end to confirm nothing regressed. Update README.md and
> CLAUDE.md (files list + publish flow) when done.

If only part of Phase A gets done, update the Status line at the top of this
file and note where it stopped.
