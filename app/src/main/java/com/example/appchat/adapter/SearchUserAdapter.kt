package com.example.appchat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class SearchUserAdapter(private val userList: ArrayList<User>, val context: Context, private val listStatus: MutableMap<String, MutableList<String>>) : RecyclerView.Adapter<SearchUserAdapter.UserSearchViewHolder>(){
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    class UserSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameUser : TextView = itemView.findViewById(R.id.txtUserNameSearch)
        val imgUser : ImageView = itemView.findViewById(R.id.imageAvatarUserSearch)
        val btnAddUser : TextView = itemView.findViewById(R.id.btnAddFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchViewHolder {
        Log.d("SearchUser", "oncreate")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_friend, parent, false)
        return UserSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserSearchViewHolder, position: Int) {


        val user = userList[position]
        val status = listStatus.get(user.uid.toString())
        Log.d("SearchUser", status?.get(0).toString())
        if (status?.get(0).toString().equals("pending")) {
            holder.btnAddUser.text = "pending"
        } else if (status?.get(0).toString().equals("friend")) {
            holder.btnAddUser.text = "friend"
        } else if (status?.get(0).toString().equals("friendRequest")) {
            holder.btnAddUser.text = "accept friend"
        }
        holder.nameUser.text = user.name
        Glide.with(context)
            .load(user.profileImg)
            .placeholder(R.drawable.no_avatar)
            .error(R.drawable.no_avatar)
            .circleCrop()
            .into(holder.imgUser)
        database = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        holder.btnAddUser.setOnClickListener {
            val value = holder.btnAddUser.text
            if (value.equals("Friend request")) {
                Log.d("SearchUser", "click add user")
                val senderId = firebaseAuth.currentUser?.uid!!
                val status ="friendRequest"
                val data = hashMapOf(
                    "sender_id" to senderId,
                    "status" to status
                )
                val data_ = hashMapOf(
                    "sender_id" to user.uid,
                    "status" to "pending"
                )

                // ghi yeu cau ket ban
                database.child("friend_requests")
                    .child(user.uid!!)
                    .child(firebaseAuth.currentUser?.uid!!).setValue(data).addOnSuccessListener {
                        // ghi yeu cau gui loi moi ket ban
                        database.child("friend_requests")
                            .child(firebaseAuth?.uid!!)
                            .child(user.uid!!).setValue(data_)
                        Log.d("SearchUser", "send add user success")
                    } .addOnFailureListener {
                        Log.d("SearchUser", "send add user failure")
                    }
            } else if (value.equals("accept friend")){
                //update status user to friend
                holder.btnAddUser.text = "friend"
                val childUpdates = hashMapOf<String, Any>(
                    "status" to "friend"
                )
                database.child("friend_requests")
                    .child(firebaseAuth?.uid!!)
                    .child(user.uid!!).updateChildren(childUpdates)
                database.child("friend_requests")
                    .child(user.uid!!)
                    .child(firebaseAuth?.uid!!).updateChildren(childUpdates)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }


}