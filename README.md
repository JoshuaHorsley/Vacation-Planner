# Trip Planner

> A native Android app for planning vacations, built to explore the breadth of the Android platform, from SQLite persistence to home-screen widgets.

Trip Planner lets you create trips, search for real destinations via Google Places, see the weather for where you're headed, add travel companions from your contacts, and keep everything saved on-device. A home-screen widget and reminder notifications keep your next trip in view.

---

## Screenshots

<!--

**Trip List**
<img width="300" alt="Trip list" src="PASTE_LINK_HERE" />

**Trip Details**
<img width="300" alt="Trip details" src="PASTE_LINK_HERE" />

**Weather Feed**
<img width="300" alt="Weather" src="PASTE_LINK_HERE" />
-->

---

## Features

| Feature | Description |
|---------|-------------|
| **Trip management** | Create, view, edit, and delete trips, each stored locally in SQLite |
| **Destination search** | Autocomplete real places using the [Google Places API](https://developers.google.com/maps/documentation/places/android-sdk) |
| **Weather** | Pull current conditions for a trip's destination from [OpenWeatherMap](https://openweathermap.org/api) |
| **Travel companions** | Add people to a trip, importing names from the device's **Contacts** |
| **Trip summary** | A consolidated summary view of a trip's details |
| **Export to file** | Save trip details (including weather) out to a file via the File Manager |
| **Home-screen widget** | An App Widget showing trip info on the launcher |
| **Reminders** | Scheduled notifications remind you of upcoming trips |
| **Offline awareness** | A network-change receiver detects connectivity and disables online features gracefully |

---

## Architecture

A single-module Android app (`:app`) written primarily in **Java**, using classic Activities with XML layouts. It deliberately touches most of the core Android component types:

- **Activities** — `MainActivity` (trip list), `TripDetailsActivity` (create/edit), `TripDetailViewActivity`, `PeopleActivity`, `SummaryActivity`, `WeatherFeedActivity`, `FileManagerActivity`
- **Persistence** — a SQLite `DatabaseHelper` with DAOs (`TripDAO`, `PeopleDAO`) and models (`TripModel`, `PeopleModel`)
- **Content Provider** — `TripContentProvider` exposes trip data through a `content://` URI
- **Service** — `TripNotificationService` for trip notifications
- **Broadcast Receivers** — `NetworkChangeReceiver` (connectivity) and `TripReminderReceiver` (reminder alarms)
- **App Widget** — `TripWidgetProvider` for the home-screen widget
- **Application class** — `TripPlannerApplication` for app-wide setup
- **Utilities** — connectivity, contacts, file, permissions, and widget helpers

### Tech stack

- **Language:** Java (with Kotlin/Compose theme scaffolding from the project template)
- **Build:** Gradle (Kotlin DSL), `compileSdk` 35, `minSdk` 28
- **APIs:** Google Places SDK for Android, OpenWeatherMap REST API

---

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (recent version)
- An Android emulator or a physical device running **Android 9 (API 28)** or newer
- A **Google Places (Maps) API key** - [get one here](https://developers.google.com/maps/documentation/places/android-sdk/cloud-setup)
- An **OpenWeatherMap API key** - [free tier here](https://home.openweathermap.org/users/sign_up)

### Configuration

API keys are **not** stored in source. They're read from `local.properties` (which is gitignored) and injected at build time via `BuildConfig` and a manifest placeholder.

Add the following to your `local.properties` file at the project root:

```properties
MAPS_API_KEY=your_google_places_api_key
WEATHER_API_KEY=your_openweathermap_api_key
```

That's it, the Gradle build wires them into the app automatically:
- `MAPS_API_KEY` → the `com.google.android.geo.API_KEY` manifest placeholder and `Places.initialize(...)`
- `WEATHER_API_KEY` → `BuildConfig.WEATHER_API_KEY`, used by the weather download tasks

> **Restrict your keys.** For the Google key, add an Android application restriction (package name `com.example.assignment1` + your signing SHA-1) and limit it to the Places/Maps APIs in the Google Cloud Console.

### Run

1. Open the project in **Android Studio** and let Gradle sync.
2. Add your keys to `local.properties` (above).
3. Select an emulator or connected device and press **Run**.

### Build from the command line

```bash
# macOS/Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

The built APK lands in `app/build/outputs/apk/debug/`.
