package com.example.autimate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddChildProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView avatarCat, avatarDog, avatarRabbit, avatarBear;
    private CardView avatarPanda, avatarFox, avatarPenguin, avatarOwl;
    private CardView avatarUnicorn, avatarDragon, avatarAlien, avatarRobot;
    private CardView avatarStar, avatarHeart, avatarSmile, avatarSparkle;
    private EditText etChildName, etAge;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private RadioGroup radioGender;
    private RadioButton rbMale, rbFemale;
    private TextView tvSelectedAvatar;
    private Button btnAddChild;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userEmail, userFullName, userUsername, userPassword;
    private String selectedAvatar = "🐱";
    private String selectedAvatarName = "Cat";
    private String selectedGender = "Male";
    private String selectedDateOfBirth = "";

    // Array of all avatar cards for easy reset
    private CardView[] allAvatars;

    // Lists for spinners
    private ArrayList<String> dayList, monthList, yearList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get parent data from intent
        userEmail = getIntent().getStringExtra("userEmail");
        userFullName = getIntent().getStringExtra("userFullName");
        userUsername = getIntent().getStringExtra("userUsername");
        userPassword = getIntent().getStringExtra("userPassword");

        // If coming from selection page (no intent data), use default
        if (userEmail == null || userEmail.isEmpty()) {
            // Try to get from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
            userEmail = prefs.getString("parentEmail", "");
            userFullName = prefs.getString("parentName", "");
            userPassword = "";
            userUsername = "";
        }

        // If still empty, try to get from FirebaseUser
        if (userEmail == null || userEmail.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                // Try to get fullName from SharedPreferences
                SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                userFullName = prefs.getString("parentName", "");
            }
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);

        // Animal avatars
        avatarCat = findViewById(R.id.avatarCat);
        avatarDog = findViewById(R.id.avatarDog);
        avatarRabbit = findViewById(R.id.avatarRabbit);
        avatarBear = findViewById(R.id.avatarBear);
        avatarPanda = findViewById(R.id.avatarPanda);
        avatarFox = findViewById(R.id.avatarFox);
        avatarPenguin = findViewById(R.id.avatarPenguin);
        avatarOwl = findViewById(R.id.avatarOwl);

        // Fantasy avatars
        avatarUnicorn = findViewById(R.id.avatarUnicorn);
        avatarDragon = findViewById(R.id.avatarDragon);
        avatarAlien = findViewById(R.id.avatarAlien);
        avatarRobot = findViewById(R.id.avatarRobot);

        // Cute avatars
        avatarStar = findViewById(R.id.avatarStar);
        avatarHeart = findViewById(R.id.avatarHeart);
        avatarSmile = findViewById(R.id.avatarSmile);
        avatarSparkle = findViewById(R.id.avatarSparkle);

        etChildName = findViewById(R.id.etChildName);
        etAge = findViewById(R.id.etAge);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        radioGender = findViewById(R.id.radioGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        tvSelectedAvatar = findViewById(R.id.tvSelectedAvatar);
        btnAddChild = findViewById(R.id.btnAddChild);
        progressBar = findViewById(R.id.progressBar);

        // Initialize spinners
        setupSpinners();

        // Initialize all avatars array
        allAvatars = new CardView[]{
                avatarCat, avatarDog, avatarRabbit, avatarBear,
                avatarPanda, avatarFox, avatarPenguin, avatarOwl,
                avatarUnicorn, avatarDragon, avatarAlien, avatarRobot,
                avatarStar, avatarHeart, avatarSmile, avatarSparkle
        };

        // Set default selected
        avatarCat.setCardBackgroundColor(getColor(R.color.soft_blue));
        tvSelectedAvatar.setText("Selected: 🐱 Cat");

        // Setup avatar click listeners
        setupAvatarClick(avatarCat, "🐱", "Cat");
        setupAvatarClick(avatarDog, "🐶", "Dog");
        setupAvatarClick(avatarRabbit, "🐰", "Rabbit");
        setupAvatarClick(avatarBear, "🐻", "Bear");
        setupAvatarClick(avatarPanda, "🐼", "Panda");
        setupAvatarClick(avatarFox, "🦊", "Fox");
        setupAvatarClick(avatarPenguin, "🐧", "Penguin");
        setupAvatarClick(avatarOwl, "🦉", "Owl");
        setupAvatarClick(avatarUnicorn, "🦄", "Unicorn");
        setupAvatarClick(avatarDragon, "🐉", "Dragon");
        setupAvatarClick(avatarAlien, "👽", "Alien");
        setupAvatarClick(avatarRobot, "🤖", "Robot");
        setupAvatarClick(avatarStar, "⭐", "Star");
        setupAvatarClick(avatarHeart, "❤️", "Heart");
        setupAvatarClick(avatarSmile, "😊", "Smile");
        setupAvatarClick(avatarSparkle, "✨", "Sparkle");

        // Gender selection
        radioGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                selectedGender = "Male";
            } else if (checkedId == R.id.rbFemale) {
                selectedGender = "Female";
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnAddChild.setOnClickListener(v -> showConfirmationDialog());
    }

    private void setupSpinners() {
        // Day Spinner (1-31)
        dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.format("%02d", d));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dayList);
        spinnerDay.setAdapter(dayAdapter);

        // Month Spinner (1-12)
        monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.format("%02d", m));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, monthList);
        spinnerMonth.setAdapter(monthAdapter);

        // Year Spinner (1900 to current year)
        yearList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1900; y--) {
            yearList.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearList);
        spinnerYear.setAdapter(yearAdapter);

        // Set default to current date
        Calendar calendar = Calendar.getInstance();
        int defaultDay = calendar.get(Calendar.DAY_OF_MONTH);
        int defaultMonth = calendar.get(Calendar.MONTH) + 1;
        int defaultYear = calendar.get(Calendar.YEAR);

        spinnerDay.setSelection(dayList.indexOf(String.format("%02d", defaultDay)));
        spinnerMonth.setSelection(monthList.indexOf(String.format("%02d", defaultMonth)));
        spinnerYear.setSelection(yearList.indexOf(String.valueOf(defaultYear)));
    }

    private String getSelectedDateOfBirth() {
        String day = dayList.get(spinnerDay.getSelectedItemPosition());
        String month = monthList.get(spinnerMonth.getSelectedItemPosition());
        String year = yearList.get(spinnerYear.getSelectedItemPosition());
        return month + "/" + day + "/" + year;
    }

    private void setupAvatarClick(CardView avatar, String emoji, String name) {
        avatar.setOnClickListener(v -> {
            selectedAvatar = emoji;
            selectedAvatarName = name;
            tvSelectedAvatar.setText("Selected: " + emoji + " " + name);

            // Reset all avatar backgrounds
            for (CardView av : allAvatars) {
                av.setCardBackgroundColor(getColor(R.color.white));
            }
            avatar.setCardBackgroundColor(getColor(R.color.soft_blue));
        });
    }

    private void showConfirmationDialog() {
        // Get input values
        String childName = etChildName.getText().toString().trim();
        String childAge = etAge.getText().toString().trim();
        selectedDateOfBirth = getSelectedDateOfBirth();

        // Validate inputs
        if (childName.isEmpty()) {
            etChildName.setError("Child name required");
            etChildName.requestFocus();
            return;
        }

        // Validate age (must be between 5 and 12)
        if (childAge.isEmpty()) {
            etAge.setError("Age required");
            etAge.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(childAge);
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid number");
            etAge.requestFocus();
            return;
        }

        if (age < 5 || age > 12) {
            etAge.setError("Age must be between 5 and 12");
            etAge.requestFocus();
            Toast.makeText(this,
                    "⚠️ Age must be between 5 and 12 years old.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Inflate the confirmation dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_child, null);

        // Find views in dialog
        TextView tvConfirmName = dialogView.findViewById(R.id.tvConfirmName);
        TextView tvConfirmAge = dialogView.findViewById(R.id.tvConfirmAge);
        TextView tvConfirmGender = dialogView.findViewById(R.id.tvConfirmGender);
        TextView tvConfirmAvatar = dialogView.findViewById(R.id.tvConfirmAvatar);
        TextView tvConfirmAvatarName = dialogView.findViewById(R.id.tvConfirmAvatarName);
        TextView tvConfirmDob = dialogView.findViewById(R.id.tvConfirmDob);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Set data to dialog
        tvConfirmName.setText(childName);
        tvConfirmAge.setText(childAge + " years old");
        tvConfirmGender.setText(selectedGender);
        tvConfirmAvatar.setText(selectedAvatar);
        tvConfirmAvatarName.setText(selectedAvatarName);
        tvConfirmDob.setText(selectedDateOfBirth);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Cancel button - close dialog
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Submit button - save the child profile
        btnSubmit.setOnClickListener(v -> {
            dialog.dismiss();
            saveChildProfile(childName, childAge);
        });
    }

    private void saveChildProfile(String childName, String childAge) {
        progressBar.setVisibility(View.VISIBLE);
        btnAddChild.setEnabled(false);
        btnAddChild.setText("SAVING...");

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Already logged in - save directly
            String userId = currentUser.getUid();

            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";
            }

            if (userFullName == null || userFullName.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                userFullName = prefs.getString("parentName", "");
            }

            if (userFullName == null || userFullName.isEmpty()) {
                getUserDataFromFirestore(userId, childName, childAge);
                return;
            }

            saveParentAndChildToFirestore(userId, childName, childAge);
        } else {
            // Need to create account first
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            saveParentAndChildToFirestore(userId, childName, childAge);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnAddChild.setEnabled(true);
                            btnAddChild.setText("NEXT");
                            Toast.makeText(AddChildProfileActivity.this,
                                    "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void getUserDataFromFirestore(String userId, String childName, String childAge) {
        db.collection("parents").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userFullName = documentSnapshot.getString("fullName");
                        userEmail = documentSnapshot.getString("email");
                    }
                    saveParentAndChildToFirestore(userId, childName, childAge);
                })
                .addOnFailureListener(e -> {
                    saveParentAndChildToFirestore(userId, childName, childAge);
                });
    }

    private void saveParentAndChildToFirestore(String userId, String childName, String childAge) {
        // 1. Check if parent exists and get data
        db.collection("parents").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    final String finalFullName;
                    final String finalEmail;

                    if (documentSnapshot.exists()) {
                        String docFullName = documentSnapshot.getString("fullName");
                        String docEmail = documentSnapshot.getString("email");

                        if (docFullName != null && !docFullName.isEmpty()) {
                            finalFullName = docFullName;
                        } else {
                            finalFullName = userFullName;
                        }

                        if (docEmail != null && !docEmail.isEmpty()) {
                            finalEmail = docEmail;
                        } else {
                            finalEmail = userEmail;
                        }
                    } else {
                        finalFullName = userFullName;
                        finalEmail = userEmail;
                    }

                    // 2. Save/Update Parent
                    Map<String, Object> parent = new HashMap<>();
                    parent.put("userId", userId);
                    parent.put("fullName", finalFullName);
                    parent.put("email", finalEmail);
                    parent.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("parents").document(userId)
                            .set(parent)
                            .addOnSuccessListener(aVoid -> {
                                // ===== STEP 3: Save Child =====
                                Map<String, Object> child = new HashMap<>();
                                child.put("parentId", userId);
                                child.put("parentName", finalFullName);
                                child.put("childName", childName);
                                child.put("age", childAge);
                                child.put("gender", selectedGender);
                                child.put("avatar", selectedAvatar);
                                child.put("dob", selectedDateOfBirth);
                                child.put("createdAt", FieldValue.serverTimestamp());
                                // IMPORTANT: New child starts with EMPTY activities array
                                child.put("activities", new ArrayList<>());

                                db.collection("children").add(child)
                                        .addOnSuccessListener(documentReference -> {
                                            String childId = documentReference.getId();

                                            // ===== STEP 4: Add Child ID to Parent =====
                                            db.collection("parents").document(userId)
                                                    .update("children", FieldValue.arrayUnion(childId))
                                                    .addOnSuccessListener(aVoid2 -> {})
                                                    .addOnFailureListener(e -> {});

                                            // ===== STEP 5: NO DEFAULT ACTIVITIES =====

                                            // ===== STEP 6: Save to SharedPreferences =====
                                            SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                                            childPrefs.edit()
                                                    .putString("childId", childId)
                                                    .putString("childName", childName)
                                                    .putString("childAvatar", selectedAvatar)
                                                    .apply();

                                            SharedPreferences parentPrefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                                            parentPrefs.edit()
                                                    .putString("parentId", userId)
                                                    .putString("parentName", finalFullName)
                                                    .putString("parentEmail", finalEmail)
                                                    .apply();

                                            // ===== STEP 7: Navigate to Child Selection =====
                                            progressBar.setVisibility(View.GONE);
                                            btnAddChild.setEnabled(true);
                                            btnAddChild.setText("NEXT");
                                            Toast.makeText(AddChildProfileActivity.this,
                                                    "✅ Child profile created successfully!", Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(AddChildProfileActivity.this, ChildSelectionActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnAddChild.setEnabled(true);
                                            btnAddChild.setText("NEXT");
                                            Toast.makeText(AddChildProfileActivity.this,
                                                    "❌ Error saving child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnAddChild.setEnabled(true);
                                btnAddChild.setText("NEXT");
                                Toast.makeText(AddChildProfileActivity.this,
                                        "❌ Error saving parent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    saveParentAndChildDirect(userId, childName, childAge);
                });
    }

    private void saveParentAndChildDirect(String userId, String childName, String childAge) {
        final String finalFullName = userFullName;
        final String finalEmail = userEmail;

        Map<String, Object> parent = new HashMap<>();
        parent.put("userId", userId);
        parent.put("fullName", finalFullName);
        parent.put("email", finalEmail);
        parent.put("createdAt", FieldValue.serverTimestamp());

        db.collection("parents").document(userId)
                .set(parent)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> child = new HashMap<>();
                    child.put("parentId", userId);
                    child.put("parentName", finalFullName);
                    child.put("childName", childName);
                    child.put("age", childAge);
                    child.put("gender", selectedGender);
                    child.put("avatar", selectedAvatar);
                    child.put("dob", selectedDateOfBirth);
                    child.put("createdAt", FieldValue.serverTimestamp());
                    child.put("activities", new ArrayList<>()); // Empty activities

                    db.collection("children").add(child)
                            .addOnSuccessListener(documentReference -> {
                                String childId = documentReference.getId();
                                db.collection("parents").document(userId)
                                        .update("children", FieldValue.arrayUnion(childId));

                                progressBar.setVisibility(View.GONE);
                                btnAddChild.setEnabled(true);
                                btnAddChild.setText("NEXT");
                                Toast.makeText(AddChildProfileActivity.this,
                                        "✅ Child profile created!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(AddChildProfileActivity.this, ChildSelectionActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}