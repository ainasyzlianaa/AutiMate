package com.example.autimate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private ProgressBar progressBar;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
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

    private void registerUser() {
        // Check internet connection
        if (!isNetworkAvailable()) {
            Toast.makeText(this,
                    "🌐 No internet connection. Please check your network and try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be 6+ characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);
        btnSignup.setText("CREATING ACCOUNT...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(userId, fullName, email);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSignup.setEnabled(true);
                        btnSignup.setText("SIGN UP");

                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";

                        // Handle specific error types
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError("This email is already registered");
                            etEmail.requestFocus();
                            Toast.makeText(SignupActivity.this,
                                    "❌ This email is already registered. Please login or use another email.",
                                    Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("network")) {
                            Toast.makeText(SignupActivity.this,
                                    "🌐 Network error. Please check your internet connection.",
                                    Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("invalid email")) {
                            etEmail.setError("Invalid email address");
                            etEmail.requestFocus();
                            Toast.makeText(SignupActivity.this,
                                    "❌ Invalid email address. Please check and try again.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignupActivity.this,
                                    "❌ Signup Failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String email) {
        Map<String, Object> parent = new HashMap<>();
        parent.put("userId", userId);
        parent.put("fullName", fullName);
        parent.put("email", email);
        parent.put("createdAt", FieldValue.serverTimestamp());
        parent.put("children", new ArrayList<>());

        db.collection("parents").document(userId)
                .set(parent)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("parentId", userId)
                            .putString("parentName", fullName)
                            .putString("parentEmail", email)
                            .apply();

                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    btnSignup.setText("SIGN UP");
                    Toast.makeText(SignupActivity.this,
                            "✅ Account created! Please login.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    btnSignup.setText("SIGN UP");

                    if (e.getMessage() != null && e.getMessage().contains("network")) {
                        Toast.makeText(SignupActivity.this,
                                "🌐 Network error. Please check your connection.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this,
                                "❌ Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}