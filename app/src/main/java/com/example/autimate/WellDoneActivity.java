package com.example.autimate;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class WellDoneActivity extends AppCompatActivity {

    private VideoView welldoneVideo;
    private TextView tvTitle, tvMessage, tvPointsEarned;
    private Button btnViewReward, btnBackToHome;

    // MediaPlayer for hooray sound
    private MediaPlayer hoorayMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_well_done);

        String routineTitle = getIntent().getStringExtra("routine_title");
        if (routineTitle == null) routineTitle = "routine";

        // Initialize views
        welldoneVideo = findViewById(R.id.welldoneVideo);
        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvPointsEarned = findViewById(R.id.tvPointsEarned);
        btnViewReward = findViewById(R.id.btnViewReward);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Load and play hooray.mp3 sound
        playHooraySound();

        // Load and play welldone.mp4
        loadWelldoneVideo();

        // Set values
        tvTitle.setText("Well Done!");
        tvMessage.setText("You've completed " + routineTitle + "!");
        tvPointsEarned.setText("+5 POINTS");

        // Set click listeners
        btnViewReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WellDoneActivity.this, RewardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WellDoneActivity.this, ChildHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void playHooraySound() {
        try {
            int rawResourceId = getResources().getIdentifier("hooray", "raw", getPackageName());

            if (rawResourceId != 0) {
                hoorayMediaPlayer = MediaPlayer.create(this, rawResourceId);
                if (hoorayMediaPlayer != null) {
                    hoorayMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            hoorayMediaPlayer.start();
                        }
                    });
                    hoorayMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // Release resources after playing
                            releaseHoorayMediaPlayer();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseHoorayMediaPlayer() {
        if (hoorayMediaPlayer != null) {
            try {
                if (hoorayMediaPlayer.isPlaying()) {
                    hoorayMediaPlayer.stop();
                }
                hoorayMediaPlayer.release();
                hoorayMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadWelldoneVideo() {
        try {
            // Try to load welldone.mp4 from raw folder
            int rawResourceId = getResources().getIdentifier("welldone", "raw", getPackageName());

            if (rawResourceId != 0) {
                String videoPath = "android.resource://" + getPackageName() + "/" + rawResourceId;
                Uri videoUri = Uri.parse(videoPath);

                welldoneVideo.setVideoURI(videoUri);
                welldoneVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    welldoneVideo.start();
                });

                welldoneVideo.setOnErrorListener((mp, what, extra) -> {
                    welldoneVideo.setVisibility(View.GONE);
                    return true;
                });
            } else {
                welldoneVideo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            welldoneVideo.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (welldoneVideo != null && welldoneVideo.isPlaying()) {
            welldoneVideo.pause();
        }
        // Release media player on pause
        releaseHoorayMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (welldoneVideo != null && !welldoneVideo.isPlaying()) {
            welldoneVideo.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (welldoneVideo != null) {
            welldoneVideo.stopPlayback();
        }
        releaseHoorayMediaPlayer();
    }
}