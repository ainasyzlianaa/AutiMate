package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddChildProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView avatarCat, avatarDog, avatarRabbit, avatarBear;
    private CardView avatarPanda, avatarFox, avatarPenguin, avatarOwl;
    private CardView avatarUnicorn, avatarDragon, avatarAlien, avatarRobot;
    private CardView avatarStar, avatarHeart, avatarSmile, avatarSparkle;
    private EditText etChildName, etAge;
    private RadioGroup radioGender;
    private RadioButton rbMale, rbFemale;
    private TextView tvSelectedAvatar;
    private Button btnAddChild;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userEmail, userFullName, userUsername, userPassword;
    private String selectedAvatar = "🐱";
    private String selectedGender = "Male";

    // Array of all avatar cards for easy reset
    private CardView[] allAvatars;

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
        if (userEmail == null) {
            userEmail = "";
            userFullName = "";
            userUsername = "";
            userPassword = "";
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
        radioGender = findViewById(R.id.radioGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        tvSelectedAvatar = findViewById(R.id.tvSelectedAvatar);
        btnAddChild = findViewById(R.id.btnAddChild);
        progressBar = findViewById(R.id.progressBar);

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

        btnAddChild.setOnClickListener(v -> createAccountAndAddChild());
    }

    private void setupAvatarClick(CardView avatar, String emoji, String name) {
        avatar.setOnClickListener(v -> {
            selectedAvatar = emoji;
            tvSelectedAvatar.setText("Selected: " + emoji + " " + name);

            // Reset all avatar backgrounds
            for (CardView av : allAvatars) {
                av.setCardBackgroundColor(getColor(R.color.white));
            }
            avatar.setCardBackgroundColor(getColor(R.color.soft_blue));
        });
    }

    private void createAccountAndAddChild() {
        String childName = etChildName.getText().toString().trim();
        String childAge = etAge.getText().toString().trim();

        if (childName.isEmpty()) {
            etChildName.setError("Child name required");
            return;
        }
        if (childAge.isEmpty()) {
            etAge.setError("Age required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAddChild.setEnabled(false);

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveParentAndChildToFirestore(userId, childName, childAge);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnAddChild.setEnabled(true);
                        Toast.makeText(AddChildProfileActivity.this,
                                "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveParentAndChildToFirestore(String userId, String childName, String childAge) {
        // Save Parent
        Map<String, Object> parent = new HashMap<>();
        parent.put("userId", userId);
        parent.put("fullName", userFullName);
        parent.put("email", userEmail);
        parent.put("username", userUsername);
        parent.put("role", "parent");
        parent.put("createdAt", System.currentTimeMillis());

        db.collection("parents").document(userId)
                .set(parent)
                .addOnSuccessListener(aVoid -> {
                    // Save Child
                    Map<String, Object> child = new HashMap<>();
                    child.put("parentId", userId);
                    child.put("parentName", userFullName);
                    child.put("childName", childName);
                    child.put("age", childAge);
                    child.put("gender", selectedGender);
                    child.put("avatar", selectedAvatar);
                    child.put("createdAt", System.currentTimeMillis());

                    db.collection("children").add(child)
                            .addOnSuccessListener(documentReference -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddChildProfileActivity.this,
                                        "Account created successfully!", Toast.LENGTH_LONG).show();

                                // Save to SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putString("parentId", userId)
                                        .putString("parentName", userFullName)
                                        .apply();

                                // Go to Child Selection
                                Intent intent = new Intent(AddChildProfileActivity.this, ChildSelectionActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnAddChild.setEnabled(true);
                                Toast.makeText(AddChildProfileActivity.this,
                                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAddChild.setEnabled(true);
                    Toast.makeText(AddChildProfileActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}