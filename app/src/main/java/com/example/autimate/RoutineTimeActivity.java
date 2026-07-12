package com.example.autimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RoutineTimeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEmptyMessage;
    private CardView cardBrushTeeth, cardEatFoods, cardWashHands, cardSleep;
    private CardView cardPackBag, cardWearClothes;
    private String childId;
    private String parentId;
    private FirebaseFirestore db;
    private List<String> activityNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_time);

        try {
            db = FirebaseFirestore.getInstance();

            btnBack = findViewById(R.id.btnBack);
            tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
            cardBrushTeeth = findViewById(R.id.cardBrushTeeth);
            cardEatFoods = findViewById(R.id.cardEatFoods);
            cardWashHands = findViewById(R.id.cardWashHands);
            cardSleep = findViewById(R.id.cardSleep);
            cardPackBag = findViewById(R.id.cardPackBag);
            cardWearClothes = findViewById(R.id.cardWearClothes);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            childId = getSharedPreferences("ChildPrefs", MODE_PRIVATE).getString("childId", "");
            parentId = getSharedPreferences("ParentPrefs", MODE_PRIVATE).getString("parentId", "");

            loadActivities();

            IntentFilter filter = new IntentFilter("ACTIVITIES_UPDATED");
            registerReceiver(activityUpdateReceiver, filter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading routine page", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadActivities() {
        // Hide all cards first
        hideAllCards();

        if (childId == null || childId.isEmpty()) {
            showEmptyMessage();
            return;
        }

        // Load from child's activities array in Firestore
        db.collection("children").document(childId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get activities array from child document
                        List<String> activities = (List<String>) documentSnapshot.get("activities");

                        if (activities != null && !activities.isEmpty()) {
                            activityNames.clear();
                            activityNames.addAll(activities);

                            // Save to SharedPreferences for faster access
                            saveActivitiesToPrefs();

                            // Display the activities
                            displayActivities();
                        } else {
                            // No activities found - show empty message
                            showEmptyMessage();
                        }
                    } else {
                        showEmptyMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    showEmptyMessage();
                });
    }

    private void showEmptyMessage() {
        hideAllCards();
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("No activities added yet.\nAsk your parent to add activities for you! 🌟");
        }
    }

    private void displayActivities() {
        hideAllCards();
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.GONE);
        }

        if (activityNames.isEmpty()) {
            showEmptyMessage();
            return;
        }

        for (String activity : activityNames) {
            switch (activity) {
                case "Brush Teeth":
                    if (cardBrushTeeth != null) {
                        cardBrushTeeth.setVisibility(View.VISIBLE);
                        cardBrushTeeth.setOnClickListener(v -> openBrushTeeth());
                    }
                    break;
                case "Eat Foods":
                    if (cardEatFoods != null) {
                        cardEatFoods.setVisibility(View.VISIBLE);
                        cardEatFoods.setOnClickListener(v -> openEatFoods());
                    }
                    break;
                case "Wash Hands":
                    if (cardWashHands != null) {
                        cardWashHands.setVisibility(View.VISIBLE);
                        cardWashHands.setOnClickListener(v -> openWashHands());
                    }
                    break;
                case "Sleep":
                    if (cardSleep != null) {
                        cardSleep.setVisibility(View.VISIBLE);
                        cardSleep.setOnClickListener(v -> openSleep());
                    }
                    break;
                case "Pack School Bag":
                    if (cardPackBag != null) {
                        cardPackBag.setVisibility(View.VISIBLE);
                        cardPackBag.setOnClickListener(v -> openPackBag());
                    }
                    break;
                case "Wear Clothes":
                    if (cardWearClothes != null) {
                        cardWearClothes.setVisibility(View.VISIBLE);
                        cardWearClothes.setOnClickListener(v -> openWearClothes());
                    }
                    break;
            }
        }
    }

    private void hideAllCards() {
        if (cardBrushTeeth != null) cardBrushTeeth.setVisibility(View.GONE);
        if (cardEatFoods != null) cardEatFoods.setVisibility(View.GONE);
        if (cardWashHands != null) cardWashHands.setVisibility(View.GONE);
        if (cardSleep != null) cardSleep.setVisibility(View.GONE);
        if (cardPackBag != null) cardPackBag.setVisibility(View.GONE);
        if (cardWearClothes != null) cardWearClothes.setVisibility(View.GONE);
    }

    private void saveActivitiesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (String activity : activityNames) {
            if (sb.length() > 0) sb.append(",");
            sb.append(activity);
        }
        getSharedPreferences("RoutinePrefs", MODE_PRIVATE)
                .edit()
                .putString(getActivityPrefKey(), sb.toString())
                .apply();
    }

    private String getActivityPrefKey() {
        if (childId != null && !childId.isEmpty()) {
            return "activities_" + childId;
        }
        return "activities";
    }

    // ===================== UPDATED: Pack School Bag =====================
    private void openPackBag() {
        openStepDetail("Pack School Bag", new String[]{
                "Pick your bag",
                "Open the bag",
                "Close the bag",
                "Wear the bag"
        }, new String[]{
                "pickbag_test.mp4",
                "openbag_test.mp4",
                "closebag_test.mp4",
                "wearbag_test.mp4"
        });
    }

    // ===================== UPDATED: Sleep =====================
    private void openSleep() {
        openStepDetail("Sleep", new String[]{
                "Get ready for bed",
                "Sit on your bed",
                "Time to sleep!"
        }, new String[]{
                "readysleep_test.mp4",
                "sitbed_test.mp4",
                "sleep_test.mp4"
        });
    }

    // ===================== OTHER ACTIVITIES (Unchanged) =====================
    private void openBrushTeeth() {
        openStepDetail("Brush Teeth", new String[]{
                "Take a toothbrush",
                "Put toothpaste on brush",
                "Brush top teeth",
                "Brush bottom teeth",
                "Put back toothbrush"
        }, new String[]{
                "takebrush_test.mp4",
                "puttoothpaste_test.mp4",
                "topteeth_test.mp4",
                "bottomteeth_test.mp4",
                "putbrush_test.mp4"
        });
    }

    private void openEatFoods() {
        openStepDetail("Eat Foods", new String[]{
                "Sit at the table",
                "Take spoon and fork",
                "Eat your food slowly",
                "Drink some water",
                "Wipe your mouth"
        }, new String[]{
                "sitattable_test.mp4",
                "spoonfork_test.mp4",
                "eat_test.mp4",
                "drink_test.mp4",
                "wipemouth_test.mp4"
        });
    }

    private void openWashHands() {
        openStepDetail("Wash Hands", new String[]{
                "Turn on water",
                "Pump soap",
                "Rinse hands",
                "Wipe hands dry"
        }, new String[]{
                "openwater_test.mp4",
                "pumpsoap_test.mp4",
                "rinsehand_test.mp4",
                "wipehand_test.mp4"
        });
    }

    private void openWearClothes() {
        openStepDetail("Wear Clothes", new String[]{
                "Pick your clothes",
                "Put on shirt",
                "Put on pants",
                "Put on socks",
                "Look in the mirror"
        }, new String[]{
                "pickclothes.mp4",
                "putonshirt.mp4",
                "putonpants.mp4",
                "putonsocks.mp4",
                "lookmirror.mp4"
        });
    }

    private void openStepDetail(String title, String[] steps, String[] videoFiles) {
        Intent intent = new Intent(RoutineTimeActivity.this, StepDetailActivity.class);
        intent.putExtra("task_title", title);
        intent.putExtra("task_steps", steps);
        intent.putExtra("task_videos", videoFiles);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private final BroadcastReceiver activityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTIVITIES_UPDATED".equals(intent.getAction())) {
                activityNames.clear();
                loadActivities();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(activityUpdateReceiver);
        } catch (Exception e) {
            // Receiver not registered
        }
    }
}