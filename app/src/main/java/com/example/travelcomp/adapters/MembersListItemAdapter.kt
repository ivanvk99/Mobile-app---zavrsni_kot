package com.example.travelcomp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.models.User


open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    private var board: Board,
    private var user: User
) : RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_member_image))

            holder.itemView.findViewById<TextView>(R.id.tv_member_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_member_email).text = model.email
            val imageView = holder.itemView.findViewById<ImageView>(R.id.btn_popup) as ImageView

            if (board.userID == user.id && model.id != user.id || !user.user)
                imageView.visibility = View.VISIBLE
            else
                imageView.visibility = View.GONE

            imageView.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menuInflater.inflate(R.menu.menu_delete_member, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> {
                            val alertDialog = AlertDialog.Builder(context)
                            alertDialog.apply {
                                setTitle("Delete")
                                setMessage("Are you sure to delete this member?")
                                setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                                    board.members.remove(model.id)
                                    list.remove(model)
                                    FirestoreClass().deleteMember(
                                        context,
                                        board.documentId,
                                        board.members
                                    )
                                    notifyDataSetChanged()
                                }
                                setNegativeButton("No") { _, _ ->

                                }
                            }
                            var dialog = alertDialog.create()
                            dialog.show()
                        }
                    }
                    true
                }

                popup.show() //showing popup menu

            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
