package com.adwait.widget.dragcart.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adwait.widget.dragcart.R
import com.adwait.widget.dragcart.adapters.SampleRecyclerAdapter
import com.adwait.widget.dragcart.utils.*

import kotlinx.android.synthetic.main.fragment_list.*




/**
 * Created by Adwait Abhyankar on 1/6/2019.
 */
class SampleListFragment: Fragment() {
    lateinit var callback: () -> Unit
    lateinit var count: ViewGroup
    private lateinit var touchHelper: HalfwayItemListHelper
    lateinit var anchor:View

    companion object {
        fun newInstance():SampleListFragment{
            return SampleListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.adwait.widget.dragcart.R.layout.fragment_list,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private lateinit var itemAnimator: SampleItemAnimator

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView_content.layoutManager = StackLayoutManager(activity as Context,recyclerView_content)//LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
        recyclerView_content.adapter = SampleRecyclerAdapter()
//        touchHelper = ModifiedItemListHelper(activity!!, anchor, R.color.colorAccent, R.drawable.ic_shopping_cart, recyclerView_content)
        touchHelper = HalfwayItemListHelper(recyclerView_content, anchor, callback, count)
        recyclerView_content.itemAnimator = ModifiedItemListAnimator(recyclerView_content.parent as ViewGroup, touchHelper, object:SampleItemAnimator.UpdateListener{
            override fun onAnimateUpdate(animator: ValueAnimator) {

            }
        })
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView_content)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing)
        recyclerView_content.addItemDecoration(SpacesDecoration(spacingInPixels))
    }
}