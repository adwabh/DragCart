package com.adwait.widget.dragcart.utils

import android.support.v7.widget.RecyclerView
import android.view.View

open class CartViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
    public var cartInfo = MoveToCartInfo(0f,0f,0f,0f,0,0,0,0)
}
