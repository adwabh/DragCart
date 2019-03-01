package com.adwait.widget.dragcart.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adwait.widget.dragcart.R
import com.adwait.widget.dragcart.adapters.SampleRecyclerAdapter
import com.adwait.widget.dragcart.utils.SampleItemAnimator
import com.adwait.widget.dragcart.utils.SampleItemListHelper
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * Created by Adwait Abhyankar on 1/6/2019.
 */
class SampleListFragment : Fragment() {
    private lateinit var touchHelper: SampleItemListHelper

    companion object {
        fun newInstance():SampleListFragment{
            return SampleListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private lateinit var itemAnimator: SampleItemAnimator

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView_content.layoutManager = GridLayoutManager(activity,2,GridLayoutManager.VERTICAL,false)//LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        recyclerView_content.adapter = SampleRecyclerAdapter()
        touchHelper = SampleItemListHelper(recyclerView_content.parent as ViewGroup?)
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView_content)

        itemAnimator = SampleItemAnimator(root,touchHelper)
        recyclerView_content.itemAnimator = itemAnimator
    }
}