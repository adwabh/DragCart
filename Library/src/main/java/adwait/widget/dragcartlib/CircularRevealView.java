package adwait.widget.dragcartlib;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by adwait on 27/07/17.
 */

public class CircularRevealView extends View {

    protected static final int ANIMATION_DURATION = 300;

    protected final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    protected Paint paint = null;
    protected float expandFraction = 0;
    protected float centerX, centerY;
    private View anchor;

    public CircularRevealView(Context context) {
        super(context);
        init(context, null, -1);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.black));
    }

    public CircularRevealView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public CircularRevealView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircularRevealView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        float radius = (float) Math.sqrt(cx * cx + cy * cy) * expandFraction;
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public Animator expand() {
        return animateExpandFraction(0.1f, 1);
    }

    public Animator expand(float from, float to) {
        if (from < to) {
            return animateExpandFraction(from, to);
        }
        return expand();
    }

    public Animator contract() {
        return animateExpandFraction(1, 0.1f);
    }

    public Animator contract(float from, float to) {
        if (from > to) {
            return animateExpandFraction(from, to);
        }
        return contract();
    }

    private Animator animateExpandFraction(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(INTERPOLATOR);

        animator.addUpdateListener(updateListener);
        return animator;
    }

    protected ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setExpandFraction((float) animation.getAnimatedValue());
        }
    };

    public void setExpandFraction(float expandFraction) {
        this.expandFraction = expandFraction;
        invalidate();
    }

    public void setCenter(float x, float y) {
        centerX = x;
        centerY = y;
        invalidate();
    }

    public void setAnchor(View view) {
        this.anchor = view;
    }

    public float getAnchorX() {
        return centerX;
    }

    public float getAnchorY() {
        return centerY;
    }

    private static class ViewObserver implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {

        }
    }

}
