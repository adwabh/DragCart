package adwait.widget.dragcart;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import adwait.widget.dragcartlib.helper.ItemTouchHelperAdapter;
import adwait.widget.dragcartlib.helper.ItemTouchHelperViewHolder;
import adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback;

import static adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback.ALPHA_FULL;
import static adwait.widget.dragcartlib.helper.SimpleItemTouchHelperCallback.ALPHA_MIN;
import static android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags;

/**
 * Created by adwait on 09/11/17.
 */

public class CustomItemTouchHelper extends ItemTouchHelper.Callback {

    private final Paint paint;
    private ItemTouchHelperAdapter mAdapter;
    private Context mContext;
    private float translationFactor;

    public CustomItemTouchHelper(ItemTouchHelperAdapter adapter, Context context) {
        this.mAdapter = adapter;
        this.mContext = context;
        mAdapter = adapter;
        paint = new Paint();
        paint.setColor(context.getResources().getColor(adwait.widget.dragcartlib.R.color.black));
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
               if(viewHolder instanceof RecyclerListAdapter.ItemViewHolder){
                   RecyclerListAdapter.ItemViewHolder holder = (RecyclerListAdapter.ItemViewHolder) viewHolder;
                   Rect bounds = new Rect();
                   viewHolder.itemView.getLocalVisibleRect(bounds);
                   Animator anim = null;
                   if (bounds.contains(Math.round(dX),Math.round(dY))) {
                       anim = holder.imageView_clipping.contract();
                       anim.addListener(new Animator.AnimatorListener() {
                           @Override
                           public void onAnimationStart(Animator animation) {
                           }

                           @Override
                           public void onAnimationEnd(Animator animation) {
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
