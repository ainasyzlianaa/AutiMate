package com.example.autimate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class GameSelectionActivity extends AppCompatActivity {

    private CardView cardMixMatch, cardDragDrop;
    private TextView tvTitle, tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);

        // Find views
        cardMixMatch = findViewById(R.id.cardMixMatch);
        cardDragDrop = findViewById(R.id.cardDragDrop);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        // Set texts
        if (tvTitle != null) {
            tvTitle.setText("Let's Play!");
        }
        if (tvSubtitle != null) {
            tvSubtitle.setText("Choose your favourite game");
        }

        // Mix & Match Game Card Click
        if (cardMixMatch != null) {
            cardMixMatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GameSelectionActivity.this, MixAndMatchGameActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Drag & Drop Game Card Click
        if (cardDragDrop != null) {
            cardDragDrop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GameSelectionActivity.this, DragDropGameActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}