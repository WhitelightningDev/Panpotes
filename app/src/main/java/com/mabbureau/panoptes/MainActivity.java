package com.mabbureau.panoptes;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.face.Face;
import com.mabbureau.panoptes.databinding.ActivityMainBinding;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int CAMERA_REQUEST_CODE = 100; // Define a request code for camera permissions
    private Bitmap lastCapturedFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            setupCamera(); // Set up the camera if permissions are granted
        }

        // Set up the floating action button click listener
        binding.fab.setOnClickListener(view -> showAddPersonDialog());
    }

    private void showFingerprintDialog(String name, String surname, String id) {
        // Create and show your custom fingerprint dialog
        FingerprintDialog fingerprintDialog = new FingerprintDialog(this, name, surname, id);
        fingerprintDialog.show(); // Show the custom fingerprint dialog
    }



    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera(); // Set up camera if permission granted
            } else {
                Snackbar.make(binding.fab, "Camera permission is required for this app.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT) // Front camera for face recognition
                        .build();

                // Ensure PreviewView is initialized correctly
                PreviewView facePreviewView = findViewById(R.id.facePreviewView);
                if (facePreviewView != null) {
                    preview.setSurfaceProvider(facePreviewView.getSurfaceProvider());
                } else {
                    Log.e(TAG, "facePreviewView is null! Check your layout.");
                    return; // Prevent further execution if facePreviewView is null
                }

                // Set up ImageAnalysis for face detection
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    // Process the imageProxy here using your face detection logic or capture a bitmap
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    if (bitmap != null) {
                        lastCapturedFrame = bitmap; // Store the frame for later retrieval
                    }
                    imageProxy.close(); // Close the imageProxy to avoid memory leaks
                });

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera setup error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showAddPersonDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_person, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);

        EditText inputName = dialogView.findViewById(R.id.input_name);
        EditText inputSurname = dialogView.findViewById(R.id.input_surname);
        EditText inputId = dialogView.findViewById(R.id.input_id);
        Button buttonNext = dialogView.findViewById(R.id.button_next);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        builder.setCancelable(true);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString();
                String surname = inputSurname.getText().toString();
                String id = inputId.getText().toString();
                dialog.dismiss(); // Close the dialog
                showFingerprintDialog(name, surname, id); // Show fingerprint dialog
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

        dialog.show();
    }




    public void showFaceScanDialog(String name, String surname, String id) {
        showFaceCaptureDialog(name, surname, id);
    }

    private void showFaceCaptureDialog(String name, String surname, String id) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_face_capture, null);

        PreviewView facePreviewView = dialogView.findViewById(R.id.facePreviewView);
        if (facePreviewView == null) {
            Log.e(TAG, "facePreviewView is null! Check your dialog layout.");
            return; // Early return if facePreviewView is null
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false);

        Button buttonCapture = dialogView.findViewById(R.id.button_capture);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        AlertDialog dialog = builder.create();

        // Show the dialog first
        dialog.show();

        // Set up the camera after the dialog is shown
        setupFaceCamera(facePreviewView);

        buttonCapture.setOnClickListener(v -> {
            Bitmap currentFrame = getCurrentCameraFrame();

            if (currentFrame == null) {
                Snackbar.make(binding.fab, "Failed to capture camera frame.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            FaceDetection faceDetection = new FaceDetection(this);
            faceDetection.startFaceScan(currentFrame, new FaceDetection.FaceScanCallback() {
                @Override
                public void onScanSuccess(List<Face> faces) {
                    Snackbar.make(binding.fab, "Face scan successful for " + name + " " + surname, Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                    saveBiometricData(name, surname, id);
                }

                @Override
                public void onScanFailure(String errorMessage) {
                    Snackbar.make(binding.fab, "Face scan failed: " + errorMessage, Snackbar.LENGTH_SHORT).show();
                }
            });
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void setupFaceCamera(PreviewView previewView) {
        Log.d(TAG, "Setting up face camera with PreviewView: " + previewView);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // Set up ImageAnalysis for frame analysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    // Convert the ImageProxy to Bitmap
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    if (bitmap != null) {
                        lastCapturedFrame = bitmap; // Store the frame for later retrieval
                    }
                    imageProxy.close(); // Close the imageProxy to avoid memory leaks
                });

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Face camera setup error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private Bitmap getCurrentCameraFrame() {
        return lastCapturedFrame; // Return the last captured frame
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        if (planes.length > 0) {
            ByteBuffer buffer = planes[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        }
        return null; // Return null if conversion fails
    }

    private void saveBiometricData(String name, String surname, String id) {
        // Save biometric data securely
        Log.d(TAG, "Biometric data saved for " + name + " " + surname + " with ID: " + id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        if (item.getItemId() == R.id.action_settings) {
            return true; // Handle settings action here if needed
        }
        return super.onOptionsItemSelected(item);
    }
}
