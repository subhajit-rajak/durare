
<img width="200" height="200" src="https://github.com/user-attachments/assets/92a10466-9952-4744-b9ed-67b63183bdd9" />

# Push-Up Counter App

An Android application that uses face detection to count push-ups in real-time. The app detects your face position through the front camera and counts push-ups based on how close your face is to the camera.

## Features

- **Real-time face detection** using ML Kit
- **Automatic push-up counting** based on face position
- **High sensitivity detection** for easy testing
- **Visual feedback** with face overlay and position indicators
- **Live counter display** showing total push-ups completed

## How It Works

The app uses the following logic to detect push-ups:

1. **Down Position**: When your face takes more than 50% of the screen (face close to camera)
2. **Up Position**: When your face takes more than 25% of the screen (face at medium distance)
3. **Push-up Count**: A complete push-up is counted when you transition from down to up position

## Usage Instructions

1. **Grant Camera Permission**: Allow the app to access your front camera when prompted
2. **Position Yourself**: Stand in front of your device with the front camera facing you
3. **Start Push-ups**: 
   - Move your face close to the camera (down position)
   - Push up to move your face further from the camera (up position)
   - Repeat to count more push-ups

## Technical Details

### Dependencies
- **ML Kit Face Detection**: For real-time face detection
- **CameraX**: For camera preview and image analysis
- **AndroidX**: For modern Android development

### Sensitivity Settings
- **Down Threshold**: 50% (face takes more than 50% of screen)
- **Up Threshold**: 25% (face takes more than 25% of screen)
- **Frame Threshold**: 3 consecutive frames required to confirm position
- **Min Face Size**: 0.1f (detects very small faces for high sensitivity)

### Permissions Required
- `android.permission.CAMERA`: For accessing the front camera
- `android.hardware.camera`: Required camera hardware
- `android.hardware.camera.autofocus`: Optional autofocus feature

## Building and Running

1. Open the project in Android Studio
2. Sync Gradle files to download dependencies
3. Connect an Android device or start an emulator
4. Build and run the app
5. Grant camera permissions when prompted

## Troubleshooting

- **No face detected**: Make sure you're in a well-lit area and your face is clearly visible
- **Inconsistent counting**: Try adjusting your distance from the camera
- **App crashes**: Ensure you have granted camera permissions
- **Poor detection**: Clean your camera lens and ensure good lighting

## Customization

You can adjust the sensitivity by modifying these constants in `utils/PushUpDetector.kt/companion object`:

```kotlin
private const val DOWN_THRESHOLD = 50f  // Adjust for down position sensitivity
private const val UP_THRESHOLD = 25f    // Adjust for up position sensitivity
private const val FRAME_THRESHOLD = 3   // Adjust for position confirmation frames
```

## License

This project is open source and available under the MIT License.
