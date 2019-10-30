package com.adwait.widget.dragcart.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.adwait.widget.dragcart.R
import com.adwait.widget.dragcart.utils.CartViewHolder
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_card_new.*
import java.util.concurrent.ThreadLocalRandom


/**
 * Created by Adwait Abhyankar on 1/4/2019.
 */
class SampleRecyclerAdapter : RecyclerView.Adapter<SampleRecyclerAdapter.SampleViewHolder>() {
    val picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        var itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_new,parent,false);
        return SampleViewHolder(itemView,picasso)
    }

    override fun getItemCount(): Int {
        return STATIC_NUMBER
    }

    override fun onBindViewHolder(viewHolder: SampleViewHolder, position: Int) {
        var pos = position% names.size
        viewHolder.bind(imageIdS[pos])
        /*viewHolder.textView_name?.text = names[pos]
        viewHolder.textView_status?.text =  if(pos%2==0){ "Transferred"}else{"Pending"}
        viewHolder.textView_amount?.text = randomAmount()
        viewHolder.imageView_logo?.setImageResource(R.mipmap.ic_launcher)
        viewHolder.imageView_alert?.setImageResource(R.drawable.ic_notifications_black_24dp)
        viewHolder.textView_date?.setText(R.string.sample_date)*/

    }
    private fun randomAmount():String{
        return formatCurrency(ThreadLocalRandom.current().nextInt(1, 999999 + 1).toFloat(),
                "$")
    }

    private fun formatCurrency(number:Float,currency: String):String{
        var formated = formatDecimal(number)
        return "$currency $formated"
    }
    private fun formatDecimal(number:Float):String{
        var epsilon:Float = 0.004f
        return if(Math.abs(Math.round(number)- epsilon) < epsilon){
            String.format("%10.0f", number)
        }else{
            String.format("%10.2f", number)
        }
    }

    /**
     * public String formatDecimal(float number) {
    float epsilon = 0.004f; // 4 tenths of a cent
    if (Math.abs(Math.round(number) - number) < epsilon) {
    return String.format("%10.0f", number); // sdb
    } else {
    return String.format("%10.2f", number); // dj_segfault
    }
    }
     * */

    class SampleViewHolder(override val containerView: View, val picasso: Picasso) : CartViewHolder(containerView),LayoutContainer {

        fun bind(i: Int) {
            picasso.load(i).fit().into(imageView2)
        }
    }

    companion object {
        private const val STATIC_NUMBER: Int = 10
        private val names = arrayOf("Dribble","Pintrest","Behance","YouTube","Twitter","Facebook","LinkedIn","WhatsApp","Instagram","Medium")
        private val imageIdS = arrayOf(R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4,R.drawable.p5,R.drawable.p6,R.drawable.p7,R.drawable.p8,R.drawable.p9,R.drawable.p10)

    }
}