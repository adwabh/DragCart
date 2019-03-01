package com.pb.learning.interaction.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.GridLayoutManager.DEFAULT_SPAN_COUNT
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pb.learning.interaction.R
import com.pb.learning.interaction.adapters.SampleRecyclerAdapter
import com.pb.learning.interaction.utils.SampleItemListHelper


/**
 * Created by Adwait Abhyankar on 1/6/2019.
 */
class SampleListFragment : Fragment() {
    private lateinit var touchHelper: SampleItemListHelper
    private lateinit var recyclerView: RecyclerView


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
        recyclerView = view.findViewById(R.id.recyclerView_content)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.layoutManager = GridLayoutManager(activity,2,GridLayoutManager.VERTICAL,false)//LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter = SampleRecyclerAdapter()
        touchHelper = SampleItemListHelper(recyclerView.parent as ViewGroup?)
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
    }
}