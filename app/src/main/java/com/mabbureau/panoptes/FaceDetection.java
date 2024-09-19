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
        // Set up face detection options
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        // Initialize the face detector
        faceDetector = com.google.mlkit.vision.face.FaceDetection.getClient(options);
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

        // Convert Bitmap to InputImage
        InputImage image = InputImage.fromBitmap(currentFrame, 0);


        // Start face detection
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Log.d(TAG, "Face detected: " + faces.size());
                        callback.onScanSuccess(faces); // Notify success
                    } else {
                        callback.onScanFailure("No face detected.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed: " + e.getMessage());
                    callback.onScanFailure("Face detection failed: " + e.getMessage());
                });
    }

    public void stopFaceScan() {
        if (faceDetector != null) {
            faceDetector.close(); // Close the detector when done
        }
    }
}