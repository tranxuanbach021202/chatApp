package com.example.appchat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.adapter.UserAdapter
import com.example.appchat.databinding.ActivityHomeBinding
import com.example.appchat.model.User
import com.example.recyclerview.FavouriteUserAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity()  {
    private lateinit var listFavourite: ArrayList<User>
    private lateinit var userList: ArrayList<User>
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: UserAdapter
    private lateinit var adapterFavourite: FavouriteUserAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var dialogSearch : BottomSheetDialog
    private var mapStatus = mutableMapOf<String, MutableList<String>>()
    private lateinit var status : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerViewFv = findViewById<RecyclerView>(R.id.myRecyclerViewUserFv)
        recyclerViewFv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        listFavourite = ArrayList()
        adapterFavourite = FavouriteUserAdapter(listFavourite, this)
        val hadsetFv = HashSet<String>()
        recyclerViewFv.adapter = adapterFavourite



        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        updateStatusUser("online")
        userList = ArrayList()
        adapter = UserAdapter(userList, this)
        val userRecyclerView  = findViewById<RecyclerView>(R.id.myRecyclerViewUser)
        userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        userRecyclerView.adapter = adapter



        database.child("users").child(firebaseAuth.currentUser?.uid!!).child("favourite")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (favourite in snapshot.children) {
                        val favourite_ = favourite.getValue(String::class.java)
                        hadsetFv.add(favourite_.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w( "Failed to read value.", error.toException())
                }
            })


        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val currentUser = postSnapshot.getValue(/* valueType = */ User::class.java)
                    Log.d("HomeActivity", currentUser!!.toString())
                    if(firebaseAuth.currentUser?.uid != currentUser?.uid) {
                        val user = User(currentUser.profileImg, currentUser.name, currentUser.email, currentUser.address, currentUser.phone, currentUser.uid, currentUser.status)
                        if (!userList.contains(user)) {
                            checkFriend(currentUser?.uid!!) { it ->
                                if (it == true) {
                                    Log.d("checkfriend", user.toString())
                                    Log.d("checkfriend", "friend")
                                    userList.add(user)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    Log.d("checkfriend", "not friend")
                                }
                            }

                        }

                    } else {
                        binding.txtUserName.text = currentUser?.name
                        Glide.with(this@HomeActivity)
                            .load(currentUser.profileImg)
                            .placeholder(R.drawable.no_avatar)
                            .error(R.drawable.no_avatar)
                            .circleCrop()
                            .into(binding.btnAvatar)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("HomeActivity", error.toException().toString())
            }

        })



        database.child("users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (favouriteUser in snapshot.children) {
                    val crUser = favouriteUser.getValue(User::class.java)
                    if (firebaseAuth.currentUser?.uid != crUser?.uid) {
                        if(hadsetFv.contains(crUser?.uid)) {
                            val user = User(crUser?.profileImg, crUser?.name, crUser?.email, crUser?.address, crUser?.phone, crUser?.uid)
                            if (!listFavourite.contains(user)) {
                                listFavourite.add(user)
                                adapterFavourite.notifyDataSetChanged()
                            }
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("HomeActivity", error.toException().toString())
            }
        })


        binding.btnAvatar.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
        val intentt = Intent(this, SearchUser::class.java)
        binding.homeSearch.setOnClickListener {
            startActivity(intentt)
        }




        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { it ->
                var token = ""
                if (it.isSuccessful) {
                    token = it.getResult()
                }

                Log.d("Token", token.toString())
            }

    }
    private fun checkFriend(uid : String, callback: (Boolean?) -> Unit) {
        database = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        status = ""
        database.child("friend_requests")
            .child(firebaseAuth?.uid!!)
            .child(uid)
            .child("status")
                .get().addOnSuccessListener { it ->
                val regex = "value = (\\w+)".toRegex()
                val matchResult = regex.find(it.toString())
                status = matchResult?.groupValues?.getOrNull(1).toString()
                Log.d("getfriend", status.toString())
                callback(status.toString().equals("friend"))

            } .addOnFailureListener {
                Log.e("getfriend", "error getting data", it)
            }
    }

    private fun updateStatusUser(status : String) {
        Log.d("UpdateStatus", firebaseAuth?.currentUser!!.uid)
        val childUpdates = hashMapOf<String, Any>(
            "status" to  status.toString()
        )
        database.child("users").child(firebaseAuth?.currentUser!!.uid).updateChildren(childUpdates)
    }

    override fun onDestroy() {
        Log.d("OnDess", firebaseAuth.currentUser!!.uid)
        updateStatusUser("offline")
        super.onDestroy()
    }






}