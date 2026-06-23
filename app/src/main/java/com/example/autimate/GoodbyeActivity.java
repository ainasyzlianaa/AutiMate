package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class GoodbyeActivity extends AppCompatActivity {

    private VideoView goodbyeVideo;
    private TextView tvGoodbyeMessage, tvGoodbyeSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goodbye);

        // Initialize views
        goodbyeVideo = findViewById(R.id.goodbyeVideo);
        tvGoodbyeMessage = findViewById(R.id.tvGoodbyeMessage);
        tvGoodbyeSubtitle = findViewById(R.id.tvGoodbyeSubtitle);

        // Load and play goodbye animation
        loadGoodbyeVideo();

        // Set messages
        tvGoodbyeMessage.setText("See you again! 👋");
        tvGoodbyeSubtitle.setText("You've logged out successfully");

        // After 3 seconds, navigate to MainActivity (Login screen)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Clear all SharedPreferences
                clearAllPreferences();

                // Navigate to Login screen
                Intent intent = new Intent(GoodbyeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3 seconds delay
    }

    private void loadGoodbyeVideo() {
        try {
            // Load bye.mp4 from raw folder
            int rawResourceId = getResources().getIdentifier("bye", "raw", getPackageName());

            if (rawResourceId != 0) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                goodbyeVideo.setVideoURI(videoUri);
                goodbyeVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    goodbyeVideo.start();
                });

                goodbyeVideo.setOnErrorListener((mp, what, extra) -> {
                    goodbyeVideo.setVisibility(View.GONE);
                    return true;
                });
            } else {
                goodbyeVideo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            goodbyeVideo.setVisibility(View.GONE);
        }
    }

    private void clearAllPreferences() {
        // Clear all SharedPreferences used in the app
        getSharedPreferences("ChildPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("ChildProgress", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("ParentPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("RewardPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("RoutinePrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (goodbyeVideo != null && goodbyeVideo.isPlaying()) {
            goodbyeVideo.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goodbyeVideo != null && !goodbyeVideo.isPlaying()) {
            goodbyeVideo.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (goodbyeVideo != null) {
            goodbyeVideo.stopPlayback();
        }
    }
}