package com.example.appchat.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.adapter.SearchUserAdapter
import com.example.appchat.databinding.ActivitySearchUserBinding
import com.example.appchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchUser : AppCompatActivity() {
    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var listUser: ArrayList<User>
    private lateinit var listUser_ : ArrayList<User>
    private lateinit var adapter: SearchUserAdapter
    private lateinit var adapterSearch : SearchUserAdapter
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var mapStatus = mutableMapOf<String, MutableList<String>>()
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listUser = ArrayList()
        listUser_ = ArrayList()
        database = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        adapter = SearchUserAdapter(listUser, this, mapStatus)

        val userRecyclerView  = findViewById<RecyclerView>(R.id.recyclerUserSearch)
        userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        userRecyclerView.adapter = adapter

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val currentUser = postSnapshot.getValue(/* valueType = */ User::class.java)
                    Log.d("SearchUser", currentUser!!.toString())
                    if(firebaseAuth.currentUser?.uid != currentUser?.uid) {
                        val user = User(currentUser.profileImg, currentUser.name, currentUser.email, currentUser.address, currentUser.phone, currentUser.uid)
                        if (!listUser.contains(user)) {
                            listUser.add(user)
                            listUser_.add(user)
                            adapter.notifyDataSetChanged()
                        }

                    } else {

                    }




                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("HomeActivity", error.toException().toString())
            }

        })

        database.child("friend_requests").child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (post in snapshot.children) {
                    val post_ = post.value as Map<String, Any>
                    Log.d("SearchUser", post_.get("status").toString())
                    mapStatus.getOrPut(post_.get("sender_id").toString()) { mutableListOf() }.add(post_.get("status").toString())
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.d("SearchUser", "Error get status")
            }
        })



        binding.tvExit.setOnClickListener {
            finish()
        }

        binding.edSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("listUser", listUser_.toString())
                if (!s.isNullOrEmpty()) {
                    binding.suggestion.visibility = View.GONE
                    val searchResult = serchUser(s.toString(), listUser_)
                    listUser.clear()
                    listUser.addAll(searchResult)
                } else {
                    binding.suggestion.visibility = View.VISIBLE
                    Log.d("SearchUserEd", "null" + listUser_.toString())
                    listUser.clear()
                    listUser.addAll(listUser_)
                    Log.d("SearchUserEd", "null" + listUser.toString())
                }
                adapter.notifyDataSetChanged()
            }
        })




    }

    private fun serchUser(name: String, list : ArrayList<User>) : List<User> {
        val result = mutableListOf<User>()
        Log.d("SearchUserlist", list.toString())
        for (user in list) {
            if(user.name!!.contains(name, ignoreCase = true)) {
                result.add(user)
            }
        }

        return result

    }


}