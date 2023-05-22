package com.example.appchat.view

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.databinding.ActivityProfileEditBinding
import com.example.appchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class SetupProfileActivity : AppCompatActivity(){
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        var downloadUri: String = "No avatar"
        // START initialize_database_ref
        database = Firebase.database.reference

        // START storage_field_initialization
        val storageRef = Firebase.storage.reference

        // auth
        firebaseAuth = FirebaseAuth.getInstance()

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
//
//        dialog!!.setMessage("Updating profile")
//        dialog!!.setCancelable(false)

//        binding.imgAvatar.setOnClickListener{
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
//        }

        binding.btnNext.setOnClickListener {
            val name = binding.etName.text.toString()
            val phone = binding.etPhoneNumber.text.toString()
            val address = binding.etAddress.text.toString()

            setUpProfile(name, firebaseAuth.currentUser?.uid!!, firebaseAuth.currentUser?.email!!, address, phone, downloadUri)
            val intent = Intent(this, HomeActivity::class.java)
            finish()
            startActivity(intent)


        }

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
                    .into(binding.imgAvatar)

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
                        Log.d("MainActivity", "url : ${downloadUri}")
                    } else {
                        Log.d("MainActivity", "Loi lay uri img ")
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        binding.imgAvatar.setOnClickListener {
            // Launch the photo picker and allow the user to choose images and videos.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }



    }


    // add user to database
    private fun setUpProfile(name: String, uid: String, email: String, address: String, phone: String, imgUrl: String) {
        database = Firebase.database.reference
        Log.d("Setupprofile", name)
        Log.d("Setupprofile", email)
        Log.d("Setupprofile", address)
        Log.d("Setupprofile", phone)
        Log.d("Setupprofile", imgUrl)
        val user = User(imgUrl, name, email, address, phone, uid, "offline")
        database.child("users").child(uid).setValue(user).addOnSuccessListener {
            Log.d("SetupProfile", "Success")
        }
            .addOnFailureListener{
                Log.d("SetupProfile", "unsuccessful")
            }
    }

}



