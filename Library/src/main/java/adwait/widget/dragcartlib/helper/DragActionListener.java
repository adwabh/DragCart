package adwait.widget.dragcartlib.helper;

import android.support.v7.widget.RecyclerView;

/**
 * Created by adwait on 25/08/17.
 */

public interface DragActionListener extends OnStartDragListener {
    /**
     * Called when a view is requesting a stop of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStopDrag(RecyclerView.ViewHolder viewHolder); 
}
