package com.example.appchat.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.appchat.R
import com.example.appchat.databinding.ActivityProfileBinding
import com.example.appchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView


class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var downloadUri: String = "No avatar"
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val storageRef = Firebase.storage.reference
        var user = User()
        val imgView : CircleImageView = findViewById(R.id.imgProfileAvatar)

        database.child("users").child(firebaseAuth?.uid!!).addValueEventListener( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(User::class.java)
                Log.d("Profile", post?.profileImg.toString())
                Glide.with(this@Profile)
                    .load(post?.profileImg)
                    .placeholder(R.drawable.no_avatar)
                    .error(R.drawable.no_avatar)
                    .into(imgView)
                user = User(post?.profileImg, post?.name, post?.email, "xuanbach", post?.phone, post?.uid, post?.status)
                downloadUri = post?.profileImg!!
                binding.etNameProfile.hint = post?.name
                binding.txtNameProfile.text = post?.name
                binding.etAddressProfile.hint = post?.address
                binding.etPhoneNumberProfile.hint = post?.phone
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Profile", error.toException().toString())
            }

        })

        getTotalFriend()
        getTotalFriendrequest()



        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.no_avatar)
                    .error(R.drawable.no_avatar)
                    .circleCrop()
                    .into(binding.imgProfileAvatar)
                //uploading images to
                val riversRef = storageRef.child("images/${uri}")
                val uploadTask = riversRef.putFile(uri)
                uploadTask.addOnFailureListener {
                    Log.d("MainActivity", "ko upload thanh cong")
                    Log.d("MainActivity", it.toString())
                } .addOnSuccessListener {
                    Log.d("MainActivity", "upload thanh cong")

                }
                val urlTask = uploadTask.continueWithTask { task ->
                    if(!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    riversRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        downloadUri = task.result.toString()
                    } else {
                        Log.d("MainActivity", "Loi lay uri img ")
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        binding.imgProfileAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etNameProfile.text.toString()
            val phone = binding.etPhoneNumberProfile.text.toString()
            val address = binding.etAddressProfile.text.toString()
            if (downloadUri == null) {

            }
            val userUpdate = User(downloadUri, name, user.email, address, phone, user.uid, user.status)
            saveProfile(userUpdate)
        }

        binding.arrowBackProfile.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            logOut()
        }

        val radioGroupGender = binding.rgGender

        // Lấy ID của RadioButton được chọn
        val selectedRadioButtonId = radioGroupGender.checkedRadioButtonId
        binding.rbMale.isChecked = true
        Log.d("ProfileGender", selectedRadioButtonId.toString())
    }
    private fun saveProfile(user: User) {
        val key = database.child("users").child(user?.uid!!).push().key
        if (key == null) {
            Log.w("Profile", "Couldn't get push key for posts")
            return
        }

        val post = User(user.profileImg, user.name, user.email, user.address, user.phone, user.uid, user.status)
        val postValues = post.toMap()
        val uid = user.uid
        val childUpdates = hashMapOf<String,Any>(
            "profileImg" to user.profileImg.toString(),
            "name" to user.name.toString(),
            "phone" to user.phone.toString(),
            "address" to user.address.toString()

        )
        database.child("users").child(uid).updateChildren(childUpdates)

    }

    private fun getTotalFriend(){

        firebaseAuth = FirebaseAuth.getInstance()
        Log.d("TotalFriend", firebaseAuth?.currentUser!!.uid)
        database.child("friend_requests").child(firebaseAuth?.currentUser!!.uid)
            .orderByChild("status").equalTo("friend").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    binding.totalFriend.text = count.toString() + " Friend"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun getTotalFriendrequest() {

        firebaseAuth = FirebaseAuth.getInstance()
        database.child("friend_requests").child(firebaseAuth?.currentUser!!.uid)
            .orderByChild("status").equalTo("friendRequest").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    Log.d("countFriend", count.toString())
                    binding.totalFriendRequest.text = count.toString() + " Friend requests"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun logOut() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("myapp", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove("isLogged")
        editor.apply()

        updateStatusUser()
        // Khởi tạo Intent để chuyển đổi sang màn hình đăng nhập
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun updateStatusUser() {
        Log.d("UpdateStatus", firebaseAuth?.currentUser!!.uid)
        val childUpdates = hashMapOf<String, Any>(
            "status" to  "offline"
        )
        database.child("users").child(firebaseAuth?.currentUser!!.uid).updateChildren(childUpdates)
    }





}