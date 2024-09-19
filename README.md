# Panoptes Biometric Recognition App

## Overview

The **Panoptes Facial Recognition App** is an Android application that allows users to scan faces using their device’s camera. The app leverages **Google ML Kit** to perform real-time facial detection and recognition, allowing you to easily capture and process biometric data. 

This application is useful for securely enrolling users with facial recognition as a biometric input, while keeping the biometric data within the application without storing it in the device’s native settings. Additionally, the app integrates other biometrics, such as fingerprint scanning.

## Features

- **Face Scanning**: Capture and detect faces using the device’s camera in real-time.
- **Biometric Enrollment**: Add and manage users’ facial data directly within the app (no integration with device settings).
- **Google ML Kit Integration**: Built-in machine learning models for face detection and classification.
- **External Fingerprint Scanner Integration**: Option to add and use external fingerprint scanners (like Columbo 2.0) for fingerprint enrollment and verification.
- **Real-time Feedback**: Notify users whether a face is successfully detected or if there is a failure (e.g., no face detected).
- **User-Friendly UI**: A sleek Material Design 3 interface for a modern and intuitive user experience.

## Prerequisites

Before installing or running the app, ensure that your development environment includes the following:

- **Android Studio** (Latest stable version recommended)
- **Gradle** (Configured for your project)
- **Minimum SDK Version**: 21 (Android 5.0 Lollipop)
- **Google ML Kit** for Face Detection
- **External Camera Access**: Ensure that the app has the necessary camera and storage permissions.

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-repository/panoptes-facial-recognition-app.git
   cd panoptes-facial-recognition-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio and open the project from the cloned repository.
   - Sync the project with Gradle to ensure all dependencies are installed.

3. **Add Google ML Kit to Gradle**
   Ensure the following is included in your `build.gradle` file:
   ```gradle
   dependencies {
       // ML Kit Face Detection API
       implementation 'com.google.mlkit:face-detection:16.1.5'
   }
   ```

4. **Add Camera Permissions** to `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

5. **Run the Application**:
   - Connect a physical Android device (emulators may not work correctly with the camera APIs).
   - Click the **Run** button in Android Studio or use `Shift + F10`.

## Usage

1. **Launch the Application**: Open the Panoptes Facial Recognition App on your Android device.
   
2. **Add New User**: 
   - On the main screen, select the option to add a new user.
   - Enter the user’s details such as name, surname, and ID.

3. **Face Scan**:
   - Upon clicking "Next," the app will prompt you to scan the user’s face.
   - Hold the device's camera in front of the user’s face to allow the app to detect their face.
   - If the scan is successful, the app will confirm and store the biometric data locally within the app.

4. **Fingerprint Scan (Optional)**:
   - If integrated, users can scan their fingerprint using the device’s scanner or an external fingerprint scanner like Columbo 2.0.

5. **Error Handling**: 
   - If the app fails to detect a face or fingerprint, appropriate messages will appear prompting the user to retry or cancel.

## Code Overview

### `FaceDetection.java`
This class handles the core functionality of the face detection process using **Google ML Kit**. It initializes the face detector, processes the camera frames, and communicates the success or failure of the detection through callbacks.

- **`initializeFaceDetector()`**: Sets up the detector with specific options.
- **`startFaceScan(Bitmap currentFrame, FaceScanCallback callback)`**: Scans the provided frame for faces and returns results.
- **`stopFaceScan()`**: Cleans up resources when face detection is no longer needed.
- **`FaceScanCallback` Interface**: Defines methods for success and failure callbacks during face scanning.

### `MainActivity.java`
The main activity handles the user interface and interactions, such as starting face scans, collecting user details, and displaying results.

### Permissions
The app requests runtime permissions for camera access and storage to capture and process biometric data.

## Future Improvements

- **Facial Recognition**: Add the ability to not only detect faces but also recognize specific users.
- **Multiple Biometric Enrollments**: Expand the app to support more biometric features like voice and iris scanning.
- **Data Encryption**: Implement encryption for securely storing biometric data.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
