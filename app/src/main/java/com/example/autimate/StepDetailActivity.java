package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView taskTitle, stepNumber, stepInstruction, tvStepProgress, tvNavigationHint;
    private ImageView btnPrev, btnNext, btnSpeak;
    private Button btnFinish;
    private CardView cardRoutine;
    private VideoView stepVideo;
    private ProgressBar videoProgressBar;
    private LinearLayout progressDots;

    private List<String> stepList = new ArrayList<>();
    private List<String> videoList = new ArrayList<>();
    private int currentStep = 0;
    private SharedPreferences progressPrefs;
    private String title;

    // TTS Variables
    private TextToSpeech textToSpeech;
    private boolean isTTSReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);

        progressPrefs = getSharedPreferences("ChildProgress", MODE_PRIVATE);
        title = getIntent().getStringExtra("task_title");

        String[] stepInstructions = getIntent().getStringArrayExtra("task_steps");
        String[] stepVideos = getIntent().getStringArrayExtra("task_videos");

        for (String step : stepInstructions) {
            stepList.add(step);
        }
        for (String video : stepVideos) {
            videoList.add(video);
        }

        // Initialize TTS
        initTTS();

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        taskTitle = findViewById(R.id.taskTitle);
        stepNumber = findViewById(R.id.stepNumber);
        stepInstruction = findViewById(R.id.stepInstruction);
        tvStepProgress = findViewById(R.id.tvStepProgress);
        tvNavigationHint = findViewById(R.id.tvNavigationHint);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnFinish = findViewById(R.id.btnFinish);
        cardRoutine = findViewById(R.id.cardRoutine);
        stepVideo = findViewById(R.id.stepVideo);
        videoProgressBar = findViewById(R.id.videoProgressBar);
        progressDots = findViewById(R.id.progressDots);

        taskTitle.setText(title);

        setupProgressDots();
        loadStep(currentStep);

        btnBack.setOnClickListener(v -> finish());

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                loadStep(currentStep);
                animateArrow(btnPrev);
            } else {
                animateShake(btnPrev);
                Toast.makeText(this, "This is the first step!", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < stepList.size() - 1) {
                currentStep++;
                loadStep(currentStep);
                animateArrow(btnNext);
            } else {
                animateShake(btnNext);
                Toast.makeText(this, "You've completed all steps! Click FINISH to complete.", Toast.LENGTH_LONG).show();
            }
        });

        // TTS Speaker Button Click Listener
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakCurrentStep();
            }
        });

        // ==================== UPDATED FINISH BUTTON WITH DATE SAVING ====================
        btnFinish.setOnClickListener(v -> {

            int completed =
                    progressPrefs.getInt(
                            "completedRoutines",
                            0);

            progressPrefs.edit()
                    .putInt(
                            "completedRoutines",
                            completed + 1)
                    .apply();

            // IMPORTANT: Save today's date for calendar tracking
            saveCompletedDate();

            // Add task progress to Reward system
            RewardActivity.addTaskProgress(
                    this,
                    title,
                    5
            );

            Intent intent =
                    new Intent(
                            StepDetailActivity.this,
                            WellDoneActivity.class);

            intent.putExtra(
                    "routine_title",
                    title);

            startActivity(intent);
            finish();

        });
        // ================================================================
    }

    // NEW METHOD: Save today's date to completedDates
    private void saveCompletedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = sdf.format(new Date());

        SharedPreferences rewardPrefs = getSharedPreferences("RewardPrefs", MODE_PRIVATE);
        String existingDates = rewardPrefs.getString("completedDates", "");

        // Check if today already exists
        if (!existingDates.contains(today)) {
            String newDates = existingDates.isEmpty() ? today : existingDates + "," + today;
            rewardPrefs.edit().putString("completedDates", newDates).apply();

            // Debug log
            android.util.Log.d("StepDetail", "Date saved: " + today);
            android.util.Log.d("StepDetail", "All dates: " + newDates);
        }
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(StepDetailActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                        isTTSReady = false;
                    } else {
                        isTTSReady = true;
                        textToSpeech.setSpeechRate(0.9f);
                        textToSpeech.setPitch(1.0f);
                    }
                } else {
                    Toast.makeText(StepDetailActivity.this, "TTS initialization failed", Toast.LENGTH_SHORT).show();
                    isTTSReady = false;
                }
            }
        });
    }

    private void speakCurrentStep() {
        if (!isTTSReady) {
            Toast.makeText(this, "Text-to-Speech is not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        String stepNumberText = "Step " + (currentStep + 1) + ": ";
        String instruction = stepInstruction.getText().toString();
        String messageToSpeak = stepNumberText + instruction;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(messageToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(messageToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }

        btnSpeak.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    btnSpeak.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void setupProgressDots() {
        progressDots.removeAllViews();
        for (int i = 0; i < stepList.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.progress_dot_inactive);
            progressDots.addView(dot);
        }
        updateProgressDots();
    }

    private void updateProgressDots() {
        for (int i = 0; i < progressDots.getChildCount(); i++) {
            View dot = progressDots.getChildAt(i);
            if (i <= currentStep) {
                dot.setBackgroundResource(R.drawable.progress_dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.progress_dot_inactive);
            }
        }
    }

    private void loadStep(int step) {
        stepNumber.setText("Step " + (step + 1) + ":");
        stepInstruction.setText(stepList.get(step));
        tvStepProgress.setText((step + 1) + "/" + stepList.size());
        updateProgressDots();

        if (step == 0) {
            btnPrev.setAlpha(0.5f);
        } else {
            btnPrev.setAlpha(1f);
        }

        if (step == stepList.size() - 1) {
            btnFinish.setVisibility(View.VISIBLE);
            tvNavigationHint.setText("Click FINISH to complete!");
        } else {
            btnFinish.setVisibility(View.GONE);
            tvNavigationHint.setText("Tap arrows to navigate");
        }

        loadVideo(videoList.get(step));
        cardRoutine.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void loadVideo(String videoFileName) {
        try {
            String fileName = videoFileName.replace(".mp4", "");
            int rawResourceId = getResources().getIdentifier(fileName, "raw", getPackageName());

            if (rawResourceId != 0) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                videoProgressBar.setVisibility(View.VISIBLE);
                stepVideo.setVideoURI(videoUri);

                stepVideo.setOnPreparedListener(mp -> {
                    videoProgressBar.setVisibility(View.GONE);
                    mp.setLooping(true);
                    stepVideo.start();
                });

                stepVideo.setOnErrorListener((mp, what, extra) -> {
                    videoProgressBar.setVisibility(View.GONE);
                    return true;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            videoProgressBar.setVisibility(View.GONE);
        }
    }

    private void animateArrow(ImageView arrow) {
        arrow.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() -> {
                    arrow.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                })
                .start();
    }

    private void animateShake(ImageView view) {
        view.animate()
                .translationX(10)
                .setDuration(50)
                .withEndAction(() -> {
                    view.animate().translationX(-10).setDuration(50)
                            .withEndAction(() -> {
                                view.animate().translationX(5).setDuration(50)
                                        .withEndAction(() -> {
                                            view.animate().translationX(-5).setDuration(50)
                                                    .withEndAction(() -> {
                                                        view.animate().translationX(0).setDuration(50).start();
                                                    }).start();
                                        }).start();
                            }).start();
                })
                .start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepVideo != null && stepVideo.isPlaying()) {
            stepVideo.pause();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepVideo != null && !stepVideo.isPlaying()) {
            stepVideo.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stepVideo != null) {
            stepVideo.stopPlayback();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}