package adwait.widget.dragcart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import static android.R.attr.centerX;
import static android.R.attr.centerY;
import static android.R.attr.visibility;

/**
 * Created by adwait on 08/11/17.
 */

public class ClippingImageView extends android.support.v7.widget.AppCompatImageView {

    protected static final int ANIMATION_DURATION = 300;

    protected final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private Paint mBackgroundPaint;
    private Paint mErasePaint;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int cX;
    private int cY;
    private double radius;
    private float expandFraction;
    private Bitmap mBitmap;
    private Paint foreground;

    public ClippingImageView(Context context) {
        super(context);
        init(context, null, -1);
    }

    public ClippingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public ClippingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){

        if(attrs!=null){
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ClippingImageView,
                    0, 0);
            try {
                mBackgroundColor = a.getColor(R.styleable.ClippingImageView_backgroundColor,Color.MAGENTA);
            } finally {
                a.recycle();
            }
        }

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setAlpha(0xFF);

        foreground = new Paint();
        foreground.setColor(Color.BLUE);

        mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mErasePaint.setAlpha(0xFF);
        mErasePaint.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = canvas.getWidth()/2;
        int cy = canvas.getHeight()/2;
        float radius = (float) Math.sqrt(cx*cx + cy*cy) * expandFraction;
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(mBackgroundColor);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBackgroundPaint);
        canvas.drawCircle(cx, cy, radius, mErasePaint);
    }

    public void setColor(int color){
        mBackgroundPaint.setColor(color);
    }

    public Animator expand(){
        return animateExpandFraction(0.1f, 1);
    }

    public Animator expand(float from, float to){
        if (from<to) {
            return animateExpandFraction(from, to);
        }
        return expand();
    }

    public Animator contract(){
        return animateExpandFraction(1, 0.1f);
    }

    public Animator contract(float from, float to){
        if (from>to) {
            return animateExpandFraction(from, to);
        }
        return contract();
    }

    private Animator animateExpandFraction(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(INTERPOLATOR);
        animator.addUpdateListener(updateListener);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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
}
