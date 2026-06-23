package com.example.autimate;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class StepPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        // Fade animation
        page.setAlpha(1 - Math.abs(position));

        // Scale animation
        float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
        page.setScaleX(scale);
        page.setScaleY(scale);

        // Rotation animation for 3D effect
        page.setRotationY(position * 30);

        // Translation animation
        if (position < -1) {
            page.setTranslationX(-page.getWidth());
        } else if (position <= 1) {
            page.setTranslationX(page.getWidth() * -position);
        } else {
            page.setTranslationX(page.getWidth());
        }

        // Z-order for better effect
        if (position <= 0) {
            page.setTranslationZ(0);
        } else {
            page.setTranslationZ(-Math.abs(position));
        }
    }
}