package com.mabbureau.panoptes;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import java.util.List;

public class FaceDetection {
    private static final String TAG = "FaceDetection";
    public final Context context;
    private FaceDetector faceDetector; // Use the correct type here

    public FaceDetection(Context context) {
        this.context = context;
        initializeFaceDetector(); // Initialize the face detector
    }

    private void initializeFaceDetector() {
        // Set up the face detector options
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // Use fast mode for quick detection
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Classify facial features
                .build();

        // Initialize the face detector with the options from the ML Kit library
        faceDetector = com.google.mlkit.vision.face.FaceDetection.getClient(options); // Ensure the correct method call
    }

    public Context getContext() {
        return context;
    }

    public interface FaceScanCallback {
        void onScanSuccess(List<Face> faces);
        void onScanFailure(String errorMessage);
    }

    public void startFaceScan(Bitmap currentFrame, FaceScanCallback callback) {
        if (currentFrame == null) {
            callback.onScanFailure("Current frame is null.");
            return;
        }

        // Convert the current frame to an InputImage for face detection
        InputImage image = InputImage.fromBitmap(currentFrame, 0);

        // Use the face detector to analyze the current frame
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Log.d(TAG, "Face detected: " + faces.size());
                        callback.onScanSuccess(faces); // Notify success
                    } else {
                        Log.d(TAG, "No face detected.");
                        callback.onScanFailure("No face detected."); // Notify failure
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection error: " + e.getMessage());
                    callback.onScanFailure("Face detection error: " + e.getMessage()); // Notify error
                });
    }

    public void stopFaceScan() {
        // Stop any ongoing face detection processes if applicable
        if (faceDetector != null) {
            faceDetector = null; // Clear reference to the face detector
        }
    }
}