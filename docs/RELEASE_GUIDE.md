# Shipping Sippy to Google Play

1. **Finalise identity and version.** Choose the final applicationId (e.g. `com.yourcompany.sippy`) in `app/build.gradle.kts` (`namespace` must match). Set `versionCode` (start at 1, increment for every upload) and `versionName` (e.g. `1.0.0`). Never reuse a versionCode.

2. **Create an upload keystore.**

```bash
keytool -genkeypair -v -keystore ~/sippy-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sippy-upload
```

Create `keystore.properties` in the repository root (git-ignored):

```
storeFile=/absolute/path/to/sippy-upload.jks
storePassword=YOUR_PASSWORD
keyAlias=sippy-upload
keyPassword=YOUR_PASSWORD
```

Gradle already reads this file in `app/build.gradle.kts` and signs `release`. Back up the keystore and passwords outside the repository: Play App Signing cannot recover a lost upload key.

3. **R8 and resource shrinking.** `app/build.gradle.kts` sets `isMinifyEnabled = true` and `isShrinkResources = true` for release, with `app/proguard-rules.pro` keeping Room entities/daos/database subclasses and Hilt/Dagger classes (useful if you migrate DI later). Run `./gradlew :app:dependencies` and test the release variant on a device before upload.

4. **Build and verify the bundle.**

```bash
./gradlew clean testDebugUnitTest bundleRelease
ls -lh app/build/outputs/bundle/release/app-release.aab
jarsigner -verify -verbose app/build/outputs/bundle/release/app-release.aab   # verifies signature metadata
```

Install the release APK variant locally (`./gradlew assembleRelease`) and smoke-test onboarding, logging, reminders, history and restore-after-restart.

5. **Play Console setup.** Create a personal or organisation developer account and pay the one-time fee; add a payment profile if the app will ever be paid. Create app → default language English (US) → Free. Store listing draft:

   - **Title:** Sippy — Water Friend Tracker
   - **Short description (80 chars):** "Track water with tiny friends who cheer every sip."
   - **Full description:** "Sippy turns hydration into a tiny daily ritual. Pick a Droplet, Sprout, Kitty or Blobby, name it, and watch it grow as you log water. Big animated reactions, a daily goal ring, streaks, seven- and thirty-day history, and friendly reminders make it easy to keep going. Everything is stored on your device — no account, no ads, no tracking. Choose your own goal, units, reminder interval, waking hours and accent theme. Friends gain growth points and evolve through Baby, Young, Grown and Elder stages. Reach your goal and they dance!"
   - **Screenshots:** at least 2, up to 8 per device type, PNG/JPEG, 320–3840 px, 16:9 for phone; capture Home (friend + ring), Friends grid, History chart, Settings. Feature graphic: **1024 × 500 px** JPG/PNG (make it from the vector icon plus a short tagline). App icon: 512×512 PNG 32-bit.
   - **Content rating:** complete the questionnaire (no violence, no user-generated content, no ads; "Family" / Everyone is the likely result).
   - **Data safety:** answer that **no data is collected and no data is shared**. On-device data (friends, logs, settings) is stored only locally and never transmitted. Declare no encryption in transit (nothing is transmitted). Data deletion is in-app via Settings → Developer options → Clear all data (after unlocking) and by uninstalling; for a production build consider adding a visible Settings entry for deletion.
   - **Target audience:** not directed at children; select "18+" or an adult audience if asked, or "Families" with no child-directed content. Do not mark as "Designed for families" unless you complete the families policy requirements.
   - **Privacy policy:** host `docs/PRIVACY_POLICY.md` at a public HTTPS URL and paste it into the listing.

6. **App signing and testing.** In Play Console → App signing, enrol in Play App Signing (generate/upload the app signing key with `pepk` or let Google generate it). Create Internal testing, upload the `.aab`, add tester email lists, and install via the Play Store test link. Then create a Closed testing track: **new personal developer accounts must run a closed test with at least 12 testers continuously enrolled for 14 days** before they can request production access (requirements can change — check Play Console). Once approved, promote the same release to Production with a staged rollout.

7. **Pre-launch checklist.**

   - Test on API 26, 28, 33 and 35 devices/emulator images, including dark mode, system font scaling and "Remove animations".
   - Grant/deny POST_NOTIFICATIONS; verify exact-alarm fallback and the "Log 250 ml" notification action.
   - Verify onboarding, one-friend flow, eight-friend limit, last-friend protection, midnight/time-zone rollover, process death (Don't keep activities), and backup/restore.
   - Check that private data is not logged, permissions match the manifest, screenshots are honest, and the privacy URL resolves.
   - Upload a fresh build after every versionCode bump; never reuse a versionCode.

   Common rejections: misleading screenshots or descriptions, undeclared permissions/SDKs, broken notification permission flow, crashes on older Android, missing privacy policy, child-directed design without a Families declaration, and a private or unreachable privacy policy URL.

8. **Optional GitHub Actions.** `.github/workflows/release.yml` decodes a base64 keystore from repository secrets (`SIPPY_KEYSTORE_BASE64`, `SIPPY_KEYSTORE_PASSWORD`, `SIPPY_KEY_ALIAS`, `SIPPY_KEY_PASSWORD`), runs unit tests, builds `bundleRelease` and uploads the AAB as an artifact on tag `v*` or manual dispatch. Create those secrets in Settings → Secrets and variables → Actions.
