package com.example.autimate;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MixAndMatchGameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView tvScore, tvMatches, tvMessage;
    private Button btnReset;

    private List<CardItem> cards;
    private int firstSelected = -1, secondSelected = -1, score = 0, matches = 0;
    private boolean isChecking = false;
    private boolean isGameComplete = false;

    private Vibrator vibrator;
    private MediaPlayer correctSound;

    private final String[] colorNames = {"RED", "BLUE", "GREEN", "YELLOW", "ORANGE", "PURPLE"};
    private final int[] colorValues = {0xFFFF5722, 0xFF2196F3, 0xFF4CAF50, 0xFFFFEB3B, 0xFFFF9800, 0xFF9C27B0};
    private final String[] emojis = {"🔴", "🔵", "🟢", "🟡", "🟠", "🟣"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_and_match_game);

        try {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            correctSound = MediaPlayer.create(this, R.raw.ting);
            if (correctSound != null) {
                correctSound.setVolume(0.8f, 0.8f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gridLayout = findViewById(R.id.gridLayout);
        tvScore = findViewById(R.id.tvScore);
        tvMatches = findViewById(R.id.tvMatches);
        tvMessage = findViewById(R.id.tvMessage);
        btnReset = findViewById(R.id.btnReset);

        if (tvMessage != null) {
            tvMessage.setText("Match the colour!");
        }

        setupGame();

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetGame());
        }
    }

    private void setupGame() {
        if (gridLayout == null) return;

        cards = new ArrayList<>();
        for (int i = 0; i < colorNames.length; i++) {
            cards.add(new CardItem(i, colorNames[i], false, colorValues[i], emojis[i]));
            cards.add(new CardItem(i, "", true, colorValues[i], emojis[i]));
        }
        Collections.shuffle(cards);
        gridLayout.removeAllViews();

        int cardWidth = getResources().getDisplayMetrics().widthPixels / 3 - 32;
        for (int i = 0; i < cards.size(); i++) {
            CardItem card = cards.get(i);
            View cardView;

            try {
                cardView = getLayoutInflater().inflate(R.layout.item_game_card, null);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            cardView.setLayoutParams(new GridLayout.LayoutParams());
            ((GridLayout.LayoutParams) cardView.getLayoutParams()).width = cardWidth;
            ((GridLayout.LayoutParams) cardView.getLayoutParams()).height = cardWidth;
            ((GridLayout.LayoutParams) cardView.getLayoutParams()).setMargins(8, 8, 8, 8);

            TextView tvContent = cardView.findViewById(R.id.tvContent);
            View colorView = cardView.findViewById(R.id.colorView);
            TextView tvEmoji = cardView.findViewById(R.id.tvEmoji);

            if (card.isColorCard) {
                if (colorView != null) {
                    colorView.setVisibility(View.VISIBLE);
                }
                if (tvContent != null) {
                    tvContent.setVisibility(View.GONE);
                }
                if (tvEmoji != null) {
                    tvEmoji.setVisibility(View.GONE);
                }
                if (colorView != null) {
                    colorView.setBackgroundColor(card.colorValue);
                }
            } else {
                if (colorView != null) {
                    colorView.setVisibility(View.GONE);
                }
                if (tvContent != null) {
                    tvContent.setVisibility(View.VISIBLE);
                    tvContent.setText(card.displayText);
                }
                if (tvEmoji != null) {
                    tvEmoji.setVisibility(View.VISIBLE);
                    tvEmoji.setText(card.emoji);
                }
            }

            final int pos = i;
            cardView.setOnClickListener(v -> {
                if (!isGameComplete && !isChecking) {
                    onCardClick(pos);
                }
            });
            card.cardView = cardView;
            gridLayout.addView(cardView);
        }

        firstSelected = -1;
        secondSelected = -1;
        isChecking = false;
        isGameComplete = false;
        score = 0;
        matches = 0;
        updateUI();
    }

    private void onCardClick(int pos) {
        if (isChecking || isGameComplete) return;

        CardItem card = cards.get(pos);
        if (card.isMatched) return;

        if (firstSelected == -1) {
            firstSelected = pos;
            highlight(pos, true);
            return;
        }

        if (firstSelected == pos) {
            highlight(firstSelected, false);
            firstSelected = -1;
            return;
        }

        secondSelected = pos;
        highlight(secondSelected, true);
        checkMatch();
    }

    private void checkMatch() {
        isChecking = true;

        CardItem card1 = cards.get(firstSelected);
        CardItem card2 = cards.get(secondSelected);
        boolean isMatch = (card1.pairId == card2.pairId);

        new Handler().postDelayed(() -> {
            try {
                if (isMatch) {
                    playCorrectSound();

                    card1.isMatched = true;
                    card2.isMatched = true;
                    matches++;
                    score += 10;
                    updateUI();

                    animateSuccess(card1.cardView);
                    animateSuccess(card2.cardView);
                    clearSelection();

                    if (matches == colorNames.length) {
                        isGameComplete = true;
                        showGameCompleteDialog();
                    }
                } else {
                    vibrateWrong();
                    showWrongFeedback();

                    highlight(firstSelected, false);
                    highlight(secondSelected, false);
                    clearSelection();

                    animateShake(card1.cardView);
                    animateShake(card2.cardView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                clearSelection();
            }
            isChecking = false;
        }, 600);
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

    private void vibrateWrong() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showWrongFeedback() {
        if (tvMessage != null) {
            tvMessage.setText("❌ Wrong match! Try again!");
            tvMessage.setTextColor(getColor(R.color.brown));
            new Handler().postDelayed(() -> {
                if (tvMessage != null) {
                    tvMessage.setText("Match the colour!");
                    tvMessage.setTextColor(getColor(R.color.light_brown));
                }
            }, 1000);
        }
    }

    private void highlight(int pos, boolean highlight) {
        try {
            View cardView = cards.get(pos).cardView;
            if (cardView != null) {
                cardView.setBackgroundResource(highlight ? R.drawable.card_highlight : R.drawable.card_background);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateSuccess(View v) {
        if (v == null) return;
        try {
            ScaleAnimation anim = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(200);
            anim.setRepeatCount(1);
            anim.setRepeatMode(Animation.REVERSE);
            v.startAnimation(anim);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateShake(View v) {
        if (v == null) return;
        try {
            v.animate()
                    .translationX(20)
                    .setDuration(50)
                    .withEndAction(() -> {
                        if (v != null) {
                            v.animate().translationX(-20).setDuration(50)
                                    .withEndAction(() -> {
                                        if (v != null) {
                                            v.animate().translationX(10).setDuration(50)
                                                    .withEndAction(() -> {
                                                        if (v != null) {
                                                            v.animate().translationX(-10).setDuration(50)
                                                                    .withEndAction(() -> {
                                                                        if (v != null) {
                                                                            v.animate().translationX(0).setDuration(50).start();
                                                                        }
                                                                    }).start();
                                                        }
                                                    }).start();
                                        }
                                    }).start();
                        }
                    }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearSelection() {
        firstSelected = -1;
        secondSelected = -1;
    }

    private void updateUI() {
        if (tvScore != null) {
            tvScore.setText("Score: " + score);
        }
        if (tvMatches != null) {
            tvMatches.setText("Matches: " + matches + "/" + colorNames.length);
        }
    }

    private void resetGame() {
        isGameComplete = false;
        setupGame();
        if (tvMessage != null) {
            tvMessage.setText("Game reset! Try again!");
            tvMessage.setTextColor(getColor(R.color.light_brown));
            new Handler().postDelayed(() -> {
                if (tvMessage != null) {
                    tvMessage.setText("Match the colour!");
                }
            }, 1500);
        }
    }

    private void showGameCompleteDialog() {
        try {
            playCorrectSound();

            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_game_complete);
            dialog.setCancelable(false);

            VideoView congratulationsVideo = dialog.findViewById(R.id.congratulationsVideo);

            // Load and play congratulations.mp4
            loadCongratulationsVideo(congratulationsVideo);

            TextView tvGameMessage = dialog.findViewById(R.id.tvGameMessage);
            TextView tvScoreDisplay = dialog.findViewById(R.id.tvScoreDisplay);
            Button playAgain = dialog.findViewById(R.id.btnPlayAgain);
            Button exit = dialog.findViewById(R.id.btnExit);

            if (tvGameMessage != null) {
                tvGameMessage.setText("You completed the game!");
            }
            if (tvScoreDisplay != null) {
                tvScoreDisplay.setText("🎯 Score: " + score);
            }

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

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            resetGame();
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

    private static class CardItem {
        int pairId;
        String displayText;
        boolean isColorCard;
        int colorValue;
        String emoji;
        boolean isMatched = false;
        View cardView;

        CardItem(int pairId, String displayText, boolean isColorCard, int colorValue, String emoji) {
            this.pairId = pairId;
            this.displayText = displayText;
            this.isColorCard = isColorCard;
            this.colorValue = colorValue;
            this.emoji = emoji;
        }
    }
}