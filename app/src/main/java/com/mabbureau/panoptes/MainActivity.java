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
    private PreviewView facePreviewView; // PreviewView to show the camera feed
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

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
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

                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider()); // Face preview

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Analyzer for face detection
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new FaceAnalyzer());

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera setup error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void showAddPersonDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_person, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true);

        EditText inputName = dialogView.findViewById(R.id.input_name);
        EditText inputSurname = dialogView.findViewById(R.id.input_surname);
        EditText inputId = dialogView.findViewById(R.id.input_id);
        Button buttonNext = dialogView.findViewById(R.id.button_next);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set click listener for the "Next" button
        buttonNext.setOnClickListener(v -> {
            String name = inputName.getText().toString();
            String surname = inputSurname.getText().toString();
            String id = inputId.getText().toString();
            dialog.dismiss(); // Close the dialog
            showFingerprintDialog(name, surname, id); // Show fingerprint dialog
        });

        // Set click listener for the "Cancel" button
        buttonCancel.setOnClickListener(v -> dialog.dismiss()); // Close the dialog

        dialog.show(); // Show the dialog
    }

    private void showFingerprintDialog(String name, String surname, String id) {
        // Create and show the custom fingerprint dialog
        FingerprintDialog fingerprintDialog = new FingerprintDialog(this, name, surname, id);
        fingerprintDialog.show();
    }

    private void showExternalFingerprintScannerDialog(String name, String surname, String id) {
        // Dialog for external fingerprint scanner
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("External Fingerprint Scanner")
                .setMessage("Please connect the Columbo 2.0 fingerprint scanner.")
                .setCancelable(true)
                .setPositiveButton("Next", (dialog, which) -> initiateExternalScanner(name, surname, id))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void initiateExternalScanner(String name, String surname, String id) {
        // Example logic for the external fingerprint scanner
        // Replace with actual scanner implementation
    }

    private boolean isFingerprintHardwareAvailable() {
        // Check if the device has fingerprint hardware
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
    }

    public void showFaceScanDialog(String name, String surname, String id) {
        showFaceCaptureDialog(name, surname, id);
    }

    private void showFaceCaptureDialog(String name, String surname, String id) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_face_capture, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false);

        facePreviewView = dialogView.findViewById(R.id.facePreviewView); // PreviewView for face preview
        Button buttonCapture = dialogView.findViewById(R.id.button_capture);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        AlertDialog dialog = builder.create();

        setupFaceCamera(facePreviewView); // Set up the face preview camera

        buttonCapture.setOnClickListener(v -> {
            Bitmap currentFrame = getCurrentCameraFrame(facePreviewView); // Placeholder for camera frame

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

        buttonCancel.setOnClickListener(v -> dialog.dismiss()); // Close the dialog

        dialog.show();
    }


    private void setupFaceCamera(PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // Set up ImageAnalysis to analyze the camera frames
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Assign the analyzer to process the camera frames
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        // Process the frame and convert to Bitmap
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        if (bitmap != null) {
                            // Use the bitmap as needed
                        }
                        imageProxy.close(); // Close the frame after use
                    }
                });

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Face camera setup error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        return bitmap;
    }




    private Bitmap getCurrentCameraFrame(PreviewView previewView) {
        return lastCapturedFrame; // Return the last captured frame from the analyzer
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
