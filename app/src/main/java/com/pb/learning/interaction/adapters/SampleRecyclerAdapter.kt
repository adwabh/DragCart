package com.pb.learning.interaction.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pb.learning.interaction.R
import java.util.concurrent.ThreadLocalRandom


/**
 * Created by Adwait Abhyankar on 1/4/2019.
 */
class SampleRecyclerAdapter : RecyclerView.Adapter<SampleRecyclerAdapter.SampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        var itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sample,parent,false);
        return SampleViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return STATIC_NUMBER
    }

    override fun onBindViewHolder(viewHolder: SampleViewHolder, position: Int) {
        var pos = position% names.size
        viewHolder.textView_name?.text = names[pos]
        viewHolder.textView_status?.text =  if(pos%2==0){ "Transferred"}else{"Pending"}
        viewHolder.textView_amount?.text = randomAmount()
        viewHolder.imageView_logo?.setImageResource(R.mipmap.ic_launcher)
        viewHolder.imageView_alert?.setImageResource(R.drawable.ic_notifications_black_24dp)
        viewHolder.textView_date?.setText(R.string.sample_date)

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

    class SampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView_name:TextView?=null
        var textView_amount_static:TextView?=null
        var textView_amount:TextView?=null
        var textView_date_static:TextView?=null
        var textView_date:TextView?=null
        var textView_status_static:TextView?=null
        var textView_status:TextView?=null
        
        var imageView_logo:ImageView?=null
        var imageView_alert:ImageView?=null

        init {
           textView_name = itemView.findViewById(R.id.textView_name) 
           textView_amount_static = itemView.findViewById(R.id.textView_amount_static) 
           textView_amount = itemView.findViewById(R.id.textView_amount) 
           textView_date_static = itemView.findViewById(R.id.textView_date_static) 
           textView_date = itemView.findViewById(R.id.textView_date) 
           textView_status_static = itemView.findViewById(R.id.textView_status_static)
           textView_status = itemView.findViewById(R.id.textView_status)
           imageView_alert = itemView.findViewById(R.id.imageView_alert)
           imageView_logo = itemView.findViewById(R.id.imageView_logo)
        }
    }

    companion object {
        private const val STATIC_NUMBER: Int = 10
        private val names = arrayOf("Dribble","Pintrest","Behance","YouTube","Twitter","Facebook","LinkedIn","WhatsApp","Instagram","Medium")

    }
}