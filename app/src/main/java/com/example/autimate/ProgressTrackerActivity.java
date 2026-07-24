package com.example.autimate;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProgressTrackerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_PARTIAL = 1;
    private static final int STATUS_NONE = 0;
    private static final int STATUS_MISSED = -1;
    private static final int STATUS_FUTURE = -2;

    private static final int CIRCLE_SIZE_DP = 36;
    private static final int ROW_HEIGHT_DP = 48;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView tvChildName, tvTotalPoints, tvCompletedCount, tvMonthPicker;
    private TextView tvAvatar;
    private ImageView ivAvatar;
    private LinearLayout taskListContainer, calendarContainer;
    private LinearLayout weeklyChartContainer, chartSummaryContainer;
    private Button btnBackToHome;

    private SharedPreferences childPrefs;
    private SharedPreferences routinePrefs;
    private SharedPreferences rewardPrefs;
    private SharedPreferences progressPrefs;
    private String childName;
    private String childId;
    private List<String> activityList = new ArrayList<>();

    private int currentYear;
    private int currentMonth;

    private final SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat legacyDateFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());

    private BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("PROFILE_UPDATED".equals(intent.getAction())) {
                String childName = intent.getStringExtra("childName");
                String childAvatar = intent.getStringExtra("childAvatar");

                if (childName != null && !childName.isEmpty()) {
                    tvChildName.setText(childName);
                    SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                    prefs.edit().putString("childName", childName).apply();
                }

                if (childAvatar != null && !childAvatar.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                    prefs.edit().putString("childAvatar", childAvatar).apply();
                    updateProfileAvatar(childAvatar);
                } else {
                    SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                    String savedAvatar = prefs.getString("childAvatar", "👧");
                    updateProfileAvatar(savedAvatar);
                }

                updateNavHeader();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_tracker);

        childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        routinePrefs = getSharedPreferences("RoutinePrefs", MODE_PRIVATE);
        rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        progressPrefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);

        childId = childPrefs.getString("childId", "default");
        childName = childPrefs.getString("childName", "Child");

        loadActivities();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        tvAvatar = findViewById(R.id.tvAvatar);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvChildName = findViewById(R.id.tvChildName);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        taskListContainer = findViewById(R.id.taskListContainer);
        calendarContainer = findViewById(R.id.calendarContainer);
        weeklyChartContainer = findViewById(R.id.weeklyChartContainer);
        chartSummaryContainer = findViewById(R.id.chartSummaryContainer);
        tvMonthPicker = findViewById(R.id.tvMonthPicker);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        tvChildName.setText(childName);

        String avatar = childPrefs.getString("childAvatar", "👧");
        updateProfileAvatar(avatar);

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        tvMonthPicker.setOnClickListener(v -> showMonthYearPicker());

        loadPoints();
        loadTodayProgress();
        loadWeeklyChart();
        updateMonthPickerLabel();
        setupCalendar();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressTrackerActivity.this, ChildHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        updateNavHeader();
    }

    private void updateProfileAvatar(String avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.startsWith("content://") || avatar.startsWith("file://") ||
                    avatar.startsWith("http://") || avatar.startsWith("https://")) {
                try {
                    Glide.with(this)
                            .load(avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.circle_bg)
                            .error(R.drawable.circle_bg)
                            .into(ivAvatar);

                    ivAvatar.setVisibility(View.VISIBLE);
                    tvAvatar.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                    ivAvatar.setVisibility(View.GONE);
                    tvAvatar.setVisibility(View.VISIBLE);
                    tvAvatar.setText("👧");
                }
            } else {
                ivAvatar.setVisibility(View.GONE);
                tvAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setText(avatar);
            }
        } else {
            ivAvatar.setVisibility(View.GONE);
            tvAvatar.setVisibility(View.VISIBLE);
            tvAvatar.setText("👧");
        }

        ivAvatar.invalidate();
        tvAvatar.invalidate();
    }

    private void loadActivities() {
        String savedActivities = routinePrefs.getString(getActivityPrefKey(), "");
        if (savedActivities.isEmpty()) {
            savedActivities = routinePrefs.getString("activities", "");
        }
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
        }
    }

    private String getActivityPrefKey() {
        if (childId != null && !childId.isEmpty()) {
            return "activities_" + childId;
        }
        return "activities";
    }

    private void loadPoints() {
        String childKey = childId + "_";
        int totalPoints = rewardPrefs.getInt(childKey + "totalPoints", 0);
        tvTotalPoints.setText(String.valueOf(totalPoints));
    }

    private void loadTodayProgress() {
        int totalActivities = activityList.size();
        String todayKey = dateKeyFormat.format(new Date());
        Set<String> completedToday = getCompletedTasksForDate(todayKey);
        int completedCount = countMatchingActivities(completedToday);

        tvCompletedCount.setText(completedCount + " / " + totalActivities);

        taskListContainer.removeAllViews();
        for (int i = 0; i < activityList.size(); i++) {
            String activity = activityList.get(i);
            boolean isCompleted = completedToday.contains(activity);
            View taskView = createTaskRow(activity, isCompleted);
            taskListContainer.addView(taskView);
        }
    }

    private void loadWeeklyChart() {
        if (weeklyChartContainer == null || chartSummaryContainer == null) return;

        weeklyChartContainer.removeAllViews();
        chartSummaryContainer.removeAllViews();

        List<Calendar> weekDates = getLastSevenDays();
        Map<String, Integer> weeklyCounts = getCompletedCountsForWeek(weekDates);

        int maxCount = 0;
        int totalCompleted = 0;
        int fullDays = 0;
        String bestDayLabel = "-";
        int bestDayCount = 0;

        for (Calendar date : weekDates) {
            String key = dateKeyFormat.format(date.getTime());
            int count = weeklyCounts.getOrDefault(key, 0);
            totalCompleted += count;
            if (count > maxCount) {
                maxCount = count;
                bestDayCount = count;
                bestDayLabel = new SimpleDateFormat("EEE", Locale.getDefault()).format(date.getTime());
            }
            if (count >= activityList.size() && activityList.size() > 0) {
                fullDays++;
            }
        }

        if (maxCount == 0) {
            maxCount = 1;
        }

        TextView chartLabel = new TextView(this);
        chartLabel.setText("Completed activities in the last 7 days");
        chartLabel.setTextColor(getColor(R.color.text_dark));
        chartLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chartLabel.setTypeface(null, Typeface.BOLD);
        chartLabel.setPadding(0, 0, 0, dpToPx(10));
        weeklyChartContainer.addView(chartLabel);

        LinearLayout chartRow = new LinearLayout(this);
        chartRow.setOrientation(LinearLayout.HORIZONTAL);
        chartRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        chartRow.setPadding(0, dpToPx(8), 0, dpToPx(10));

        for (Calendar date : weekDates) {
            String key = dateKeyFormat.format(date.getTime());
            int count = weeklyCounts.getOrDefault(key, 0);
            float ratio = count / (float) maxCount;
            int barHeight = dpToPx(24 + Math.round(80 * ratio));

            LinearLayout barItem = new LinearLayout(this);
            barItem.setOrientation(LinearLayout.VERTICAL);
            barItem.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams barItemParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            barItemParams.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            barItem.setLayoutParams(barItemParams);

            TextView tvValue = new TextView(this);
            tvValue.setText(count > 0 ? String.valueOf(count) : "");
            tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvValue.setTextColor(getColor(R.color.text_dark));
            tvValue.setGravity(Gravity.CENTER);
            barItem.addView(tvValue);

            View barBg = new View(this);
            LinearLayout.LayoutParams barBgParams = new LinearLayout.LayoutParams(dpToPx(20), dpToPx(100));
            barBgParams.topMargin = dpToPx(8);
            barBg.setLayoutParams(barBgParams);
            barBg.setBackgroundResource(R.drawable.chart_bar_background);

            FrameLayout barWrapper = new FrameLayout(this);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapperParams.topMargin = dpToPx(8);
            barWrapper.setLayoutParams(wrapperParams);
            barWrapper.addView(barBg);

            View bar = new View(this);
            FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(dpToPx(20), barHeight, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            bar.setLayoutParams(barParams);
            bar.setBackgroundResource(R.drawable.chart_bar_fill);
            barWrapper.addView(bar);
            barItem.addView(barWrapper);

            TextView tvDay = new TextView(this);
            tvDay.setText(new SimpleDateFormat("EEE", Locale.getDefault()).format(date.getTime()));
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvDay.setTextColor(getColor(R.color.light_brown));
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setPadding(0, dpToPx(8), 0, 0);
            barItem.addView(tvDay);

            chartRow.addView(barItem);
        }

        weeklyChartContainer.addView(chartRow);

        LinearLayout summaryRow = new LinearLayout(this);
        summaryRow.setOrientation(LinearLayout.HORIZONTAL);
        summaryRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        summaryRow.setWeightSum(3);

        summaryRow.addView(createSummaryItem("Total", String.valueOf(totalCompleted), "activities"));
        summaryRow.addView(createSummaryItem("Full Days", String.valueOf(fullDays), "perfect days"));
        summaryRow.addView(createSummaryItem("Best", bestDayLabel, bestDayCount + " tasks"));

        chartSummaryContainer.addView(summaryRow);
    }

    private View createSummaryItem(String title, String value, String subtitle) {
        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        summary.setGravity(Gravity.CENTER);
        summary.setPadding(dpToPx(12), dpToPx(14), dpToPx(12), dpToPx(14));
        summary.setBackgroundResource(R.drawable.summary_card_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        summary.setLayoutParams(params);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvTitle.setTextColor(getColor(R.color.light_brown));
        tvTitle.setGravity(Gravity.CENTER);
        summary.addView(tvTitle);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setTextColor(getColor(R.color.text_dark));
        tvValue.setGravity(Gravity.CENTER);
        tvValue.setPadding(0, dpToPx(2), 0, 0);
        summary.addView(tvValue);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(subtitle);
        tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tvSubtitle.setTextColor(getColor(R.color.light_brown));
        tvSubtitle.setGravity(Gravity.CENTER);
        tvSubtitle.setPadding(0, dpToPx(2), 0, 0);
        summary.addView(tvSubtitle);

        return summary;
    }

    private List<Calendar> getLastSevenDays() {
        List<Calendar> days = new ArrayList<>();
        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        for (int i = 6; i >= 0; i--) {
            Calendar copy = (Calendar) day.clone();
            copy.add(Calendar.DAY_OF_YEAR, -i);
            days.add(copy);
        }
        return days;
    }

    private Map<String, Integer> getCompletedCountsForWeek(List<Calendar> weekDates) {
        Map<String, Integer> counts = new HashMap<>();
        for (Calendar date : weekDates) {
            String key = dateKeyFormat.format(date.getTime());
            Set<String> completed = getCompletedTasksForDate(key);
            counts.put(key, countMatchingActivities(completed));
        }
        return counts;
    }

    private View createTaskRow(String activityName, boolean isCompleted) {
        View taskView = getLayoutInflater().inflate(R.layout.item_progress_task, taskListContainer, false);

        LinearLayout taskIconBg = taskView.findViewById(R.id.taskIconBg);
        ImageView ivTaskIcon = taskView.findViewById(R.id.ivTaskIcon);
        TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);
        TextView tvStatus = taskView.findViewById(R.id.tvStatus);

        int bgRes = getTaskIconBg(activityName);
        if (bgRes != 0) {
            taskIconBg.setBackgroundResource(bgRes);
        }

        int iconRes = getTaskIconRes(activityName);
        if (iconRes != 0) {
            ivTaskIcon.setImageResource(iconRes);
        } else {
            ivTaskIcon.setImageResource(R.drawable.ic_task_icon);
        }

        tvTaskName.setText(activityName);

        if (isCompleted) {
            tvStatus.setText("✅ Completed");
            tvStatus.setTextColor(getColor(R.color.soft_blue));
        } else {
            tvStatus.setText("○ Not Started");
            tvStatus.setTextColor(getColor(R.color.light_brown));
        }

        return taskView;
    }

    private int getTaskIconRes(String taskName) {
        String drawableName;
        switch (taskName) {
            case "Brush Teeth":
                drawableName = "brush";
                break;
            case "Eat Foods":
                drawableName = "foods";
                break;
            case "Wash Hands":
                drawableName = "hands";
                break;
            case "Sleep":
                drawableName = "sleep";
                break;
            case "Pack School Bag":
                drawableName = "bag";
                break;
            case "Wear Clothes":
                drawableName = "clothes";
                break;
            default:
                return 0;
        }
        return getResources().getIdentifier(drawableName, "drawable", getPackageName());
    }

    private int getTaskIconBg(String taskName) {
        switch (taskName) {
            case "Eat Foods":
                return R.drawable.task_icon_bg_green;
            case "Wash Hands":
                return R.drawable.task_icon_bg_yellow;
            case "Sleep":
            case "Pack School Bag":
            case "Wear Clothes":
                return R.drawable.task_icon_bg_blue;
            default:
                return R.drawable.task_icon_bg_blue;
        }
    }

    private void updateMonthPickerLabel() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        tvMonthPicker.setText(months[currentMonth] + " " + currentYear + " ▾");
    }

    private void showMonthYearPicker() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = new LinearLayout(this);
        LinearLayout layout = (LinearLayout) dialogView;
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

        // Year Selection
        LinearLayout yearLayout = new LinearLayout(this);
        yearLayout.setOrientation(LinearLayout.HORIZONTAL);
        yearLayout.setGravity(Gravity.CENTER_VERTICAL);
        yearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        yearLayout.setPadding(0, 0, 0, dpToPx(16));

        TextView yearLabel = new TextView(this);
        yearLabel.setText("Year:");
        yearLabel.setTextSize(14);
        yearLabel.setTextColor(getColor(R.color.text_dark));
        yearLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));

        android.widget.Spinner yearSpinner = new android.widget.Spinner(this);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f);
        spinnerParams.setMargins(dpToPx(8), 0, 0, 0);
        yearSpinner.setLayoutParams(spinnerParams);

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        List<Integer> yearList = new ArrayList<>();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearList.add(i);
        }

        android.widget.ArrayAdapter<Integer> yearAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, yearList);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(yearList.indexOf(this.currentYear));

        yearLayout.addView(yearLabel);
        yearLayout.addView(yearSpinner);
        layout.addView(yearLayout);

        // Month Selection
        LinearLayout monthLayout = new LinearLayout(this);
        monthLayout.setOrientation(LinearLayout.HORIZONTAL);
        monthLayout.setGravity(Gravity.CENTER_VERTICAL);
        monthLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView monthLabel = new TextView(this);
        monthLabel.setText("Month:");
        monthLabel.setTextSize(14);
        monthLabel.setTextColor(getColor(R.color.text_dark));
        monthLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));

        android.widget.Spinner monthSpinner = new android.widget.Spinner(this);
        spinnerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f);
        spinnerParams.setMargins(dpToPx(8), 0, 0, 0);
        monthSpinner.setLayoutParams(spinnerParams);

        List<String> monthList = java.util.Arrays.asList(months);
        android.widget.ArrayAdapter<String> monthAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, monthList);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(this.currentMonth);

        monthLayout.addView(monthLabel);
        monthLayout.addView(monthSpinner);
        layout.addView(monthLayout);

        builder.setView(layout);
        builder.setTitle("Select Month & Year");
        builder.setPositiveButton("OK", (dialog, which) -> {
            this.currentYear = yearList.get(yearSpinner.getSelectedItemPosition());
            this.currentMonth = monthSpinner.getSelectedItemPosition();
            updateMonthPickerLabel();
            setupCalendar();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            negativeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void setupCalendar() {
        if (calendarContainer == null) return;

        calendarContainer.removeAllViews();

        String[] dayHeaders = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setPadding(0, 0, 0, dpToPx(8));

        for (String day : dayHeaders) {
            TextView dayHeader = new TextView(this);
            dayHeader.setText(day);
            dayHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            dayHeader.setTextColor(getColor(R.color.light_brown));
            dayHeader.setGravity(Gravity.CENTER);
            dayHeader.setTypeface(null, Typeface.BOLD);
            dayHeader.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(32), 1f));
            headerRow.addView(dayHeader);
        }
        calendarContainer.addView(headerRow);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
        divider.setBackgroundColor(getColor(R.color.divider));
        LinearLayout.LayoutParams dividerParams = (LinearLayout.LayoutParams) divider.getLayoutParams();
        dividerParams.bottomMargin = dpToPx(10);
        calendarContainer.addView(divider);

        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(currentYear, currentMonth, 1);
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar prevMonth = Calendar.getInstance();
        prevMonth.set(currentYear, currentMonth, 1);
        prevMonth.add(Calendar.DAY_OF_MONTH, -1);
        int daysInPrevMonth = prevMonth.get(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        int day = 1;
        int nextMonthDay = 1;

        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                int cellIndex = row * 7 + col;

                if (cellIndex < firstDayOfWeek && day == 1) {
                    int prevDay = daysInPrevMonth - (firstDayOfWeek - cellIndex - 1);
                    rowLayout.addView(createAdjacentMonthCell(prevDay));
                } else if (day <= daysInMonth) {
                    String dateKey = formatDateKey(currentYear, currentMonth, day);
                    boolean isFuture = isFutureDate(currentYear, currentMonth, day);
                    boolean isToday = currentYear == todayYear && currentMonth == todayMonth && day == todayDay;
                    int status = getStatusForDate(dateKey, isFuture, isToday);

                    final int displayDay = day;
                    View cell = createDayCell(displayDay, status, isFuture, v ->
                            showDayDetailDialog(dateKey, displayDay, currentMonth + 1, currentYear));
                    rowLayout.addView(cell);
                    day++;
                } else if (nextMonthDay <= 14) {
                    rowLayout.addView(createAdjacentMonthCell(nextMonthDay));
                    nextMonthDay++;
                } else {
                    rowLayout.addView(createEmptyCell());
                }
            }

            gridContainer.addView(rowLayout);
        }

        calendarContainer.addView(gridContainer);
        calendarContainer.addView(createLegend());
    }

    private View createEmptyCell() {
        FrameLayout cell = new FrameLayout(this);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(ROW_HEIGHT_DP), 1f));
        return cell;
    }

    private View createAdjacentMonthCell(int day) {
        FrameLayout cell = new FrameLayout(this);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(ROW_HEIGHT_DP), 1f));

        TextView dayText = new TextView(this);
        dayText.setText(String.valueOf(day));
        dayText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dayText.setTextColor(getColor(R.color.light_brown));
        dayText.setAlpha(0.35f);
        dayText.setGravity(Gravity.CENTER);
        dayText.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        cell.addView(dayText);
        return cell;
    }

    private interface DayClickListener {
        void onClick(View v);
    }

    private View createDayCell(int day, int status, boolean isFuture, DayClickListener listener) {
        FrameLayout cell = new FrameLayout(this);
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(ROW_HEIGHT_DP), 1f));

        int circleSize = dpToPx(CIRCLE_SIZE_DP);

        View circleBg = new View(this);
        FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(circleSize, circleSize, Gravity.CENTER);
        circleBg.setLayoutParams(circleParams);
        circleBg.setBackgroundResource(getCircleBackground(status, isFuture));
        cell.addView(circleBg);

        TextView dayText = new TextView(this);
        dayText.setText(String.valueOf(day));
        dayText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        dayText.setGravity(Gravity.CENTER);
        dayText.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        if (isFuture) {
            dayText.setTextColor(getColor(R.color.light_brown));
            dayText.setAlpha(0.5f);
        } else if (status == STATUS_COMPLETED || status == STATUS_PARTIAL) {
            dayText.setTextColor(getColor(R.color.white));
            dayText.setAlpha(1f);
        } else if (status == STATUS_MISSED) {
            dayText.setTextColor(getColor(R.color.white));
            dayText.setAlpha(0.85f);
        } else {
            dayText.setTextColor(getColor(R.color.white));
            dayText.setAlpha(0.7f);
        }

        cell.addView(dayText);

        if (!isFuture) {
            cell.setClickable(true);
            cell.setFocusable(true);
            android.graphics.drawable.Drawable ripple = getSelectableItemBackground();
            if (ripple != null) {
                cell.setForeground(ripple);
            }
            cell.setOnClickListener(listener::onClick);
        }

        return cell;
    }

    private int getCircleBackground(int status, boolean isFuture) {
        if (isFuture) {
            return R.drawable.calendar_no_activity;
        }
        switch (status) {
            case STATUS_COMPLETED:
                return R.drawable.calendar_completed;
            case STATUS_PARTIAL:
                return R.drawable.calendar_partial_green;
            case STATUS_MISSED:
                return R.drawable.calendar_missed;
            default:
                return R.drawable.calendar_no_activity;
        }
    }

    private View createLegend() {
        LinearLayout legend = new LinearLayout(this);
        legend.setOrientation(LinearLayout.HORIZONTAL);
        legend.setGravity(Gravity.CENTER);
        legend.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        legend.setPadding(0, dpToPx(16), 0, 0);

        legend.addView(createLegendItem(R.drawable.calendar_completed, "Completed"));
        legend.addView(createLegendItem(R.drawable.calendar_partial_green, "Partial"));
        legend.addView(createLegendItem(R.drawable.calendar_missed, "Missed"));
        legend.addView(createLegendItem(R.drawable.calendar_no_activity, "No Activity"));

        return legend;
    }

    private View createLegendItem(int circleDrawable, String label) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        item.setLayoutParams(params);

        View dot = new View(this);
        int dotSize = dpToPx(10);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.setMargins(0, 0, dpToPx(4), 0);
        dot.setLayoutParams(dotParams);
        dot.setBackgroundResource(circleDrawable);
        item.addView(dot);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        text.setTextColor(getColor(R.color.light_brown));
        text.setSingleLine(true);
        text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        item.addView(text);

        return item;
    }

    private int getStatusForDate(String dateKey, boolean isFuture, boolean isToday) {
        if (isFuture) {
            return STATUS_FUTURE;
        }

        Set<String> completed = getCompletedTasksForDate(dateKey);
        int count = countMatchingActivities(completed);
        int total = activityList.size();

        if (count >= total) {
            return STATUS_COMPLETED;
        }
        if (count > 0) {
            return STATUS_PARTIAL;
        }
        if (isToday) {
            return STATUS_NONE;
        }
        return STATUS_MISSED;
    }

    private int countMatchingActivities(Set<String> completedTasks) {
        int count = 0;
        for (String activity : activityList) {
            if (completedTasks.contains(activity)) {
                count++;
            }
        }
        return count;
    }

    private Set<String> getCompletedTasksForDate(String dateKey) {
        Set<String> tasks = new HashSet<>();

        String childKey = childId + "_";

        String history = rewardPrefs.getString(childKey + "taskHistory", "");
        parseHistoryIntoSet(history, tasks, dateKey);

        String legacyHistory = rewardPrefs.getString("taskHistory", "");
        parseHistoryIntoSet(legacyHistory, tasks, dateKey);

        if (tasks.isEmpty()) {
            String completedDates = rewardPrefs.getString(childKey + "completedDates", "");
            if (completedDates.isEmpty()) {
                completedDates = rewardPrefs.getString("completedDates", "");
            }
            if (!completedDates.isEmpty()) {
                int count = 0;
                for (String date : completedDates.split(",")) {
                    if (date != null && date.equals(dateKey)) {
                        count++;
                    }
                }
                if (count > 0) {
                    for (int i = 0; i < Math.min(count, activityList.size()); i++) {
                        tasks.add(activityList.get(i));
                    }
                }
            }
        }

        return tasks;
    }

    private void parseHistoryIntoSet(String history, Set<String> tasks, String dateKey) {
        if (history == null || history.isEmpty()) return;

        for (String entry : history.split("\\|")) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(",");
            if (parts.length >= 2) {
                String taskName = parts[0].trim();
                String date = normalizeDateKey(parts[1].trim());
                if (date.equals(dateKey)) {
                    tasks.add(taskName);
                }
            }
        }
    }

    private String normalizeDateKey(String date) {
        try {
            Date parsed = dateKeyFormat.parse(date);
            if (parsed != null) {
                return dateKeyFormat.format(parsed);
            }
            parsed = legacyDateFormat.parse(date);
            if (parsed != null) {
                return dateKeyFormat.format(parsed);
            }
        } catch (Exception ignored) {
        }
        return date;
    }

    private String formatDateKey(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return dateKeyFormat.format(cal.getTime());
    }

    private boolean isFutureDate(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(year, month, day, 0, 0, 0);
        target.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return target.after(today);
    }

    /**
     * Show day detail dialog with functional CLOSE button
     */
    private void showDayDetailDialog(String dateKey, int day, int month, int year) {
        try {
            Set<String> completedTasks = getCompletedTasksForDate(dateKey);
            int completedCount = countMatchingActivities(completedTasks);
            int totalActivities = activityList.size();

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_day_detail, null);

            TextView tvDate = dialogView.findViewById(R.id.tvDate);
            TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
            LinearLayout taskDetailContainer = dialogView.findViewById(R.id.taskDetailContainer);
            Button btnClose = dialogView.findViewById(R.id.btnClose);

            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1);
            String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime());
            tvDate.setText(monthName + " " + day + ", " + year);

            if (completedCount >= totalActivities) {
                tvStatus.setText("✅ All completed!");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));
            } else if (completedCount > 0) {
                tvStatus.setText("⏳ " + completedCount + "/" + totalActivities + " completed");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.khaki));
            } else {
                boolean isToday = dateKey.equals(dateKeyFormat.format(new Date()));
                if (isToday) {
                    tvStatus.setText("○ No activities completed yet today");
                } else {
                    tvStatus.setText("❌ No activities completed");
                }
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.light_brown));
            }

            taskDetailContainer.removeAllViews();
            for (int i = 0; i < activityList.size(); i++) {
                String activity = activityList.get(i);
                boolean isCompleted = completedTasks.contains(activity);

                View taskView = getLayoutInflater().inflate(R.layout.item_progress_task, taskDetailContainer, false);

                LinearLayout iconBg = taskView.findViewById(R.id.taskIconBg);
                ImageView ivTaskIcon = taskView.findViewById(R.id.ivTaskIcon);
                TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);
                TextView tvStatusText = taskView.findViewById(R.id.tvStatus);

                int iconRes = getTaskIconRes(activity);
                if (iconRes != 0) {
                    ivTaskIcon.setImageResource(iconRes);
                } else {
                    ivTaskIcon.setImageResource(R.drawable.ic_task_icon);
                }

                int bgRes = getTaskIconBg(activity);
                if (bgRes != 0) {
                    iconBg.setBackgroundResource(bgRes);
                }

                tvTaskName.setText(activity);

                if (isCompleted) {
                    tvStatusText.setText("✅ Completed");
                    tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.soft_blue));
                } else {
                    tvStatusText.setText("○ Not Started");
                    tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.light_brown));
                }

                taskDetailContainer.addView(taskView);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setCancelable(true);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Set CLOSE button click listener
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
            });

            // Also handle the back button
            dialog.setOnCancelListener(dialogInterface -> {
                // Dialog dismissed by back button or outside click
            });

        } catch (Exception e) {
            e.printStackTrace();
            showSimpleDayDetailDialog(dateKey, day, month, year);
        }
    }

    private void showSimpleDayDetailDialog(String dateKey, int day, int month, int year) {
        try {
            Set<String> completedTasks = getCompletedTasksForDate(dateKey);
            int completedCount = countMatchingActivities(completedTasks);
            int totalActivities = activityList.size();

            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1);
            String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime());
            String title = monthName + " " + day + ", " + year;

            String status = completedCount >= totalActivities ? "✅ All completed!" :
                    completedCount > 0 ? "⏳ " + completedCount + "/" + totalActivities + " completed" :
                            "❌ No activities completed";

            StringBuilder details = new StringBuilder();
            details.append(status).append("\n\n");

            for (String activity : activityList) {
                boolean isCompleted = completedTasks.contains(activity);
                details.append(isCompleted ? "✅ " : "○ ").append(activity).append("\n");
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(details.toString())
                    .setPositiveButton("Close", null)
                    .create();

            dialog.show();

            // Fix: Change Close button color
            Button closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (closeButton != null) {
                closeButton.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                closeButton.setTypeface(null, android.graphics.Typeface.BOLD);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing details", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private android.graphics.drawable.Drawable getSelectableItemBackground() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return ContextCompat.getDrawable(this, outValue.resourceId);
    }

    private void updateNavHeader() {
        if (navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        TextView tvChildName = headerView.findViewById(R.id.tvChildName);
        TextView tvChildStatus = headerView.findViewById(R.id.tvChildStatus);
        ImageView headerProfileImage = headerView.findViewById(R.id.headerProfileImage);
        TextView headerProfileIcon = headerView.findViewById(R.id.headerProfileIcon);

        SharedPreferences prefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
        String childName = prefs.getString("childName", "Child");
        String childAvatar = prefs.getString("childAvatar", "👧");

        if (tvChildName != null) {
            tvChildName.setText(childName);
        }

        if (tvChildStatus != null) {
            tvChildStatus.setText("● Active");
        }

        if (headerProfileImage != null && headerProfileIcon != null) {
            if (childAvatar != null && (childAvatar.startsWith("content://") || childAvatar.startsWith("file://") || childAvatar.startsWith("http://") || childAvatar.startsWith("https://"))) {
                try {
                    Glide.with(this)
                            .load(childAvatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.circle_bg)
                            .into(headerProfileImage);
                    headerProfileImage.setVisibility(View.VISIBLE);
                    headerProfileIcon.setVisibility(View.GONE);
                } catch (Exception ignored) {
                    headerProfileImage.setVisibility(View.GONE);
                    headerProfileIcon.setVisibility(View.VISIBLE);
                    headerProfileIcon.setText("👧");
                }
            } else {
                headerProfileImage.setVisibility(View.GONE);
                headerProfileIcon.setVisibility(View.VISIBLE);
                headerProfileIcon.setText(childAvatar != null && !childAvatar.isEmpty() ? childAvatar : "👧");
            }
        }
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
        } else if (id == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeCustomizationActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void showLogoutDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnYes = dialogView.findViewById(R.id.btnYes);

        int nightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDarkMode = (nightMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        if (isDarkMode) {
            btnCancel.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnYes.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnCancel.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_card_bg));
            btnYes.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.soft_blue));
        } else {
            btnCancel.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            btnYes.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            btnCancel.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.khaki));
            btnYes.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.soft_blue));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(ProgressTrackerActivity.this, GoodbyeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
        IntentFilter filter = new IntentFilter("PROFILE_UPDATED");
        registerReceiver(profileUpdateReceiver, filter);

        childId = childPrefs.getString("childId", "default");
        childName = childPrefs.getString("childName", "Child");
        tvChildName.setText(childName);

        String avatar = childPrefs.getString("childAvatar", "👧");
        updateProfileAvatar(avatar);

        loadActivities();
        loadPoints();
        loadTodayProgress();
        loadWeeklyChart();
        updateMonthPickerLabel();
        setupCalendar();
        updateNavHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(profileUpdateReceiver);
        } catch (Exception e) {
            // Receiver not registered
        }
    }
}