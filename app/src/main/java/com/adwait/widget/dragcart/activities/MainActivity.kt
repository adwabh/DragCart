package com.adwait.widget.dragcart.activities

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.adwait.widget.dragcart.R
import com.adwait.widget.dragcart.fragments.SampleListFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpFragment(floatingActionButton)
    }

    private fun setUpFragment(anchor: View) {
        var transaction = supportFragmentManager?.beginTransaction()
        with(transaction){
            var frament = SampleListFragment.newInstance()
            frament.anchor = anchor
            this?.replace(R.id.fragment_container,frament)
        this?.commit()
        }
    }
}
