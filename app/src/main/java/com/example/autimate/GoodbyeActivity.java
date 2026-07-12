package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class GoodbyeActivity extends AppCompatActivity {

    private VideoView goodbyeVideo;
    private TextView tvGoodbyeMessage, tvGoodbyeSubtitle;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goodbye);

        goodbyeVideo = findViewById(R.id.goodbyeVideo);
        tvGoodbyeMessage = findViewById(R.id.tvGoodbyeMessage);
        tvGoodbyeSubtitle = findViewById(R.id.tvGoodbyeSubtitle);

        loadGoodbyeVideo();
        playGoodbyeSound();

        tvGoodbyeMessage.setText("See you again!");
        tvGoodbyeSubtitle.setText("You've logged out successfully");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                // Clear session preferences and reset theme to light mode
                clearSessionPreferences();

                // Reset theme to light mode before going to login
                resetThemeToLight();

                // Navigate to Login screen
                Intent intent = new Intent(GoodbyeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }

    private void loadGoodbyeVideo() {
        try {
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

    private void playGoodbyeSound() {
        try {
            int rawResourceId = getResources().getIdentifier("bye_sound", "raw", getPackageName());

            if (rawResourceId != 0) {
                mediaPlayer = MediaPlayer.create(this, rawResourceId);

                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();

                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        mp.release();
                        mediaPlayer = null;
                        return true;
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearSessionPreferences() {
        // ONLY clear ParentPrefs - preserve all child data
        getSharedPreferences("ParentPrefs", MODE_PRIVATE).edit().clear().apply();

        // DO NOT clear these - they contain the child's progress data:
        // - ChildPrefs (contains childId, childName, childAvatar)
        // - RewardPrefs (contains all rewards and points)
        // - RoutinePrefs (contains activities list)
        // - ChildProgress (contains progress data)
    }

    /**
     * Reset the app theme to light mode
     */
    private void resetThemeToLight() {
        // Save theme preference as light
        SharedPreferences themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        themePrefs.edit().putString("theme", "light").apply();

        // Apply light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (goodbyeVideo != null && goodbyeVideo.isPlaying()) {
            goodbyeVideo.pause();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goodbyeVideo != null && !goodbyeVideo.isPlaying()) {
            goodbyeVideo.start();
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (goodbyeVideo != null) {
            goodbyeVideo.stopPlayback();
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}