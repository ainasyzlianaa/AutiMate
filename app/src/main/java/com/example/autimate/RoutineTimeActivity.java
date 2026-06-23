package com.example.autimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RoutineTimeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardBrushTeeth, cardEatFoods, cardWashHands, cardNap, cardSleep;
    private CardView cardPackBag, cardWearClothes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_time);

        try {
            // Initialize views
            btnBack = findViewById(R.id.btnBack);
            cardBrushTeeth = findViewById(R.id.cardBrushTeeth);
            cardEatFoods = findViewById(R.id.cardEatFoods);
            cardWashHands = findViewById(R.id.cardWashHands);
            cardSleep = findViewById(R.id.cardSleep);
            cardPackBag = findViewById(R.id.cardPackBag);
            cardWearClothes = findViewById(R.id.cardWearClothes);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            // Hide all cards first
            hideAllCards();

            // Load and show saved activities
            loadSavedActivities();

            // Register broadcast receiver
            IntentFilter filter = new IntentFilter("ACTIVITIES_UPDATED");
            registerReceiver(activityUpdateReceiver, filter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading routine page", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAllCards() {
        if (cardBrushTeeth != null) cardBrushTeeth.setVisibility(View.GONE);
        if (cardEatFoods != null) cardEatFoods.setVisibility(View.GONE);
        if (cardWashHands != null) cardWashHands.setVisibility(View.GONE);
        if (cardNap != null) cardNap.setVisibility(View.GONE);
        if (cardSleep != null) cardSleep.setVisibility(View.GONE);
        if (cardPackBag != null) cardPackBag.setVisibility(View.GONE);
        if (cardWearClothes != null) cardWearClothes.setVisibility(View.GONE);
    }

    private void loadSavedActivities() {
        SharedPreferences prefs = getSharedPreferences("RoutinePrefs", MODE_PRIVATE);
        String savedActivities = prefs.getString("activities", "");

        if (savedActivities.isEmpty()) {
            showDefaultActivities();
            return;
        }

        String[] activities = savedActivities.split(",");
        for (String activity : activities) {
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

    private void showDefaultActivities() {
        if (cardBrushTeeth != null) {
            cardBrushTeeth.setVisibility(View.VISIBLE);
            cardBrushTeeth.setOnClickListener(v -> openBrushTeeth());
        }
        if (cardEatFoods != null) {
            cardEatFoods.setVisibility(View.VISIBLE);
            cardEatFoods.setOnClickListener(v -> openEatFoods());
        }
        if (cardWashHands != null) {
            cardWashHands.setVisibility(View.VISIBLE);
            cardWashHands.setOnClickListener(v -> openWashHands());
        }
        if (cardSleep != null) {
            cardSleep.setVisibility(View.VISIBLE);
            cardSleep.setOnClickListener(v -> openSleep());
        }
    }

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

    private void openSleep() {
        openStepDetail("Sleep Time", new String[]{
                "Put on pajamas",
                "Brush your teeth",
                "Read a book",
                "Say goodnight",
                "Sweet dreams!"
        }, new String[]{
                "pajamas.mp4",
                "brushteeth.mp4",
                "readbook.mp4",
                "goodnight.mp4",
                "dreams.mp4"
        });
    }

    private void openPackBag() {
        openStepDetail("Pack School Bag", new String[]{
                "Open your bag",
                "Put books inside",
                "Put pencil case",
                "Pack lunch box",
                "Close the bag"
        }, new String[]{
                "openbag.mp4",
                "putbooks.mp4",
                "pencilcase.mp4",
                "lunchbox.mp4",
                "closebag.mp4"
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
            hideAllCards();
            loadSavedActivities();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(activityUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}