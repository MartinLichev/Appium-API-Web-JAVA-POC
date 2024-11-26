# Appium-API-Web-JAVA-POC

This repository contains an automation project that combines API testing and mobile application testing for the **AndroMoney Expense Tracker App**. The project includes API specs automated with MockServer and Appium specs for UI automation.

## Project Overview

The automation suite tests:

1. **API Specs**:

   - Endpoints are tested with MockServer for GET, POST, PUT, and DELETE requests.
   - Mock data is configured for APIs like `/people` and `/starships` (mimicking a Star Wars-themed API).

2. **Mobile UI Automation**:
   - Tests for the **AndroMoney Expense Tracker App** functionalities.
   - Appium is used for automation on an Android emulator.

---

## Prerequisites

Ensure you have the following software installed on your local machine before running the automation suite:

### General Requirements

1. **Java Development Kit (JDK)** - Version 11 or above.
2. **Maven** - For dependency management.
3. **Node.js and npm** - Required for Appium server setup.
4. **Android Studio** - To set up and manage Android emulators.
5. **Appium** - Version 2.x (installed globally via npm).
6. **MockServer** - Integrated into the project for API mocking.

### Android Emulator Setup

- Install Android Studio and ensure `adb`, `sdkmanager`, and `emulator` are available in your `PATH`.
- Configure an emulator using the following details:
  - **Name**: `Pixel_9_API_35`
  - **Android Version**: 15.0 (API 35)
  - **System Image**: `google_apis_playstore/arm64-v8a`

---

## Setting Up the Project Locally

Follow these steps to set up and run the project:

### Step 1: Clone the Repository

```bash
git clone <https://github.com/MartinLichev/Appium-API-Web-JAVA-POC.git>
cd JAVA-Appium-API-Web-POC
```

### Step 2: Step 2: Set Up Environment Variables

Create a .env file in the root directory with the following structure:

```bash
# Platform Configuration
PLATFORM_NAME=Android
DEVICE_NAME=emulator-5554
PLATFORM_VERSION=15.0
APP_PACKAGE=com.kpmoney.android
APP_ACTIVITY=com.kpmoney.home.LaunchActivity
APP_PATH=src/test/resources/app/app-debug.apk
APPIUM_SERVER=http://127.0.0.1:4723
APPIUM_NORESET=true
AUTOMATION_NAME=UiAutomator2

# APK Configuration
APK_PATH="src/test/resources/binaries/android/com.kpmoney.android_3a.13.19-362_minAPI26(arm64-v8a,armeabi,armeabi-v7a,mips,mips64,x86,x86_64)(nodpi)_apkmirror.com.apk"

# Emulator Configuration
EMULATOR_NAME=Pixel_9_API_35
DEVICE_PROFILE=pixel_3a
SYSTEM_IMAGE=system-images:android-35:google_apis_playstore:arm64-v8a
SDK_ROOT=/Users/<your_username>/Library/Android/sdk

# Image Configuration
LOCAL_IMAGE_PATH=src/test/resources/images/android-35

# API Configuration
API_BASE_URL=http://localhost:1080
```

### Step 3: Install Dependencies

Run the following command to install the Maven dependencies:

```bash
mvn clean install
```

### Step 4: Download AndroMoney APK

Download the AndroMoney APK from the following link and place it in the src/test/resources/binaries/android directory: [AndroMoney APK Download](https://www.apkmirror.com/apk/andromoney/andromoney-expense-track/andromoney-expense-track-3a-13-19-release/andromoney-expense-track-3a-13-19-android-apk-download/?redirected=download_invalid_nonce)

Ensure you download the **3a.13.19 version** with the following specifications:

- **Variant**: 3a.13.19
- **Architecture**: universal
- **Minimum Version**: Android 8.0+
- **Screen DPI**: nodpi

## Running the Automation Tests

### 1. Start the MockServer

The MockServer is configured to start automatically before API tests are executed.

### 2. Running API Tests

To execute the API specs, run the following command:

```bash
mvn test -DsuiteFile=testng-api.xml
```

### 3. Running Mobile UI Tests

To execute the Appium specs, run:

```bash
mvn test -DsuiteFile=testng-appium.xml
```

### 4. Running Both Suites Sequentially

To run API tests followed by Appium tests, use:

```bash
mvn test
```

## Project Structure

```bash
src
├── main
│   └── java
│       └── utils
│           ├── ApiUtils.java          # API Utility Class
│           ├── EmulatorManager.java   # Manages Emulator Setup
│           ├── MockServerUtils.java   # MockServer Configuration
│           └── WebDriverManager.java  # Appium WebDriver Setup
├── test
    ├── java
    │   ├── api
    │   │   └── GenericStepsAPI.java   # API Step Definitions
    │   ├── appium
    │   │   └── GenericSteps.java      # Mobile Step Definitions
    │   └── runners
    │       ├── APITestNGRunner.java   # Runner for API Specs
    │       ├── AppiumTestNGRunner.java # Runner for Mobile Specs
    └── resources
        ├── features
        │   ├── API
        │   │   ├── Setup.feature      # API Setup Specs
        │   │   └── mainFeatures.feature # Main API Specs
        │   ├── Mobile
        │   │   ├── Setup.feature      # Mobile Setup Specs
        │   │   └── mainFeatures.feature # Main Mobile Specs
        └── binaries
            └── android                # Contains AndroMoney APK
```

## Key Features

- **MockServer for API Tests:**
- **Simulates API responses to test GET, POST, PUT, and DELETE endpoints.**
- **Appium for Mobile Automation:**
- **Tests core functionalities of the AndroMoney app.**
- **Customizable Configurations:**
- **Modify the .env file to adapt to different environments and APK versions.**
- **TestNG Integration:**
- **Supports sequential and parallel execution of tests.**

## Notes

- **Emulator Management:**
  Ensure the Android emulator is running before executing Appium tests. Use the following command to start an emulator:

```bash
emulator @<EMULATOR_NAME>
```

- **MockServer:**
  MockServer is initialized automatically when API tests are run.
