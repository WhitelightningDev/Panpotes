package com.mabbureau.panoptes;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class FingerprintDialog extends Dialog {

    private TextView messageTextView;
    private Button nextButton;
    private Context context;
    private String name;
    private String surname;
    private String id;

    public FingerprintDialog(@NonNull Context context, String name, String surname, String id) {
        super(context);
        this.context = context;
        this.name = name;
        this.surname = surname;
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fingerprint); // Create your custom dialog layout

        messageTextView = findViewById(R.id.messageTextView);
        nextButton = findViewById(R.id.nextButton);
        nextButton.setEnabled(false); // Initially disable next button

        // Set up BiometricPrompt
        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((MainActivity) context,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                messageTextView.setText("Fingerprint captured successfully!");
                nextButton.setEnabled(true); // Enable the next button
                // TODO: Save the fingerprint data securely within your app
                saveFingerprintData(name, surname, id);
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                messageTextView.setText("Error: " + errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                messageTextView.setText("Authentication failed. Please try again.");
            }
        });

        // Set up prompt information
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Scan your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        // Start the fingerprint scanning when the dialog is shown
        biometricPrompt.authenticate(promptInfo);

        // Handle next button click
        nextButton.setOnClickListener(v -> {
            dismiss(); // Dismiss the dialog
            // Proceed to the next step (e.g., face recognition)
            ((MainActivity) context).showFaceScanDialog(name, surname, id);
        });
    }

    private void saveFingerprintData(String name, String surname, String id) {
        // Example of saving in SharedPreferences (not ideal for sensitive data but for demonstration)
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_surname", surname);
        editor.putString("user_id", id);
        editor.putBoolean("fingerprint_saved", true); // Mark that fingerprint is saved
        editor.apply(); // Save changes
    }

}
