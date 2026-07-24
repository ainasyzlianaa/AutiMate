package com.example.autimate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check notification permission
        checkNotificationPermission();

        // Create notification channels
        NotificationHelper.createNotificationChannels(this);

        // Schedule routine reminders
        RoutineScheduler.scheduleRoutineReminders(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignup = findViewById(R.id.tvSignup);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginUser());

        // Open Forgot Password Activity
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Open Signup Activity
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Check if user came from password reset
        handlePasswordResetIntent(getIntent());
    }

    /**
     * Check and request notification permission
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied. You won't receive reminders.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Apply theme from SharedPreferences
     */
    private void applyTheme() {
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = themePrefs.getString("theme", "light");

        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePasswordResetIntent(intent);
    }

    /**
     * Handle password reset deep link from Firebase email
     */
    private void handlePasswordResetIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String path = data.getPath();

            // Check if this is a password reset confirmation
            if (path != null && path.contains("resetPassword")) {
                Toast.makeText(this,
                        "✅ Password reset successfully!\nPlease login with your new password.",
                        Toast.LENGTH_LONG).show();

                // Clear any old password from field
                etPassword.setText("");
                etPassword.requestFocus();

                // Show a success message on the email field
                etEmail.setError(null);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
    }

    private void loginUser() {
        // Check internet connection
        if (!isNetworkAvailable()) {
            Toast.makeText(this,
                    "🌐 No internet connection. Please check your network and try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        // Disable login button during processing
        btnLogin.setEnabled(false);
        btnLogin.setText("LOGGING IN...");

        // Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Re-enable login button
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Get parent data from Firestore
                            db.collection("parents").document(userId).get()
                                    .addOnSuccessListener(document -> {
                                        String parentName = document.getString("fullName");
                                        if (parentName == null) parentName = email;

                                        // Save to SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                                        prefs.edit()
                                                .putString("parentId", userId)
                                                .putString("parentName", parentName)
                                                .apply();

                                        // Go to Child Selection
                                        Intent intent = new Intent(MainActivity.this, ChildSelectionActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // If parent document doesn't exist, still proceed with email as name
                                        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                                        prefs.edit()
                                                .putString("parentId", userId)
                                                .putString("parentName", email)
                                                .apply();

                                        Intent intent = new Intent(MainActivity.this, ChildSelectionActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";

                        // Handle specific error types
                        if (errorMessage != null) {
                            if (errorMessage.contains("There is no user record")) {
                                etEmail.setError("❌ Email not registered");
                                etEmail.requestFocus();
                                Toast.makeText(MainActivity.this,
                                        "❌ This email is not registered. Please sign up first.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("The password is invalid")) {
                                etPassword.setError("❌ Wrong password");
                                etPassword.requestFocus();
                                Toast.makeText(MainActivity.this,
                                        "❌ Wrong password. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("too many requests")) {
                                Toast.makeText(MainActivity.this,
                                        "⏳ Too many failed attempts. Please try again later.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("network")) {
                                Toast.makeText(MainActivity.this,
                                        "🌐 Network error. Please check your internet connection.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("invalid email")) {
                                etEmail.setError("❌ Invalid email address");
                                etEmail.requestFocus();
                                Toast.makeText(MainActivity.this,
                                        "❌ Invalid email address. Please check and try again.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "❌ Login failed: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "❌ Login failed. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}