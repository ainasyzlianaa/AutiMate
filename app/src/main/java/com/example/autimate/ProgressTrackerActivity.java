package com.example.autimate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressTrackerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView tvChildName, tvTotalPoints, tvCompletedCount;
    private LinearLayout taskListContainer;
    private CardView cardCalendar;
    private Button btnBackToHome;

    private SharedPreferences progressPrefs;
    private SharedPreferences childPrefs;
    private SharedPreferences routinePrefs;
    private SharedPreferences rewardPrefs;
    private String childName;
    private List<String> activityList = new ArrayList<>();

    // Calendar navigation
    private int currentYear, currentMonth;
    private TextView tvYear, tvMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracker);

        progressPrefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);
        childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        routinePrefs = getSharedPreferences("RoutinePrefs", MODE_PRIVATE);
        rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        childName = childPrefs.getString("childName", "Child");

        loadActivities();

        // Initialize toolbar and drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize views
        tvChildName = findViewById(R.id.tvChildName);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        taskListContainer = findViewById(R.id.taskListContainer);
        cardCalendar = findViewById(R.id.cardCalendar);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        if (tvChildName != null) {
            tvChildName.setText(childName);
        }

        // Set current month/year
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        loadPoints();
        loadTodayProgress();
        setupCalendar();

        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProgressTrackerActivity.this, ChildHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }

        updateNavHeader();
    }

    private void loadActivities() {
        String savedActivities = routinePrefs.getString("activities", "");
        activityList.clear();

        if (!savedActivities.isEmpty()) {
            String[] activities = savedActivities.split(",");
            for (String act : activities) {
                activityList.add(act.trim());
            }
        }

        if (activityList.isEmpty()) {
            activityList.add("Brush Teeth");
            activityList.add("Eat Foods");
            activityList.add("Wash Hands");
            activityList.add("Sleep");
            activityList.add("Pack School Bag");
        }
    }

    private void loadPoints() {
        if (tvTotalPoints == null) return;
        int totalPoints = rewardPrefs.getInt("totalPoints", 0);
        tvTotalPoints.setText(String.valueOf(totalPoints));
    }

    private void loadTodayProgress() {
        if (tvCompletedCount == null) return;

        int totalActivities = activityList.size();
        int completedRoutines = progressPrefs.getInt("completedRoutines", 0);
        int completedCount = completedRoutines % totalActivities;

        tvCompletedCount.setText(completedCount + " / " + totalActivities);

        if (taskListContainer != null) {
            taskListContainer.removeAllViews();

            for (int i = 0; i < activityList.size(); i++) {
                View taskView = getLayoutInflater().inflate(R.layout.item_progress_task, taskListContainer, false);

                TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);
                TextView tvStatus = taskView.findViewById(R.id.tvStatus);

                tvTaskName.setText(activityList.get(i));

                if (i < completedCount) {
                    tvStatus.setText("✓ Completed");
                    tvStatus.setTextColor(getColor(R.color.soft_blue));
                } else {
                    tvStatus.setText("○ Not Started");
                    tvStatus.setTextColor(getColor(R.color.light_brown));
                }

                taskListContainer.addView(taskView);
            }
        }
    }

    private void setupCalendar() {
        if (cardCalendar == null) return;

        // Clear existing views
        cardCalendar.removeAllViews();

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(28, 28, 28, 28);
        // Year Text
        tvYear = new TextView(this);
        tvYear.setText(String.valueOf(currentYear));
        tvYear.setTextSize(16);
        tvYear.setTextColor(getColor(R.color.light_brown));
        tvYear.setGravity(Gravity.CENTER);
        tvYear.setPadding(0, 0, 0, 4);
        mainContainer.addView(tvYear);

        // Month Header with navigation
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Previous button
        TextView btnPrev = new TextView(this);
        btnPrev.setText("‹");
        btnPrev.setTextSize(28);
        btnPrev.setTextColor(getColor(R.color.soft_blue));
        btnPrev.setPadding(24, 4, 24, 4);
        btnPrev.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            setupCalendar();
        });

        // Month text
        tvMonth = new TextView(this);
        tvMonth.setText(getMonthString(currentMonth));
        tvMonth.setTextSize(26);
        tvMonth.setTextColor(getColor(R.color.text_dark));
        tvMonth.setGravity(Gravity.CENTER);
        tvMonth.setTypeface(null, android.graphics.Typeface.BOLD);
        tvMonth.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Next button
        TextView btnNext = new TextView(this);
        btnNext.setText("›");
        btnNext.setTextSize(34);
        btnNext.setTextColor(getColor(R.color.soft_blue));
        btnNext.setPadding(24, 4, 24, 4);
        btnNext.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            setupCalendar();
        });

        headerLayout.addView(btnPrev);
        headerLayout.addView(tvMonth);
        headerLayout.addView(btnNext);
        mainContainer.addView(headerLayout);

        // Day headers
        String[] dayHeaders = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setPadding(0, 12, 0, 8);

        for (String day : dayHeaders) {
            TextView dayHeader = new TextView(this);
            dayHeader.setText(day);
            dayHeader.setTextSize(11);
            dayHeader.setTextColor(getColor(R.color.light_brown));
            dayHeader.setGravity(Gravity.CENTER);
            dayHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 40, 1);
            dayHeader.setLayoutParams(params);
            headerRow.addView(dayHeader);
        }
        mainContainer.addView(headerRow);

        // Divider line
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(getColor(R.color.divider));
        divider.setPadding(0, 0, 0, 8);
        mainContainer.addView(divider);

        // Calendar grid
        LinearLayout calendarGridContainer = new LinearLayout(this);
        calendarGridContainer.setOrientation(LinearLayout.VERTICAL);
        calendarGridContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Get completed dates
        Map<String, Integer> dateStatusMap = getDateStatusMap();

        // Calculate days
        Calendar firstDay = Calendar.getInstance();
        firstDay.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get today for comparison
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        int day = 1;
        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                if (row == 0 && col < firstDayOfWeek) {
                    // Empty cell before first day
                    TextView emptyCell = new TextView(this);
                    emptyCell.setLayoutParams(new LinearLayout.LayoutParams(0, 70, 1));                    emptyCell.setBackground(null);
                    rowLayout.addView(emptyCell);
                } else if (day <= daysInMonth) {
                    TextView dayCell = new TextView(this);
                    dayCell.setText(String.valueOf(day));
                    dayCell.setGravity(Gravity.CENTER);
                    dayCell.setTextSize(14);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 70, 1);
                    params.setMargins(4, 4, 4, 4);
                    dayCell.setLayoutParams(params);

                    // Set background based on status
                    String dateStr = currentYear + "-" + (currentMonth + 1) + "-" + day;
                    boolean isFuture = (currentYear > todayYear) ||
                            (currentYear == todayYear && currentMonth > todayMonth) ||
                            (currentYear == todayYear && currentMonth == todayMonth && day > todayDay);

                    if (isFuture) {
                        // Future date - no circle
                        dayCell.setBackground(null);
                        dayCell.setTextColor(getColor(R.color.light_brown));
                        dayCell.setAlpha(0.4f);
                        dayCell.setOnClickListener(null);
                    } else {
                        int status = dateStatusMap.containsKey(dateStr) ? dateStatusMap.get(dateStr) : 0;

                        if (status == 2) {
                            // All completed - Blue circle
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(ContextCompat.getColor(this, R.color.soft_blue));
                            bg.setSize(38, 38);
                            dayCell.setBackground(bg);
                            dayCell.setTextColor(Color.WHITE);
                            dayCell.setAlpha(1.0f);
                        } else if (status == 1) {
                            // Partial completed - Orange/Yellow circle
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(ContextCompat.getColor(this, R.color.khaki));
                            bg.setSize(38, 38);
                            dayCell.setBackground(bg);
                            dayCell.setTextColor(Color.WHITE);
                            dayCell.setAlpha(1.0f);
                        } else {
                            // No completion - Light grey circle
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(ContextCompat.getColor(this, R.color.divider));
                            bg.setSize(38, 38);
                            dayCell.setBackground(bg);
                            dayCell.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                            dayCell.setAlpha(0.5f);
                        }
                    }

                    // Make clickable only for past and today
                    final String finalDateStr = dateStr;
                    final int finalDay = day;
                    if (!isFuture) {
                        dayCell.setOnClickListener(v -> {
                            showDayDetailDialog(finalDateStr, finalDay, currentMonth + 1, currentYear);
                        });
                    }

                    rowLayout.addView(dayCell);
                    day++;
                } else {
                    // Empty cell after last day
                    TextView emptyCell = new TextView(this);
                    emptyCell.setLayoutParams(new LinearLayout.LayoutParams(0, 44, 1));
                    emptyCell.setBackground(null);
                    rowLayout.addView(emptyCell);
                }
            }
            calendarGridContainer.addView(rowLayout);
        }

        mainContainer.addView(calendarGridContainer);
        cardCalendar.addView(mainContainer);
    }

    private Map<String, Integer> getDateStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        String completedDates = rewardPrefs.getString("completedDates", "");
        int totalActivities = activityList.size();

        if (!completedDates.isEmpty()) {
            String[] dates = completedDates.split(",");
            for (String date : dates) {
                if (date != null && !date.isEmpty()) {
                    int count = statusMap.containsKey(date) ? statusMap.get(date) : 0;
                    statusMap.put(date, count + 1);
                }
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            int count = entry.getValue();
            if (count >= totalActivities) {
                resultMap.put(entry.getKey(), 2);
            } else if (count > 0) {
                resultMap.put(entry.getKey(), 1);
            } else {
                resultMap.put(entry.getKey(), 0);
            }
        }

        return resultMap;
    }

    private void showDayDetailDialog(String dateStr, int day, int month, int year) {
        String completedDates = rewardPrefs.getString("completedDates", "");
        String[] dates = completedDates.isEmpty() ? new String[0] : completedDates.split(",");

        int completedCount = 0;
        for (String date : dates) {
            if (date != null && date.equals(dateStr)) {
                completedCount++;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_day_detail, null);

        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
        LinearLayout taskDetailContainer = dialogView.findViewById(R.id.taskDetailContainer);

        String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(
                new Date(year - 1900, month - 1, 1));
        tvDate.setText(monthName + " " + day + ", " + year);

        int totalActivities = activityList.size();

        if (completedCount >= totalActivities) {
            tvStatus.setText("✅ All activities completed!");
            tvStatus.setTextColor(getColor(R.color.soft_blue));
        } else if (completedCount > 0) {
            tvStatus.setText("⏳ " + completedCount + "/" + totalActivities + " activities completed");
            tvStatus.setTextColor(getColor(R.color.khaki));
        } else {
            tvStatus.setText("❌ No activities completed");
            tvStatus.setTextColor(getColor(R.color.light_brown));
        }

        taskDetailContainer.removeAllViews();
        for (int i = 0; i < activityList.size(); i++) {
            View taskView = getLayoutInflater().inflate(R.layout.item_progress_task, taskDetailContainer, false);
            TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);
            TextView tvStatusText = taskView.findViewById(R.id.tvStatus);

            tvTaskName.setText(activityList.get(i));

            if (i < completedCount) {
                tvStatusText.setText("✓ Completed");
                tvStatusText.setTextColor(getColor(R.color.soft_blue));
            } else {
                tvStatusText.setText("○ Not Started");
                tvStatusText.setTextColor(getColor(R.color.light_brown));
            }

            taskDetailContainer.addView(taskView);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private String getMonthString(int month) {
        String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        return months[month];
    }

    private void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        TextView tvParentName = headerView.findViewById(R.id.tvParentName);

        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        String parentName = prefs.getString("parentName", "Parent");

        if (tvParentName != null) tvParentName.setText(parentName);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            startActivity(new Intent(this, AccountActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ChildProfileActivity.class));
        } else if (id == R.id.nav_progress_tracker) {
            // Already here
        } else if (id == R.id.nav_add_activity) {
            startActivity(new Intent(this, AddNewActivityActivity.class));
        } else if (id == R.id.nav_view_rewards) {
            startActivity(new Intent(this, RewardActivity.class));
        } else if (id == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeCustomizationActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("YES", (dialog, which) -> {
                    Intent intent = new Intent(ProgressTrackerActivity.this, GoodbyeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("CANCEL", null)
                .show();
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
        loadActivities();
        loadPoints();
        loadTodayProgress();
        setupCalendar();
    }
}