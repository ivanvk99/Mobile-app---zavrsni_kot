package com.example.travelcomp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.models.Board

open class StatusAdapter(private val context: Context, private var list: ArrayList<Board>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemClickListener: ItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //val textView = holder.itemView.findViewById<TextView>(R.id.tv_name)
        //val textcreatedby = holder.itemView.findViewById<TextView>(R.id.tv_created_by)
        val model = list[position]
        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))

            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text =
                "Created By : ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if (itemClickListener != null) {
                    itemClickListener!!.onClick(position, model)
                }
            }

            var total = 0.0
            var count = 0.0
            var average = 0.0
            for ((key, value) in model.feedback) {
                println("$key = $value")
                val feedback = model.feedback.getValue(key)
                total += feedback.rating.toFloat();
                count += 1;
                average = total / count;
            }
            holder.itemView.findViewById<TextView>(R.id.txt_rating).text = format(average)

        }
    }

    open fun format(`val`: Double): String? {
        return if (`val` == `val`.toLong().toDouble()) String.format(
            "%d",
            `val`.toLong()
        ) else String.format("%s", `val`)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setOnClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setBoardList(list: ArrayList<Board>) {
        this.list = list
        notifyDataSetChanged()
    }


    interface ItemClickListener {
        fun onClick(position: Int, model: Board)
    }


}