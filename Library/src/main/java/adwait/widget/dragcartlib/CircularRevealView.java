package adwait.widget.dragcartlib;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import adwait.widget.dragcartlib.helper.ItemTouchHelperAdapter;
import adwait.widget.dragcartlib.helper.ItemTouchHelperViewHolder;
import adwait.widget.dragcartlib.utils.ContractHolder;

import static adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback.ALPHA_FULL;
import static adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback.ALPHA_MIN;

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
    private ViewObserver mViewObserver;

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
        if(mViewObserver==null){
            mViewObserver = new ViewObserver(anchor);
        }
        getViewTreeObserver().addOnGlobalLayoutListener(mViewObserver);
    }

    public float getAnchorX() {
        return centerX;
    }

    public float getAnchorY() {
        return centerY;
    }

    public void removeAnchor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(mViewObserver);
        }
    }

    private class ViewObserver implements ViewTreeObserver.OnGlobalLayoutListener {

        private final View anchor;
        private int height;
        private int width;
        private float centerX;
        private float centerY;
        private double radius;

        public ViewObserver(View anchor) {
            this.anchor = anchor;
        }

        @Override
        public void onGlobalLayout() {
            height = anchor.getHeight();
            width = anchor.getWidth();
            centerX = anchor.getX() + width/2;
            centerY = anchor.getY() + height/2;
            radius = width/2;
            CircularRevealView.this.setCenter(centerX,centerY);
        }
    }

    /**
     * Created by adwait on 09/11/17.
     */

    public static class CustomItemTouchHelper extends ItemTouchHelper.Callback {

        private final Paint paint;
        private final CircularRevealView circularRevealView;
        private ItemTouchHelperAdapter mAdapter;
        private Context mContext;
        private float translationFactor;
        private boolean highlighted;

        public CustomItemTouchHelper(ItemTouchHelperAdapter adapter, Context context, CircularRevealView circularRevealView) {
            this.mAdapter = adapter;
            this.mContext = context;
            this.circularRevealView = circularRevealView;
            mAdapter = adapter;
            paint = new Paint();
            paint.setColor(context.getResources().getColor(R.color.black));
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            // Set movement flags based on the layout manager
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            } else {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }

            // Notify the adapter of the move
            mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            // Notify the adapter of the dismissal
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Fade out the view as it is swiped out of the parent's bounds
                    //                final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    final float alpha = ALPHA_FULL - translationFactor > ALPHA_MIN ? ALPHA_FULL - translationFactor: ALPHA_MIN;
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                }
            }
        }

        @Override
        public void onChildDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (isCurrentlyActive) {
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                   if(viewHolder instanceof ContractHolder){
                       ContractHolder holder = (ContractHolder) viewHolder;
                       Rect bounds = new Rect();
                       viewHolder.itemView.getLocalVisibleRect(bounds);
                       Animator anim = null;
                       if (!highlighted) {
                           anim = holder.contract();
                           anim.addListener(new Animator.AnimatorListener() {
                               @Override
                               public void onAnimationStart(Animator animation) {
                               }

                               @Override
                               public void onAnimationEnd(Animator animation) {
                                   highlighted = true;
                               }

                               @Override
                               public void onAnimationCancel(Animator animation) {

                               }

                               @Override
                               public void onAnimationRepeat(Animator animation) {

                               }
                           });
                           anim.start();

                       }else {
                           int cx = viewHolder.itemView.getLeft() + viewHolder.itemView.getWidth() / 2;
                           int cy = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 2;
                           float alpha = ALPHA_FULL - translationFactor > ALPHA_MIN ? ALPHA_FULL - translationFactor : ALPHA_MIN;
                           //                paint.setAlpha(Math.round(alpha));
                           canvas.drawCircle(cx + dX, cy + dY, 90, paint);
                       }
                   }
                }
            }else {
                //move this towards the anchored view
                highlighted = false;
    //            int cx = viewHolder.itemView.getLeft() + viewHolder.itemView.getWidth() / 2;
    //            int cy = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 2;
    //            float alpha = ALPHA_FULL - translationFactor > ALPHA_MIN ? ALPHA_FULL - translationFactor : ALPHA_MIN;
    //            //                paint.setAlpha(Math.round(alpha));
                canvas.drawCircle(circularRevealView.getAnchorX(), circularRevealView.getAnchorY(), 90, paint);


            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                    itemViewHolder.onItemSelected();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(ALPHA_FULL);
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                // Tell the view holder it's time to restore the idle state
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemClear();
            }
        }
    }
}
