package com.example.autimate;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragDropGameActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvScore, tvMessage;
    private Button btnReset;
    private LinearLayout foodsContainer;
    private HorizontalScrollView scrollView;
    private CardView redBasket, yellowBasket, greenBasket, purpleBasket;
    private Vibrator vibrator;
    private MediaPlayer correctSound;

    private int score = 0;
    private List<View> foodCards = new ArrayList<>();
    private Map<String, String> foodColors = new HashMap<>();

    // YOUR EXACT FOOD ITEMS
    private String[] foodNames = {"Apple", "Cherry", "Banana", "Cheese", "Grape", "Yam", "Pea", "Pear"};
    private String[] foodColorsList = {"RED", "RED", "YELLOW", "YELLOW", "PURPLE", "PURPLE", "GREEN", "GREEN"};
    private int[] foodImageIds = {
            R.drawable.apple, R.drawable.cherry, R.drawable.banana, R.drawable.cheese,
            R.drawable.grape, R.drawable.yam, R.drawable.pea, R.drawable.pear
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_drop_game);

        // Initialize vibration
        try {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize sound
        try {
            correctSound = MediaPlayer.create(this, R.raw.ting);
            if (correctSound != null) {
                correctSound.setVolume(1.0f, 1.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvScore = findViewById(R.id.tvScore);
        tvMessage = findViewById(R.id.tvMessage);
        btnReset = findViewById(R.id.btnReset);
        foodsContainer = findViewById(R.id.foodsContainer);
        scrollView = findViewById(R.id.scrollView);
        redBasket = findViewById(R.id.redBasket);
        yellowBasket = findViewById(R.id.yellowBasket);
        greenBasket = findViewById(R.id.greenBasket);
        purpleBasket = findViewById(R.id.purpleBasket);

        if (tvMessage != null) {
            tvMessage.setText("Drag the foods into the correct basket!");
        }

        setupFoodData();
        setupFoods();
        setupDropZones();

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetGame());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupFoodData() {
        for (int i = 0; i < foodNames.length; i++) {
            foodColors.put(foodNames[i], foodColorsList[i]);
        }
    }

    private void setupFoods() {
        if (foodsContainer == null) return;

        foodsContainer.removeAllViews();
        foodCards.clear();

        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < foodNames.length; i++) {
            indices.add(i);
        }

        Collections.shuffle(indices);

        for (int index : indices) {

            String foodName = foodNames[index];
            int imageId = foodImageIds[index];

            CardView foodCard = new CardView(this);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            200,
                            260);

            cardParams.setMargins(16, 16, 16, 16);

            foodCard.setLayoutParams(cardParams);
            foodCard.setCardBackgroundColor(Color.WHITE);
            foodCard.setRadius(20);
            foodCard.setCardElevation(8);
            foodCard.setTag(foodName);

            LinearLayout foodLayout = new LinearLayout(this);
            foodLayout.setOrientation(LinearLayout.VERTICAL);
            foodLayout.setGravity(android.view.Gravity.CENTER);
            foodLayout.setPadding(16, 16, 16, 16);

            ImageView foodImage = new ImageView(this);

            foodImage.setImageResource(imageId);

            LinearLayout.LayoutParams imageParams =
                    new LinearLayout.LayoutParams(
                            140,
                            140);

            foodImage.setLayoutParams(imageParams);
            foodImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

            TextView foodText = new TextView(this);

            foodText.setText(foodName);
            foodText.setTextSize(16);
            foodText.setTextColor(getColor(R.color.text_dark));
            foodText.setGravity(android.view.Gravity.CENTER);
            foodText.setTypeface(null, android.graphics.Typeface.BOLD);
            foodText.setMaxLines(2);

            foodText.setPadding(
                    0,
                    12,
                    0,
                    0
            );

            foodLayout.addView(foodImage);
            foodLayout.addView(foodText);

            foodCard.addView(foodLayout);

            final String finalFoodName = foodName;

            foodCard.setOnTouchListener((v, event) -> {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    android.content.ClipData data =
                            android.content.ClipData.newPlainText(
                                    "food",
                                    finalFoodName
                            );

                    View.DragShadowBuilder shadowBuilder =
                            new View.DragShadowBuilder(v);

                    v.startDragAndDrop(
                            data,
                            shadowBuilder,
                            v,
                            0
                    );

                    v.setVisibility(View.INVISIBLE);

                    return true;
                }

                return false;
            });

            foodsContainer.addView(foodCard);
            foodCards.add(foodCard);
        }

        Toast.makeText(
                this,
                foodCards.size() + " food items ready!",
                Toast.LENGTH_SHORT
        ).show();
    }
    private void setupDropZones() {
        if (redBasket != null) setDropTarget(redBasket, "RED");
        if (yellowBasket != null) setDropTarget(yellowBasket, "YELLOW");
        if (greenBasket != null) setDropTarget(greenBasket, "GREEN");
        if (purpleBasket != null) setDropTarget(purpleBasket, "PURPLE");
    }

    private void setDropTarget(CardView dropZone, String expectedColor) {
        dropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start();
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    return true;

                case DragEvent.ACTION_DROP:
                    android.content.ClipData.Item item = event.getClipData().getItemAt(0);
                    String foodName = item.getText().toString();
                    View draggedView = (View) event.getLocalState();

                    String foodColor = foodColors.get(foodName);

                    if (foodColor != null && foodColor.equals(expectedColor)) {
                        playCorrectSound();

                        if (draggedView != null && draggedView.getParent() != null) {
                            LinearLayout container = (LinearLayout) draggedView.getParent();
                            if (container != null) {
                                container.removeView(draggedView);
                                foodCards.remove(draggedView);
                                score += 10;
                                updateScore();
                                showCorrectAnimation(dropZone);
                                if (tvMessage != null) {
                                    tvMessage.setText("Great job! +10 points!");
                                    tvMessage.setTextColor(getColor(R.color.soft_blue));
                                }

                                if (foodCards.isEmpty()) {
                                    new Handler().postDelayed(() -> {
                                        showGameCompleteDialog();
                                    }, 500);
                                }
                            }
                        }
                    } else {
                        if (draggedView != null) {
                            draggedView.setVisibility(View.VISIBLE);
                            draggedView.animate()
                                    .translationX(0)
                                    .translationY(0)
                                    .setDuration(300)
                                    .start();
                        }
                        showWrongAnimation(dropZone);
                        showWrongFeedback();
                        if (tvMessage != null) {
                            tvMessage.setText("Oops! Wrong basket! Try again!");
                            tvMessage.setTextColor(getColor(R.color.brown));
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    View draggedViewEnd = (View) event.getLocalState();
                    if (draggedViewEnd != null && draggedViewEnd.getVisibility() == View.INVISIBLE) {
                        draggedViewEnd.setVisibility(View.VISIBLE);
                        draggedViewEnd.animate()
                                .translationX(0)
                                .translationY(0)
                                .setDuration(300)
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void playCorrectSound() {
        try {
            if (correctSound != null) {
                if (correctSound.isPlaying()) {
                    correctSound.pause();
                    correctSound.seekTo(0);
                }
                correctSound.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCorrectAnimation(CardView view) {
        if (view == null) return;
        int originalColor = view.getCardBackgroundColor().getDefaultColor();
        view.setCardBackgroundColor(Color.parseColor("#81C784"));
        new Handler().postDelayed(() -> {
            if (view != null) view.setCardBackgroundColor(originalColor);
        }, 200);

        view.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100)
                .withEndAction(() -> {
                    if (view != null) view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                })
                .start();
    }

    private void showWrongAnimation(CardView view) {
        if (view == null) return;
        view.animate()
                .translationX(15)
                .setDuration(50)
                .withEndAction(() -> {
                    if (view == null) return;
                    view.animate().translationX(-15).setDuration(50)
                            .withEndAction(() -> {
                                if (view == null) return;
                                view.animate().translationX(8).setDuration(50)
                                        .withEndAction(() -> {
                                            if (view == null) return;
                                            view.animate().translationX(-8).setDuration(50)
                                                    .withEndAction(() -> {
                                                        if (view != null) view.animate().translationX(0).setDuration(50).start();
                                                    }).start();
                                        }).start();
                            }).start();
                })
                .start();

        int originalColor = view.getCardBackgroundColor().getDefaultColor();
        view.setCardBackgroundColor(Color.parseColor("#EF5350"));
        new Handler().postDelayed(() -> {
            if (view != null) view.setCardBackgroundColor(originalColor);
        }, 300);
    }

    private void showWrongFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(150);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScore() {
        if (tvScore != null) {
            tvScore.setText("Score: " + score);
        }
    }

    private void resetGame() {
        score = 0;
        updateScore();
        if (tvMessage != null) {
            tvMessage.setText("Drag the foods into the correct basket!");
            tvMessage.setTextColor(getColor(R.color.light_brown));
        }
        setupFoods();
    }

    private void showGameCompleteDialog() {
        playCorrectSound();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_complete, null);

        VideoView congratulationsVideo = dialogView.findViewById(R.id.congratulationsVideo);

        // Load and play congratulations.mp4
        loadCongratulationsVideo(congratulationsVideo);

        TextView tvGameMessage = dialogView.findViewById(R.id.tvGameMessage);
        TextView tvScoreDisplay = dialogView.findViewById(R.id.tvScoreDisplay);
        Button playAgain = dialogView.findViewById(R.id.btnPlayAgain);
        Button exit = dialogView.findViewById(R.id.btnExit);

        if (tvGameMessage != null) {
            tvGameMessage.setText("You completed the game!");
        }
        if (tvScoreDisplay != null) {
            tvScoreDisplay.setText("Final Score: " + score);
        }

        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (playAgain != null) {
            playAgain.setOnClickListener(v -> {
                dialog.dismiss();
                resetGame();
            });
        }

        if (exit != null) {
            exit.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        }
    }

    private void loadCongratulationsVideo(VideoView videoView) {
        try {
            // Try to load congratulations.mp4 from raw folder
            int rawResourceId = getResources().getIdentifier("congratulations", "raw", getPackageName());

            if (rawResourceId != 0 && videoView != null) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                videoView.setVideoURI(videoUri);
                videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true); // Loop the video
                    videoView.start();
                });

                videoView.setOnErrorListener((mp, what, extra) -> {
                    // If video fails, hide the VideoView
                    videoView.setVisibility(View.GONE);
                    return true;
                });
            } else {
                // If congratulations.mp4 doesn't exist, hide the VideoView
                if (videoView != null) {
                    videoView.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (videoView != null) {
                videoView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (correctSound != null) {
            try {
                correctSound.release();
                correctSound = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}