package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ChildProfileActivity extends AppCompatActivity {

    private CardView btnChangePhoto;
    private ImageView profileImage;
    private TextView profileIcon, tvName, tvGender, tvAge, tvDob;
    private LinearLayout viewMode, editMode, editButtons;
    private Button btnEditProfile, btnCancel, btnSave, btnLogout;
    private EditText etName, etAge, etDob;
    private RadioGroup radioGender;
    private RadioButton rbBoy, rbGirl;

    private FirebaseFirestore db;
    private String childId;
    private String childName;
    private Uri selectedImageUri;
    private boolean useImage = false;
    private String[] avatars = {"👧", "👦", "👶", "🧒", "👧🏽", "👦🏽", "🐱", "🐶", "🦊", "🐼"};

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    useImage = true;
                    profileImage.setVisibility(View.VISIBLE);
                    profileIcon.setVisibility(View.GONE);
                    profileImage.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        childId = prefs.getString("childId", "");
        childName = prefs.getString("childName", "");

        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        profileImage = findViewById(R.id.profileImage);
        profileIcon = findViewById(R.id.profileIcon);
        tvName = findViewById(R.id.tvName);
        tvGender = findViewById(R.id.tvGender);
        tvAge = findViewById(R.id.tvAge);
        tvDob = findViewById(R.id.tvDob);
        viewMode = findViewById(R.id.viewMode);
        editMode = findViewById(R.id.editMode);
        editButtons = findViewById(R.id.editButtons);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etDob = findViewById(R.id.etDob);
        radioGender = findViewById(R.id.radioGender);
        rbBoy = findViewById(R.id.rbBoy);
        rbGirl = findViewById(R.id.rbGirl);

        loadChildData();

        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());
        btnEditProfile.setOnClickListener(v -> enableEditMode());
        btnCancel.setOnClickListener(v -> disableEditMode());
        btnSave.setOnClickListener(v -> saveProfile());
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void loadChildData() {
        db.collection("children").document(childId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvName.setText(doc.getString("childName") != null ? doc.getString("childName") : childName);
                tvGender.setText(doc.getString("gender") != null ? doc.getString("gender") : "Not set");
                tvAge.setText(doc.getString("age") != null ? doc.getString("age") + " years old" : "Not set");
                tvDob.setText(doc.getString("dob") != null ? doc.getString("dob") : "Not set");
            }
        });
    }

    private void showImagePickerDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Change profile picture");
        String[] options = {"Choose from Gallery", "Use Emoji Avatar"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            } else {
                showEmojiPickerDialog();
            }
        });
        builder.show();
    }

    private void showEmojiPickerDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose an Emoji Avatar");
        builder.setItems(avatars, (dialog, which) -> {
            useImage = false;
            profileImage.setVisibility(View.GONE);
            profileIcon.setVisibility(View.VISIBLE);
            profileIcon.setText(avatars[which]);
        });
        builder.show();
    }

    private void enableEditMode() {
        etName.setText(tvName.getText().toString());
        String ageText = tvAge.getText().toString();
        if (!ageText.equals("Not set")) etAge.setText(ageText.replace(" years old", ""));
        etDob.setText(tvDob.getText().toString());
        if (tvGender.getText().toString().equals("Boy")) rbBoy.setChecked(true);
        else if (tvGender.getText().toString().equals("Girl")) rbGirl.setChecked(true);

        viewMode.setVisibility(View.GONE);
        editMode.setVisibility(View.VISIBLE);
        editButtons.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.GONE);
        if (btnLogout != null) btnLogout.setVisibility(View.GONE);
    }

    private void disableEditMode() {
        viewMode.setVisibility(View.VISIBLE);
        editMode.setVisibility(View.GONE);
        editButtons.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
        if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = rbBoy.isChecked() ? "Boy" : "Girl";

        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("childName", name);
        updates.put("gender", gender);
        updates.put("age", age);
        updates.put("dob", dob);
        if (useImage && selectedImageUri != null) updates.put("profileIcon", selectedImageUri.toString());
        else if (!useImage) updates.put("profileIcon", profileIcon.getText().toString());

        db.collection("children").document(childId).update(updates).addOnSuccessListener(aVoid -> {
            tvName.setText(name);
            tvGender.setText(gender);
            tvAge.setText(age + " years old");
            tvDob.setText(dob);
            getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit().putString("childName", name).apply();
            Toast.makeText(this, "Profile updated! ✨", Toast.LENGTH_SHORT).show();
            disableEditMode();
        });
    }

    private void showLogoutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("LOGOUT", (dialog, which) -> {
            getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(ChildProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
}