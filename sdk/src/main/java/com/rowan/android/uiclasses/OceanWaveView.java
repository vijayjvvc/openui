package com.rowan.android.uiclasses;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;


import androidx.core.content.ContextCompat; // remove if you don't use AndroidX

/**
 * OceanWaveView
 * A rounded card whose bottom is filled with a live, animated ocean wave.
 * Centered inside the water: a bordered circle holding your icon, and a text label.
 *
 * XML example:
 *
 * <com.example.widgets.OceanWaveView
 *     android:layout_width="160dp"
 *     android:layout_height="240dp"
 *     app:waveColor="#3DB67F"
 *     app:waveLevel="0.35"
 *     app:waveAmplitude="8dp"
 *     app:waveSpeed="1"
 *     app:cornerRadius="60dp"
 *     app:icon="@drawable/ic_my_app"
 *     app:iconTint="#FFFFFF"
 *     app:iconBorderColor="#FFFFFF"
 *     app:iconBorderWidth="2dp"
 *     app:text="Continue"
 *     app:textColor="#FFFFFF" />
 *
 * If no icon is set, a default arrow (">") is drawn.
 */
public class OceanWaveView extends View {

    // ---- config (defaults match the reference design) ----
    private int waveColor = Color.parseColor("#3DB67F");
    private int containerColor = Color.TRANSPARENT;
    private float waveLevel = 0.35f;
    private float waveAmplitude;
    private float waveSpeed = 1f;
    private float cornerRadius;

    private Drawable icon;
    private int iconTint = Color.WHITE;
    private float iconSize;
    private float iconCircleSize;
    private int iconBorderColor = Color.WHITE;
    private float iconBorderWidth;

    private String text = "";
    private int textColor = Color.WHITE;
    private float textSize;

    // ---- drawing objects ----
    private final Paint containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path clipPath = new Path();
    private final Path frontWave = new Path();
    private final Path backWave = new Path();
    private final Path arrowPath = new Path();
    private final RectF rect = new RectF();

    // ---- animation ----
    private boolean running = true;
    private long lastNanos = 0L;
    private float phaseFront = 0f;
    private float phaseBack = 0f;
    private float displayedLevel;
    private ValueAnimator levelAnimator;
    private float density;

    public OceanWaveView(Context c) { this(c, null); }
    public OceanWaveView(Context c, AttributeSet a) { this(c, a, 0); }

    public OceanWaveView(Context c, AttributeSet a, int defStyle) {
        super(c, a, defStyle);
        density = c.getResources().getDisplayMetrics().density;

        // dimension defaults
        waveAmplitude = 8 * density;
        cornerRadius = 60 * density;
        iconSize = 22 * density;
        iconCircleSize = 52 * density;
        iconBorderWidth = 1.5f * density;
        textSize = 14 * density;

        if (a != null) {
            TypedArray t = c.obtainStyledAttributes(a, R.styleable.OceanWaveView, defStyle, 0);
            try {
                waveColor = t.getColor(R.styleable.OceanWaveView_waveColor, waveColor);
                waveLevel = t.getFloat(R.styleable.OceanWaveView_waveLevel, waveLevel);
                waveAmplitude = t.getDimension(R.styleable.OceanWaveView_waveAmplitude, waveAmplitude);
                waveSpeed = t.getFloat(R.styleable.OceanWaveView_waveSpeed, waveSpeed);
                cornerRadius = t.getDimension(R.styleable.OceanWaveView_cornerRadius, cornerRadius);
                containerColor = t.getColor(R.styleable.OceanWaveView_containerColor, containerColor);

                icon = t.getDrawable(R.styleable.OceanWaveView_icon);
                iconTint = t.getColor(R.styleable.OceanWaveView_iconTint, iconTint);
                iconSize = t.getDimension(R.styleable.OceanWaveView_iconSize, iconSize);
                iconCircleSize = t.getDimension(R.styleable.OceanWaveView_iconCircleSize, iconCircleSize);
                iconBorderColor = t.getColor(R.styleable.OceanWaveView_iconBorderColor, iconBorderColor);
                iconBorderWidth = t.getDimension(R.styleable.OceanWaveView_iconBorderWidth, iconBorderWidth);

                String s = t.getString(R.styleable.OceanWaveView_text);
                if (s != null) text = s;
                textColor = t.getColor(R.styleable.OceanWaveView_textColor, textColor);
                textSize = t.getDimension(R.styleable.OceanWaveView_textSize, textSize);
            } finally {
                t.recycle();
            }
        }

        displayedLevel = clamp01(waveLevel);
        if (icon != null) icon = icon.mutate();
        setClickable(true);
        applyPaints();
    }

    // ------------------------------------------------------------------
    // Public API (all optional - everything can be set from XML)
    // ------------------------------------------------------------------

    public void setWaveColor(int color) { waveColor = color; applyPaints(); invalidate(); }
    public void setContainerColor(int color) { containerColor = color; applyPaints(); invalidate(); }
    public void setWaveAmplitude(float px) { waveAmplitude = px; invalidate(); }
    public void setWaveSpeed(float speed) { waveSpeed = speed; }
    public void setCornerRadius(float px) { cornerRadius = px; rebuildClip(); invalidate(); }

    public void setIcon(Drawable d) { icon = d == null ? null : d.mutate(); invalidate(); }
    public void setIconResource(int resId) { setIcon(ContextCompat.getDrawable(getContext(), resId)); }
    public void setIconTint(int color) { iconTint = color; applyPaints(); invalidate(); }
    public void setIconBorderColor(int color) { iconBorderColor = color; applyPaints(); invalidate(); }
    public void setIconBorderWidth(float px) { iconBorderWidth = px; applyPaints(); invalidate(); }

    public void setText(String s) { text = s == null ? "" : s; invalidate(); }
    public void setTextColor(int color) { textColor = color; applyPaints(); invalidate(); }
    public void setTextSize(float px) { textSize = px; applyPaints(); invalidate(); }

    /** Set water level 0..1, optionally animated. */
    public void setWaveLevel(float level, boolean animate) {
        level = clamp01(level);
        waveLevel = level;
        if (levelAnimator != null) levelAnimator.cancel();
        if (!animate) { displayedLevel = level; invalidate(); return; }
        levelAnimator = ValueAnimator.ofFloat(displayedLevel, level);
        levelAnimator.setDuration(700);
        levelAnimator.setInterpolator(new DecelerateInterpolator());
        levelAnimator.addUpdateListener(a -> displayedLevel = (float) a.getAnimatedValue());
        levelAnimator.start();
    }

    public void setAnimating(boolean animate) {
        if (running == animate) return;
        running = animate;
        lastNanos = 0L;
        if (running) postInvalidateOnAnimation();
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastNanos = 0L;
        if (running) postInvalidateOnAnimation();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (levelAnimator != null) levelAnimator.cancel();
        lastNanos = 0L;
    }

    @Override protected void onVisibilityChanged(View changed, int visibility) {
        super.onVisibilityChanged(changed, visibility);
        lastNanos = 0L;
        if (visibility == VISIBLE && running) postInvalidateOnAnimation();
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    @Override protected void onMeasure(int wSpec, int hSpec) {
        setMeasuredDimension(
                resolveSize((int) (160 * density), wSpec),
                resolveSize((int) (240 * density), hSpec));
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        rebuildClip();
    }

    private void rebuildClip() {
        rect.set(0, 0, getWidth(), getHeight());
        float r = Math.min(cornerRadius, Math.min(getWidth(), getHeight()) / 2f);
        clipPath.reset();
        clipPath.addRoundRect(rect, r, r, Path.Direction.CW);
    }

    private void applyPaints() {
        containerPaint.setStyle(Paint.Style.FILL);
        containerPaint.setColor(containerColor);

        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setColor(waveColor);

        wavePaintBack.setStyle(Paint.Style.FILL);
        wavePaintBack.setColor(waveColor);
        wavePaintBack.setAlpha(110); // translucent back wave gives depth

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(iconBorderColor);
        borderPaint.setStrokeWidth(iconBorderWidth);

        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setColor(iconTint);
        arrowPaint.setStrokeWidth(2f * density);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);

        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    @Override protected void onDraw(Canvas canvas) {
        long now = System.nanoTime();
        float dt = (lastNanos == 0L) ? 0.016f : Math.min((now - lastNanos) / 1_000_000_000f, 0.05f);
        lastNanos = now;

        phaseFront += dt * 2.2f * waveSpeed;
        phaseBack -= dt * 1.5f * waveSpeed; // opposite direction

        final int w = getWidth();
        final int h = getHeight();
        if (w == 0 || h == 0) return;

        canvas.save();
        canvas.clipPath(clipPath);

        if (Color.alpha(containerColor) > 0) {
            canvas.drawRect(0, 0, w, h, containerPaint);
        }

        float waterTop = h * (1f - displayedLevel);
        buildWave(backWave, w, h, waterTop, waveAmplitude * 1.1f, phaseBack, 0.8f);
        buildWave(frontWave, w, h, waterTop, waveAmplitude, phaseFront, 1.2f);
        canvas.drawPath(backWave, wavePaintBack);
        canvas.drawPath(frontWave, wavePaint);

        canvas.restore();

        drawContent(canvas, w, h, waterTop);

        if (running && isShown()) postInvalidateOnAnimation();
    }

    private void buildWave(Path p, int w, int h, float baseY,
                           float amp, float phase, float cycles) {
        p.reset();
        final int steps = 48;
        float stepX = w / (float) steps;
        p.moveTo(0, h);
        for (int i = 0; i <= steps; i++) {
            float x = i * stepX;
            double a = (x / w) * Math.PI * 2.0 * cycles + phase;
            // two harmonics -> more natural water than a pure sine
            float y = baseY + amp * (float) (Math.sin(a) + 0.35 * Math.sin(2.3 * a + phase));
            p.lineTo(x, y);
        }
        p.lineTo(w, h);
        p.close();
    }

    private void drawContent(Canvas canvas, int w, int h, float waterTop) {
        boolean hasText = !TextUtils.isEmpty(text);
        float gap = 10 * density;

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textH = hasText ? (fm.descent - fm.ascent) : 0f;
        float groupH = iconCircleSize + (hasText ? gap + textH : 0f);

        // center the group inside the visible water area
        float areaTop = waterTop + waveAmplitude * 1.5f;
        float areaBottom = h - cornerRadius * 0.15f;
        float groupTop = areaTop + ((areaBottom - areaTop) - groupH) / 2f;
        if (groupTop < areaTop) groupTop = areaTop;

        float cx = w / 2f;
        float circleCy = groupTop + iconCircleSize / 2f;

        // centered text under the icon
        if (hasText) {
            float baseline = groupTop + iconCircleSize + gap - fm.ascent;
            canvas.drawText(text, cx, baseline, textPaint);
        }
        // bordered circle
        canvas.drawCircle(cx, circleCy, (iconCircleSize - iconBorderWidth) / 2f, borderPaint);

        // icon (custom drawable) or default arrow
        if (icon != null) {
            icon.setTint(iconTint);
            int half = (int) (iconSize / 2f);
            icon.setBounds((int) cx - half, (int) circleCy - half, (int) cx + half, (int) circleCy + half);
            icon.draw(canvas);
        } else {
            float s = iconSize * 0.28f;
            arrowPath.reset();
            arrowPath.moveTo(cx - s * 0.6f, circleCy - s * 1.4f);
            arrowPath.lineTo(cx + s * 0.8f, circleCy);
            arrowPath.lineTo(cx - s * 0.6f, circleCy + s * 1.4f);
            canvas.drawPath(arrowPath, arrowPaint);
        }

    }

    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
}