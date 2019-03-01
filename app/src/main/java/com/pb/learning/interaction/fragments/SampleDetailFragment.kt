package com.pb.learning.interaction.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pb.learning.interaction.R


/**
 * Created by Adwait Abhyankar on 1/6/2019.
 */
class SampleDetailFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.fragment_detail,container,false)
        return view
    }
}