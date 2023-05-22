package com.example.recyclerview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.appchat.R
import com.example.appchat.model.User
import com.example.appchat.view.ChatActivity

class FavouriteUserAdapter(private val userList: ArrayList<User>, val context: Context) : RecyclerView.Adapter<FavouriteUserAdapter.FavouriteViewHolder>(){


    class FavouriteViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.txtNameUserFv)
        val imgUser : ImageView = itemView.findViewById(R.id.imgUserFv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val user = userList[position]
        holder.textView.text = user.name
        Glide.with(context)
            .load(user.profileImg)
            .placeholder(R.drawable.no_img)
            .into(holder.imgUser)
        holder.imgUser.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("urlAvatar", user.profileImg)
            intent.putExtra("statusUser", user.status)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return userList.size
    }

}



