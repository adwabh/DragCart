package com.adwait.widget.dragcart.activities

import android.opengl.Visibility
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.adwait.widget.dragcart.R
import com.adwait.widget.dragcart.fragments.SampleListFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView_cart_count.text = "1"
        setUpFragment(floatingActionButton,textView_cart_count)
    }

    private fun setUpFragment(anchor: View, textView_cart_count: TextView) {
        var transaction = supportFragmentManager?.beginTransaction()
        with(transaction){
            var fragment = SampleListFragment.newInstance().apply {
                callback = { this@MainActivity.count += 1; textView_cart_count.text = this@MainActivity.count.toString()} }
            fragment.count = count_layout
            fragment.anchor = anchor
            this?.replace(R.id.fragment_container,fragment)
        this?.commit()
        }

    }

    var a:Int = 0
    var b = 9
    var lambda : (Int,Int)->Unit = {a:Int , b:Int->a+b}

}
