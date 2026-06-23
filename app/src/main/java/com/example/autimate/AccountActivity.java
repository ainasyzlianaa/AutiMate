package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class AccountActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvTitle;
    private CardView cardMimi, cardAdam, cardSara;
    private Button btnConfirm;
    private String selectedChild = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        tvTitle = findViewById(R.id.tvTitle);
        cardMimi = findViewById(R.id.cardMimi);
        cardAdam = findViewById(R.id.cardAdam);
        cardSara = findViewById(R.id.cardSara);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvTitle.setText("Choose account:");

        cardMimi.setOnClickListener(v -> selectChild("MIMI", cardMimi));
        cardAdam.setOnClickListener(v -> selectChild("ADAM", cardAdam));
        cardSara.setOnClickListener(v -> selectChild("SARA", cardSara));

        btnConfirm.setOnClickListener(v -> {
            if (!selectedChild.isEmpty()) {
                Toast.makeText(this, "Selected: " + selectedChild, Toast.LENGTH_SHORT).show();
                // Save selected child to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("childName", selectedChild)
                        .putString("childId", selectedChild.toLowerCase())
                        .apply();
                finish();
            } else {
                Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectChild(String childName, CardView selectedCard) {
        selectedChild = childName;

        // Reset all cards
        cardMimi.setCardBackgroundColor(getColor(R.color.white));
        cardAdam.setCardBackgroundColor(getColor(R.color.white));
        cardSara.setCardBackgroundColor(getColor(R.color.white));

        // Highlight selected card
        selectedCard.setCardBackgroundColor(getColor(R.color.soft_blue));

        Toast.makeText(this, "Selected: " + childName, Toast.LENGTH_SHORT).show();
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvParentName = headerView.findViewById(R.id.tvParentName);
        if (tvParentName != null) {
            tvParentName.setText("Parent Account");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            // Already in account page
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ChildProfileActivity.class));
        } else if (id == R.id.nav_progress_tracker) {
            startActivity(new Intent(this, ProgressTrackerActivity.class));
        } else if (id == R.id.nav_add_activity) {
            startActivity(new Intent(this, AddNewActivityActivity.class));
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            Toast.makeText(this, "Theme customization coming soon!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
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