package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private CardView btnChangePhoto, avatarCat, avatarDog, avatarRabbit, avatarBear, avatarPanda,
            avatarFox, avatarPenguin, avatarOwl, avatarUnicorn, avatarDragon,
            avatarAlien, avatarRobot, avatarStar, avatarHeart, avatarSmile,
            avatarSparkle;
    private ImageView profileImage;
    private TextView profileIcon, tvName, tvGender, tvAge, tvDob;
    private LinearLayout viewMode, editMode, editButtons;
    private Button btnEditProfile, btnCancel, btnSave, btnLogout;
    private EditText etName, etAge;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private List<String> dayList, monthList, yearList;
    private RadioGroup radioGender;
    private RadioButton rbBoy, rbGirl;

    private FirebaseFirestore db;
    private String childId;
    private String childName;
    private String selectedAvatar = "👧";

    // Allowed emojis list
    private final String[] allowedEmojis = {
            "🐱", "🐶", "🐰", "🐻", "🐼", "🦊", "🐧", "🦉",
            "🦄", "🐉", "👽", "🤖", "⭐", "❤️", "😊", "✨"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        childId = prefs.getString("childId", "");
        childName = prefs.getString("childName", "");

        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "No child selected. Please select a child first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ChildSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        // Setup drawer first
        setupDrawer();

        initViews();
        loadChildData();
        setupAvatarClickListeners();
        setupSpinners();

        btnChangePhoto.setOnClickListener(v -> showEmojiPickerDialog());
        btnEditProfile.setOnClickListener(v -> enableEditMode());
        btnCancel.setOnClickListener(v -> disableEditMode());
        btnSave.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the home button (hamburger icon)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        // Create the toggle with the drawer layout and toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        );

        // Set the toggle as the drawer listener
        drawerLayout.addDrawerListener(toggle);

        // Sync the state to update the hamburger icon
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();
    }

    private void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        TextView tvParentName = headerView.findViewById(R.id.tvParentName);
        TextView tvParentEmail = headerView.findViewById(R.id.tvParentEmail);

        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentName = prefs.getString("parentName", "Parent");
        String parentEmail = prefs.getString("parentEmail", "");

        if (tvParentName != null) tvParentName.setText(parentName);
        if (tvParentEmail != null) tvParentEmail.setText(parentEmail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            startActivity(new Intent(this, AccountActivity.class));
        } else if (id == R.id.nav_profile) {
            // Already here
        } else if (id == R.id.nav_progress_tracker) {
            startActivity(new Intent(this, ProgressTrackerActivity.class));
        } else if (id == R.id.nav_add_activity) {
            startActivity(new Intent(this, AddNewActivityActivity.class));
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeCustomizationActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(ChildProfileActivity.this, GoodbyeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void initViews() {
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
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        radioGender = findViewById(R.id.radioGender);
        rbBoy = findViewById(R.id.rbBoy);
        rbGirl = findViewById(R.id.rbGirl);

        // Initialize all avatar cards
        avatarCat = findViewById(R.id.avatarCat);
        avatarDog = findViewById(R.id.avatarDog);
        avatarRabbit = findViewById(R.id.avatarRabbit);
        avatarBear = findViewById(R.id.avatarBear);
        avatarPanda = findViewById(R.id.avatarPanda);
        avatarFox = findViewById(R.id.avatarFox);
        avatarPenguin = findViewById(R.id.avatarPenguin);
        avatarOwl = findViewById(R.id.avatarOwl);
        avatarUnicorn = findViewById(R.id.avatarUnicorn);
        avatarDragon = findViewById(R.id.avatarDragon);
        avatarAlien = findViewById(R.id.avatarAlien);
        avatarRobot = findViewById(R.id.avatarRobot);
        avatarStar = findViewById(R.id.avatarStar);
        avatarHeart = findViewById(R.id.avatarHeart);
        avatarSmile = findViewById(R.id.avatarSmile);
        avatarSparkle = findViewById(R.id.avatarSparkle);
    }

    private void setupSpinners() {
        dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.format("%02d", d));
        }

        monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.format("%02d", m));
        }

        yearList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1900; y--) {
            yearList.add(Integer.toString(y));
        }

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dayList);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, monthList);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearList);

        spinnerDay.setAdapter(dayAdapter);
        spinnerMonth.setAdapter(monthAdapter);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupAvatarClickListeners() {
        View.OnClickListener avatarClickListener = v -> {
            CardView card = (CardView) v;
            TextView emojiView = (TextView) card.getChildAt(0);
            String emoji = emojiView.getText().toString();
            selectAvatar(emoji);
        };

        avatarCat.setOnClickListener(avatarClickListener);
        avatarDog.setOnClickListener(avatarClickListener);
        avatarRabbit.setOnClickListener(avatarClickListener);
        avatarBear.setOnClickListener(avatarClickListener);
        avatarPanda.setOnClickListener(avatarClickListener);
        avatarFox.setOnClickListener(avatarClickListener);
        avatarPenguin.setOnClickListener(avatarClickListener);
        avatarOwl.setOnClickListener(avatarClickListener);
        avatarUnicorn.setOnClickListener(avatarClickListener);
        avatarDragon.setOnClickListener(avatarClickListener);
        avatarAlien.setOnClickListener(avatarClickListener);
        avatarRobot.setOnClickListener(avatarClickListener);
        avatarStar.setOnClickListener(avatarClickListener);
        avatarHeart.setOnClickListener(avatarClickListener);
        avatarSmile.setOnClickListener(avatarClickListener);
        avatarSparkle.setOnClickListener(avatarClickListener);
    }

    private void selectAvatar(String emoji) {
        // Check if emoji is allowed
        boolean isAllowed = false;
        for (String allowed : allowedEmojis) {
            if (allowed.equals(emoji)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            Toast.makeText(this, "Please select an avatar from the options above.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedAvatar = emoji;
        profileIcon.setText(emoji);
        profileImage.setVisibility(View.GONE);
        profileIcon.setVisibility(View.VISIBLE);
        highlightSelectedAvatar(emoji);
    }

    private void highlightSelectedAvatar(String emoji) {
        resetAvatarHighlight();

        CardView selectedCard = findAvatarCard(emoji);
        if (selectedCard != null) {
            selectedCard.setCardBackgroundColor(getColor(R.color.soft_blue));
            selectedCard.setElevation(8f);
        }
    }

    private CardView findAvatarCard(String emoji) {
        CardView[] cards = {
                avatarCat, avatarDog, avatarRabbit, avatarBear, avatarPanda,
                avatarFox, avatarPenguin, avatarOwl, avatarUnicorn, avatarDragon,
                avatarAlien, avatarRobot, avatarStar, avatarHeart, avatarSmile,
                avatarSparkle
        };

        for (CardView card : cards) {
            TextView emojiView = (TextView) card.getChildAt(0);
            if (emojiView.getText().toString().equals(emoji)) {
                return card;
            }
        }
        return null;
    }

    private void resetAvatarHighlight() {
        CardView[] cards = {
                avatarCat, avatarDog, avatarRabbit, avatarBear, avatarPanda,
                avatarFox, avatarPenguin, avatarOwl, avatarUnicorn, avatarDragon,
                avatarAlien, avatarRobot, avatarStar, avatarHeart, avatarSmile,
                avatarSparkle
        };

        for (CardView card : cards) {
            card.setCardBackgroundColor(getColor(R.color.white));
            card.setElevation(3f);
        }
    }

    private void loadChildData() {
        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String localAvatar = prefs.getString("childAvatar", "");
        if (localAvatar != null && !localAvatar.isEmpty()) {
            setAvatar(localAvatar);
        }

        if (childId != null && !childId.isEmpty()) {
            db.collection("children").document(childId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("childName") != null ? doc.getString("childName") : childName;
                            tvName.setText(name);

                            String gender = doc.getString("gender");
                            tvGender.setText(gender != null ? gender : "Not set");

                            String age = doc.getString("age");
                            tvAge.setText(age != null && !age.isEmpty() ? age + " years old" : "Not set");

                            String dob = doc.getString("dob");
                            tvDob.setText(dob != null ? dob : "Not set");

                            String profileIconStr = doc.getString("profileIcon");
                            if (profileIconStr != null && !profileIconStr.isEmpty()) {
                                SharedPreferences.Editor editor = getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit();
                                editor.putString("childAvatar", profileIconStr);
                                editor.apply();
                                setAvatar(profileIconStr);
                                selectedAvatar = profileIconStr;
                            }
                        } else {
                            setDefaultValues();
                        }
                    })
                    .addOnFailureListener(e -> {
                        setDefaultValues();
                        Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setAvatar(String avatarStr) {
        profileImage.setVisibility(View.GONE);
        profileIcon.setVisibility(View.VISIBLE);
        profileIcon.setText(avatarStr);
        selectedAvatar = avatarStr;
    }

    private void setDefaultValues() {
        tvName.setText(childName);
        tvGender.setText("Not set");
        tvAge.setText("Not set");
        tvDob.setText("Not set");
    }

    private void showEmojiPickerDialog() {
        if (editMode.getVisibility() == View.VISIBLE) {
            editMode.requestFocus();
        } else {
            enableEditMode();
        }
    }

    private void enableEditMode() {
        etName.setText(tvName.getText().toString());
        String ageText = tvAge.getText().toString();
        if (!ageText.equals("Not set")) {
            etAge.setText(ageText.replace(" years old", ""));
        } else {
            etAge.setText("");
        }

        // Set dob spinners based on current dob display if available
        String dobText = tvDob.getText().toString();
        if (!dobText.equals("Not set") && dobText.contains("/")) {
            try {
                String[] parts = dobText.split("/");
                if (parts.length == 3) {
                    String m = parts[0];
                    String d = parts[1];
                    String y = parts[2];
                    int midx = monthList.indexOf(m);
                    if (midx >= 0) spinnerMonth.setSelection(midx);
                    int didx = dayList.indexOf(d.length() == 1 ? String.format("%02d", Integer.parseInt(d)) : d);
                    if (didx >= 0) spinnerDay.setSelection(didx);
                    int yidx = yearList.indexOf(y);
                    if (yidx >= 0) spinnerYear.setSelection(yidx);
                }
            } catch (Exception ignored) {
            }
        }

        String gender = tvGender.getText().toString();
        if (gender.equals("Boy")) {
            rbBoy.setChecked(true);
        } else if (gender.equals("Girl")) {
            rbGirl.setChecked(true);
        }

        // Highlight current avatar
        resetAvatarHighlight();
        highlightSelectedAvatar(selectedAvatar);

        viewMode.setVisibility(View.GONE);
        editMode.setVisibility(View.VISIBLE);
        editButtons.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
    }

    private void disableEditMode() {
        viewMode.setVisibility(View.VISIBLE);
        editMode.setVisibility(View.GONE);
        editButtons.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);
        loadChildData();
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        // Validate age
        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age < 5 || age > 12) {
                etAge.setError("Age must be between 5 and 12");
                etAge.requestFocus();
                Toast.makeText(this, "❌ Age must be between 5 and 12", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid number");
            etAge.requestFocus();
            return;
        }

        String dobTemp = "";
        try {
            String d = dayList.get(spinnerDay.getSelectedItemPosition());
            String m = monthList.get(spinnerMonth.getSelectedItemPosition());
            String y = yearList.get(spinnerYear.getSelectedItemPosition());
            dobTemp = m + "/" + d + "/" + y;
        } catch (Exception ignored) {
        }
        final String dob = dobTemp;
        String gender = rbBoy.isChecked() ? "Boy" : "Girl";

        if (name.isEmpty()) {
            etName.setError("Name required");
            etName.requestFocus();
            return;
        }

        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Error: No child selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate avatar is from allowed list
        boolean isValidAvatar = false;
        for (String allowed : allowedEmojis) {
            if (allowed.equals(selectedAvatar)) {
                isValidAvatar = true;
                break;
            }
        }

        if (!isValidAvatar) {
            Toast.makeText(this, "Please select a valid avatar from the options", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("childName", name);
        updates.put("gender", gender);
        updates.put("age", ageStr);
        updates.put("dob", dob);
        updates.put("profileIcon", selectedAvatar);

        btnSave.setEnabled(false);
        btnSave.setText("SAVING...");

        db.collection("children").document(childId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    tvName.setText(name);
                    tvGender.setText(gender);
                    tvAge.setText(ageStr + " years old");
                    tvDob.setText(dob);

                    SharedPreferences.Editor editor = getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit();
                    editor.putString("childName", name);
                    editor.putString("childAvatar", selectedAvatar);
                    editor.apply();

                    broadcastProfileUpdate(name, selectedAvatar);

                    Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    disableEditMode();
                    btnSave.setEnabled(true);
                    btnSave.setText("SAVE");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("SAVE");
                });
    }

    private void broadcastProfileUpdate(String childName, String avatarValue) {
        Intent intent = new Intent("PROFILE_UPDATED");
        intent.putExtra("childName", childName);
        intent.putExtra("childAvatar", avatarValue);
        sendBroadcast(intent);

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("childName", childName)
                .putString("childAvatar", avatarValue)
                .apply();
    }

    private void showLogoutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        // Set positive button (YES)
        builder.setPositiveButton("YES", (dialog, which) -> {
            getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(ChildProfileActivity.this, GoodbyeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Set negative button (CANCEL)
        builder.setNegativeButton("CANCEL", null);

        // Create and show dialog
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Get the buttons
        Button positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        // Check current theme mode
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDarkMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        if (positiveButton != null) {
            if (isDarkMode) {
                // Dark mode: use soft blue for YES
                positiveButton.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));
            } else {
                // Light mode: use black for YES
                positiveButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        }

        if (negativeButton != null) {
            if (isDarkMode) {
                // Dark mode: use white for CANCEL
                negativeButton.setTextColor(ContextCompat.getColor(this, R.color.dark_text_primary));
            } else {
                // Light mode: use black for CANCEL
                negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}