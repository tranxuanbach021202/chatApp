package com.example.appchat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.model.User
import com.example.appchat.view.ChatActivity
import com.example.appchat.view.StartActivity

class UserAdapter(private val UserChatList: ArrayList<User>, val context: Context) : RecyclerView.Adapter<UserAdapter.UserChatViewHolder>(){
    class UserChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName : TextView = itemView.findViewById(R.id.txtChatName)
        val textEmail : TextView = itemView.findViewById(R.id.txtEmail)
        val imgUser : ImageView = itemView.findViewById(R.id.imageAvatarUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_chat, parent, false)
        return UserChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserChatViewHolder, position: Int) {
        val user = UserChatList[position]
        holder.textName.text = user.name
        holder.textEmail.text = user.email
        Glide.with(context)
            .load(user.profileImg)
            .placeholder(R.drawable.no_avatar)
            .error(R.drawable.no_avatar)
            .circleCrop()
            .into(holder.imgUser)
        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("urlAvatar", user.profileImg)
            intent.putExtra("statusUser", user.status)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return UserChatList.size
    }

}
