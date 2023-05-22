package com.example.appchat.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.`interface`.BottomDialogFragment
import com.example.appchat.adapter.MessageAdapter
import com.example.appchat.databinding.ActivityChatBinding
import com.example.appchat.model.Message
import com.example.appchat.model.User
import com.example.appchat.notification.SendNotificationTask
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var listUserChat: ArrayList<User>
    private lateinit var listMessage : ArrayList<Message>
    private lateinit var mesAdapter: MessageAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var savecurrentTime: String? = null
    private  var savecurrentDate:String? = null
    var dialog:ProgressDialog? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var downloadUri: String = "No avatar"

        val name = intent.getStringExtra("name")
        val statusUser = intent.getStringExtra("statusUser")
        Log.d("StatusUser", statusUser.toString())
        binding.txtNameUser.text = name
        binding.txtStatusUser.text = statusUser

        firebaseAuth = FirebaseAuth.getInstance()
        val receiverId = intent.getStringExtra("uid")
        val senderUid = firebaseAuth.currentUser?.uid!!
        database = FirebaseDatabase.getInstance().reference

        // load images recipient's
        val urlAvatar = intent.getStringExtra("urlAvatar")

        Glide.with(this)
            .load(urlAvatar)
            .placeholder(R.drawable.no_avatar)
            .error(R.drawable.no_avatar)
            .circleCrop()
            .into(binding.receiverAvatar)

        senderRoom = receiverId + senderUid
        receiverRoom = senderUid + receiverId

        binding.imgBack.setOnClickListener { finish() }
        val recyclerListMessage = findViewById<RecyclerView>(R.id.listMessage)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        recyclerListMessage.layoutManager = layoutManager
        listMessage = ArrayList()

        mesAdapter = MessageAdapter(listMessage, this)
        recyclerListMessage.adapter = mesAdapter

        val calendar = Calendar.getInstance()

        val currentDate = SimpleDateFormat("dd/MM/yyyy")

        val currentTime = SimpleDateFormat("hh:mm a")

        database.child("chats").child(senderRoom!!).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listMessage.clear()
                    for (post in snapshot.children){
                        val message = post.getValue(Message::class.java)
                        Log.d("ChatActivity", "for single")
                        if (message != null) {
                            listMessage.add(message)
                        }
                    }
                    mesAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatActivity", error.toException().toString())
                }
            })

        database.child("chats").child(senderRoom!!).child("messages")
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null) {
                        runOnUiThread {
                            try {
                                listMessage.add(message)
                                mesAdapter.notifyDataSetChanged()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        recyclerListMessage.smoothScrollToPosition(recyclerListMessage?.adapter!!.itemCount)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatActivity", error.toException().toString())
                }

            })


        binding.btSendMessage.setOnClickListener {

            Log.d("ChatActivity", currentTime.format(calendar.time).toString())
            Log.d("ChatActivity", "click send Message")
            val txtMessage = binding.edMessage.text.toString()
            Log.d("sendmessage", currentTime.format(calendar.time).toString())
            val messageObject = Message(txtMessage, "dasda",currentTime.format(calendar.time).toString(), senderUid, "dasda")

            binding.edMessage.setText("")
            val randomKey = database!!.push().key
//            val lastMessageObj = HashMap<String, Any>()send
//
//            lastMessageObj["lastMessage"] = messageObject.message!!
//            lastMessageObj["lastMessageTime"] = date.time
//            database!!.child("chats").child(senderRoom!!)
//                .updateChildren(lastMessageObj)
//            database!!.child("chats").child(receiverRoom!!)
//                .updateChildren(lastMessageObj)
            database!!.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(messageObject).addOnSuccessListener {
                    database.child("chats").child(receiverRoom!!).child("messages")
                        .push()
                        .setValue(messageObject)

                }

            val deviceToken_ = "ccBfpTjERX-VFJDSZp87sf:APA91bGtEY6VGYmDPrSbTfRlTcC8ghhs53WbxG0Bi40Nqr0m75WbjuMd6N2JwwC6Fogn5SObbRHDp510iONOnGT0GqdlJlKC1LyBqULIgbHH-IPxUO4DqZWTvFb3N4xJBmVDFRLMTBJ9"
            val task = SendNotificationTask(deviceToken_, "Message notifi", txtMessage)
            task.execute()
            binding!!.edMessage.setText("")
        }
        val storageRef = Firebase.storage.reference
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image")
        dialog!!.setCancelable(false)




        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")

                //uploading images toChat
                val riversRef = storageRef.child("Img/${uri}")
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
                        val firePath = downloadUri

                        val date = Date()
                        val message = Message("photo", "addas", currentTime.format(calendar.time).toString(), senderUid, firePath)
                        binding!!.edMessage.setText("")
                        Log.d("SenderRoom", senderRoom!!)
                        database.child("chats")
                            .child(senderRoom!!)
                            .child("messages")
                            .push()
                            .setValue(message).addOnSuccessListener {
                                database!!.child("chats")
                                    .child(receiverRoom!!)
                                    .child("messages")
                                    .push()
                                    .setValue(message)
                                    .addOnSuccessListener {  }
                            }



                    } else {
                        Log.d("MainActivity", "Loi lay uri img ")
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        binding.btCamera.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

//        val hander = Handler()
//
//        binding!!.edMessage.addTextChangedListener(object :TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                database.child("Presence")
//                    .child(senderUid)
//                    .setValue("typing...")
//                hander.removeCallbacksAndMessages(null)
//                hander.postDelayed(userStoppedTyping, 1000)
//            }
//            var userStoppedTyping = Runnable {
//                database.child("Presence")
//                    .child(senderUid!!)
//                    .child("online")
//            }
//
//        })
        binding.imageFavourite.setOnClickListener {
            Log.d("ChatActivity", "click favourite")
            database.child("users")
                .child(firebaseAuth.currentUser?.uid!!)
                .child("favourite")
                .push()
                .setValue(receiverId).addOnSuccessListener {
                    Log.d("ChatActivity", "add user to favorite success")
                } .addOnFailureListener {
                    Log.d("ChatActivity", "unsuccessful")
                }
        }

        binding.edMessage.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    Log.d("ChatActivity", "ko null")
                    binding.iconLike.visibility = View.GONE
                    binding.btSendMessage.visibility = View.VISIBLE
                }  else {
                    binding.iconLike.visibility = View.VISIBLE
                    binding.btSendMessage.visibility = View.GONE
                }
            }
        })
        val v = findViewById<View>(R.id.content)


        v.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.imgLocation.visibility = View.VISIBLE
                binding.btCamera.visibility = View.VISIBLE
                binding.imgVoice.visibility = View.VISIBLE
                binding.imgNvnext.visibility = View.GONE
                return@setOnTouchListener true
            }
            Log.d("ChatActivity", "adasdsas")
            return@setOnTouchListener false
        }

        // Lấy quyền truy cập vị trí từ người dùng
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.imgLocation.setOnClickListener {
            val  bottomSheetDialogFragment = BottomDialogFragment()
            bottomSheetDialogFragment.setOnButtonClickListener(object : BottomDialogFragment.OnButtonClickListener{
                override fun onButtonClicked() {
                    Log.d("ChatAc", "click location")
                    getLocation()

                }
            })
            bottomSheetDialogFragment.show(supportFragmentManager, "bottom_sheet_dialog")
        }





    }

    override fun onResume() {
        super.onResume()
        Log.d("ChatActivity", "onResume")
        binding.edMessage.setOnClickListener {
            Log.d("ChatActivity", "click ed")
            binding.imgLocation.visibility = View.GONE
            binding.btCamera.visibility = View.GONE
            binding.imgVoice.visibility = View.GONE
            binding.imgNvnext.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("ChatActivity", "onStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ChatActivity", "onPause")

    }

    fun getLocation() {

        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd/MM/yyyy")

        val currentTime = SimpleDateFormat("hh:mm a")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Kiểm tra quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Lấy vị trí hiện tại
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Xử lý vị trí ở đây
                    val messageObject = Message("location + latitude=${location!!.latitude}+longitude=${location.longitude}", "dasda",currentTime.format(calendar.time).toString(), firebaseAuth.currentUser!!.uid, "")
                    val randomKey = database!!.push().key
                    database!!.child("chats").child(senderRoom!!)
                        .child("messages")
                        .child(randomKey!!)
                        .setValue(messageObject).addOnSuccessListener {
                            database.child("chats").child(receiverRoom!!).child("messages")
                                .push()
                                .setValue(messageObject)

                        }

                }
                .addOnFailureListener { exception: Exception ->
                    // Xử lý lỗi ở đây
                }
        }
    }

}