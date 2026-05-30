<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/bde769e0-1371-40cc-8849-ed5a01e9f56c

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

## Build APK Locally

To build an APK file on your local machine:

### Prerequisites
- JDK 11 or higher
- Android SDK (installed via Android Studio)
- Gradle (included with Android Studio)

### Build Debug APK
```bash
./gradlew assembleDebug
```
The debug APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```
**Note:** Building a release APK requires signing credentials. You'll need to:
1. Create a keystore file (`my-upload-key.jks`)
2. Set environment variables:
   - `KEYSTORE_PATH`: Path to your keystore file
   - `STORE_PASSWORD`: Keystore password
   - `KEY_PASSWORD`: Key password
   - `GEMINI_API_KEY`: Your Gemini API key

## Automatic APK Builds with GitHub Actions

This project includes a GitHub Actions workflow that automatically builds APK files on every push to the main branch and creates releases when you push version tags.

### Setup (One-time)
Add the following secrets to your GitHub repository settings (`Settings > Secrets and variables > Actions`):

1. **GEMINI_API_KEY** - Your Google Gemini API key (required)
2. **DEBUG_KEYSTORE_BASE64** - Base64 encoded debug.keystore file (optional, but recommended)
   - To encode your keystore: `base64 debug.keystore | tr -d '\n' | pbcopy` (macOS) or `base64 -w 0 debug.keystore` (Linux)
3. **KEYSTORE_PATH**, **STORE_PASSWORD**, **KEY_PASSWORD** - For release APK signing (optional)

### Triggering Builds

The workflow runs automatically on:
- **Push to main branch** - Builds debug APK automatically
- **Git tags starting with 'v'** (e.g., `v1.0.0`) - Builds and creates a release with the APK
- **Manual trigger** - Visit `Actions` tab and click "Run workflow"

### Accessing Built APKs

- **Automatic builds**: Download from the `Actions` tab under the workflow run's artifacts
- **Releases**: Download from the `Releases` page when you create a version tag

## Project Structure

- `app/` - Android app source code
- `.github/workflows/` - GitHub Actions configurations
- `gradle/` - Gradle configuration files
- `build.gradle.kts` - Root build configuration
- `app/build.gradle.kts` - App-specific build configuration

## Support

For issues or questions, please open a GitHub issue or contact the project maintainers.
