package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        });

        // Open Signup Activity
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
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

        // Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

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
                    } else {
                        String errorMessage = task.getException().getMessage();
                        if (errorMessage.contains("There is no user record")) {
                            etEmail.setError("Email not registered");
                            etEmail.requestFocus();
                        } else if (errorMessage.contains("The password is invalid")) {
                            etPassword.setError("Wrong password");
                            etPassword.requestFocus();
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}