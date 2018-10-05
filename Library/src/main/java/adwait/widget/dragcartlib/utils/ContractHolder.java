package adwait.widget.dragcartlib.utils;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import adwait.widget.dragcartlib.helper.ItemTouchHelperViewHolder;

public abstract class ContractHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
    public ContractHolder(View itemView) {
        super(itemView);
    }

    public abstract Animator contract();

    public abstract Animator expand();
}
