package com.example.autimate;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class SmoothPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        // Simple fade and slide for smooth transition
        page.setAlpha(1 - Math.abs(position) * 0.3f);

        // Small scale effect for depth
        float scale = 0.95f + (1 - Math.abs(position)) * 0.05f;
        page.setScaleX(scale);
        page.setScaleY(scale);

        // Smooth translation
        page.setTranslationX(page.getWidth() * -position * 0.3f);
    }
}