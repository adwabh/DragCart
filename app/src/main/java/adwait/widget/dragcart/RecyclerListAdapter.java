/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package adwait.widget.dragcart;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import adwait.widget.dragcartlib.helper.DragActionListener;
import adwait.widget.dragcartlib.helper.ItemTouchHelperAdapter;
import adwait.widget.dragcartlib.helper.ItemTouchHelperViewHolder;
import adwait.widget.dragcartlib.helper.OnStartDragListener;

import static android.R.attr.x;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;


/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter, Animator.AnimatorListener {

    private final List<String> mItems = new ArrayList<>();

    private final DragActionListener mDragStartListener;

    private int x,y;
    private float hypotenuse;

    public RecyclerListAdapter(Context context, DragActionListener dragStartListener) {
        mDragStartListener = dragStartListener;
        mItems.addAll(Arrays.asList(context.getResources().getStringArray(R.array.dummy_items)));
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view,mDragStartListener);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textView.setText(mItems.get(position));

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(MotionEventCompat.getActionMasked(event)){
                    case ACTION_DOWN:
                        mDragStartListener.onStartDrag(holder);
                        animateStartDrag(holder);
                        break;
                    case ACTION_UP:
                        mDragStartListener.onStopDrag(holder);
                        break;
                }
                return false;
            }
        });
    }

    private void animateStartDrag(final RecyclerView.ViewHolder viewHolder) {

        if (viewHolder instanceof ItemViewHolder) {
            final ItemViewHolder holder = (ItemViewHolder) viewHolder;
            holder.imageView_clipping.setVisibility(View.VISIBLE);
            Animator anim = holder.imageView_clipping.contract();
            anim.start();
        }

//        viewHolder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                y = viewHolder.itemView.getHeight() / 2;
//                x = viewHolder.itemView.getWidth() / 2;
//                hypotenuse = (float) Math.hypot(x - viewHolder.itemView.getLeft(),y - viewHolder.itemView.getTop());
//            }
//        });
//        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
//            /*int y = 100;//viewHolder.itemView.getHeight() / 2;
//            int x = 100;//viewHolder.itemView.getWidth() / 2;
//            float hypotenuse = (float) Math.hypot(x - viewHolder.itemView.getLeft(),y - viewHolder.itemView.getTop());*/
//            if (viewHolder instanceof ItemViewHolder) {
//                final ItemViewHolder holder = (ItemViewHolder) viewHolder;
//                holder.linear_layout_reveal.setVisibility(View.VISIBLE);
//                android.animation.Animator anim = ViewAnimationUtils.createCircularReveal(holder.linear_layout_reveal, x, y, hypotenuse, 0);
//                anim.setDuration(300);
//                anim.setInterpolator(new DecelerateInterpolator());
//                anim.addListener(new android.animation.Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(android.animation.Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(android.animation.Animator animator) {
//                        holder.linear_layout_reveal.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(android.animation.Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(android.animation.Animator animator) {
//
//                    }
//                });
//                //linear_layout_reveal.setBackgroundResource(R.color.menu_color2);
//
//                anim.start();
//            }
//        }


    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
//        Collections.swap(mItems, fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView textView;
        public final ImageView handleView;
        private final DragActionListener mListener;
        public final ClippingImageView imageView_clipping;
//        public LinearLayout linear_layout_reveal;

        public ItemViewHolder(View itemView,DragActionListener listener) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
//            linear_layout_reveal = (LinearLayout) itemView.findViewById(R.id.linear_layout_reveal);
            imageView_clipping = (ClippingImageView) itemView.findViewById(R.id.imageView_clipping);
            mListener = listener;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            //TODO:dismiss cart here
            mListener.onStopDrag(this);
        }
    }

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
}
