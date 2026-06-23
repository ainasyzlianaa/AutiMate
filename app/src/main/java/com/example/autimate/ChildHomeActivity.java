package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class ChildHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView childTitle, childSubtitle, progressStars, tvStreak, tvProgressText;
    private CardView cardRoutine, cardGame, cardRewards;
    private VideoView mascotVideo;
    private String childName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        // Get child name from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        childName = prefs.getString("childName", "Friend");

        // Initialize toolbar and drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        // Setup hamburger menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Update header with child name
        updateNavHeader();

        // Initialize views
        childTitle = findViewById(R.id.childTitle);
        childSubtitle = findViewById(R.id.childSubtitle);
        progressStars = findViewById(R.id.progressStars);
        tvStreak = findViewById(R.id.tvStreak);
        tvProgressText = findViewById(R.id.tvProgressText);
        cardRoutine = findViewById(R.id.cardRoutine);
        cardGame = findViewById(R.id.cardGame);
        cardRewards = findViewById(R.id.cardRewards);
        mascotVideo = findViewById(R.id.mascotVideo);

        // Set greeting based on time of day
        String greeting = getGreeting();
        if (childTitle != null) {
            childTitle.setText(greeting + ", " + childName + "!");
        }
        if (childSubtitle != null) {
            childSubtitle.setText("Let's have a great day!");
        }

        // Load and play mascot video
        loadMascotVideo();

        updateProgress();
        updateStreak();

        // ROUTINE button - Navigate to Routine Time
        if (cardRoutine != null) {
            cardRoutine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChildHomeActivity.this, RoutineTimeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // GAME button - Navigate to Game Selection Page
        if (cardGame != null) {
            cardGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChildHomeActivity.this, GameSelectionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // REWARDS button - Navigate to Reward Page
        if (cardRewards != null) {
            cardRewards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChildHomeActivity.this, RewardActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    private void loadMascotVideo() {
        try {
            // Try to load hello.mp4 from raw folder
            int rawResourceId = getResources().getIdentifier("hello", "raw", getPackageName());

            if (rawResourceId != 0) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                mascotVideo.setVideoURI(videoUri);
                mascotVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true); // Loop the video
                    mascotVideo.start();
                });

                mascotVideo.setOnErrorListener((mp, what, extra) -> {
                    // If video fails, hide the VideoView or show a placeholder
                    mascotVideo.setVisibility(View.GONE);
                    return true;
                });
            } else {
                // If hello.mp4 doesn't exist in raw folder, hide the VideoView
                mascotVideo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mascotVideo.setVisibility(View.GONE);
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvChildName = headerView.findViewById(R.id.tvChildName);
        if (tvChildName != null) {
            tvChildName.setText(childName);
        }
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good morning";
        } else if (hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    private void updateProgress() {
        if (progressStars != null) {
            SharedPreferences progressPrefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);
            int completedRoutines = progressPrefs.getInt("completedRoutines", 0);
            int totalRoutines = 5;
            int percent = (completedRoutines * 100) / totalRoutines;

            if (percent >= 80) {
                progressStars.setText("★★★★★");
            } else if (percent >= 60) {
                progressStars.setText("★★★★☆");
            } else if (percent >= 40) {
                progressStars.setText("★★★☆☆");
            } else if (percent >= 20) {
                progressStars.setText("★★☆☆☆");
            } else {
                progressStars.setText("★☆☆☆☆");
            }

            if (tvProgressText != null) {
                tvProgressText.setText(completedRoutines + " / " + totalRoutines + " Activities");
            }
        }
    }

    private void updateStreak() {
        if (tvStreak != null) {
            SharedPreferences prefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);
            int streak = prefs.getInt("streak", 0);
            if (streak == 0) {
                tvStreak.setText("Start today! 🔥");
            } else {
                tvStreak.setText("🔥 " + streak);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            Intent intent = new Intent(ChildHomeActivity.this, AccountActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(ChildHomeActivity.this, ChildProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_progress_tracker) {
            Intent intent = new Intent(ChildHomeActivity.this, ProgressTrackerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_add_activity) {
            Intent intent = new Intent(ChildHomeActivity.this, AddNewActivityActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_view_rewards) {
            Intent intent = new Intent(ChildHomeActivity.this, RewardActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_theme) {
            // FIXED: Navigate to ThemeCustomizationActivity
            Intent intent = new Intent(ChildHomeActivity.this, ThemeCustomizationActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (id == R.id.nav_logout) {
            logout();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            // Navigate to GoodbyeActivity
            Intent intent = new Intent(ChildHomeActivity.this, GoodbyeActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgress();
        updateStreak();

        // Resume video if it was playing
        if (mascotVideo != null && !mascotVideo.isPlaying()) {
            mascotVideo.start();
        }

        // Update greeting
        String greeting = getGreeting();
        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String updatedName = prefs.getString("childName", "Friend");
        if (childTitle != null) {
            childTitle.setText(greeting + ", " + updatedName + "!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity goes to background
        if (mascotVideo != null && mascotVideo.isPlaying()) {
            mascotVideo.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release video resources
        if (mascotVideo != null) {
            mascotVideo.stopPlayback();
        }
    }
}