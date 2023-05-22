package com.example.appchat.adapter

import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appchat.R
import com.example.appchat.databinding.ItemRecriverMessageBinding
import com.example.appchat.databinding.ItemSendMessageBinding
import com.example.appchat.model.Message
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val messages: ArrayList<Message>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_SEND = 1
    val ITEM_RECEIVER = 2
    private lateinit var firebaseAuth: FirebaseAuth
    inner class SendMessageViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage = itemView.findViewById<TextView>(R.id.txtsend_message)
        val sendImg = itemView.findViewById<ImageView>(R.id.send_img)
        val sendLocation = itemView.findViewById<LinearLayout>(R.id.share_location_send)
        val sendMapLocation = itemView.findViewById<Button>(R.id.btnViewLocationS)
        val sendTimeMessage = itemView.findViewById<TextView>(R.id.sendTimeMessage)
        val txtNameShare = itemView.findViewById<TextView>(R.id.textNameShare)
        val cardImg = itemView.findViewById<CardView>(R.id.cardImg)


        init {
            sendMessage.visibility = View.GONE
            cardImg.visibility = View.GONE
            sendLocation.visibility = View.GONE
            sendMapLocation.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val data = messages[position]
                    val regex = "latitude=(-?(\\d+)\\.(\\d+))".toRegex()
                    val regex2 = "longitude=(-?(\\d+)\\.(\\d+))".toRegex()
                    val matchResult = regex.find(data.message.toString())
                    val matchResult2 = regex2.find(data.message.toString())
                    val latitude = matchResult?.groupValues?.getOrNull(1)
                    val longitude = matchResult2?.groupValues?.getOrNull(1)
                    val label = "Vị trí hiện tại"
                    val uri = "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
                    Log.d("itemview", latitude.toString())
                    // Xử lý sự kiện nhấn vào button ở đây
                    openGoogleMaps(uri)

                }
            }
        }

    }

    inner class ReceiverMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiverMessage = itemView.findViewById<TextView>(R.id.txtreceiver_message)
        val receiverImg = itemView.findViewById<ImageView>(R.id.receiver_img)
        val receiverLocation = itemView.findViewById<LinearLayout>(R.id.share_location_receiver)
        val receiverMapLocation = itemView.findViewById<Button>(R.id.btnViewLocationR)
        val receiverTimeMessage = itemView.findViewById<TextView>(R.id.receiverTimeMessage)

        init {
            receiverMapLocation.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val data = messages[position]

                    val regex = "latitude=(-?(\\d+)\\.(\\d+))".toRegex()
                    val regex2 = "longitude=(-?(\\d+)\\.(\\d+))".toRegex()
                    val matchResult = regex.find(data.message.toString())
                    val matchResult2 = regex2.find(data.message.toString())
                    val latitude = matchResult?.groupValues?.getOrNull(1)
                    val longitude = matchResult2?.groupValues?.getOrNull(1)
                    val label = "Vị trí hiện tại"
                    val uri = "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
                    Log.d("itemview", latitude.toString())
                    // Xử lý sự kiện nhấn vào button ở đây
                    openGoogleMaps(uri)

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_SEND) {
            val view : View = LayoutInflater.from(context).inflate(R.layout.item_send_message, parent, false)
            return SendMessageViewHolder(view)
        } else {
            val view : View = LayoutInflater.from(context).inflate(R.layout.item_recriver_message, parent, false)
            return ReceiverMessageViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(message.senderId)) {
            return ITEM_SEND
        } else {
            return ITEM_RECEIVER
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curretMessage = messages.get(position)
        val messageType = curretMessage.message
        if(holder.javaClass == SendMessageViewHolder::class.java) {
            val viewHolder = holder as SendMessageViewHolder
            holder.sendTimeMessage.text = curretMessage.dateTime
            holder.cardImg.visibility = View.GONE
            holder.sendLocation.visibility = View.GONE
            holder.sendMessage.visibility = View.GONE
            if(messageType == "photo") {
                Log.d("MessageS", "photo")
                holder.cardImg.visibility = View.VISIBLE
                Glide.with(context)
                    .load(curretMessage.imgUrl)
                    .placeholder(R.drawable.no_img)
                    .into(holder.sendImg)
            } else if (messageType!!.contains("location + latitude")){
                holder.sendLocation.visibility = View.VISIBLE
            } else {
                Log.d("MessageS", "text")
                holder.sendMessage.visibility = View.VISIBLE
                holder.sendMessage.text = curretMessage.message
            }

        } else {
            val viewHolder = holder as ReceiverMessageViewHolder
            holder.receiverTimeMessage.text = curretMessage.dateTime
            holder.receiverImg.visibility = View.GONE
            holder.receiverLocation.visibility = View.GONE
            holder.receiverMessage.visibility = View.GONE
            if (curretMessage.message.equals("photo")) {
                holder.receiverImg.visibility = View.VISIBLE
                Glide.with(context)
                    .load(curretMessage.imgUrl)
                    .placeholder(R.drawable.no_img)
                    .into(holder.receiverImg)
            } else if (curretMessage.message!!.contains("location + latitude")) {
                holder.receiverLocation.visibility = View.VISIBLE
            } else {
                holder.receiverMessage.visibility = View.VISIBLE
                holder.receiverMessage.text = curretMessage.message
            }
        }

    }


    override fun getItemCount(): Int {
        Log.d("ChatActivitySize", messages.size.toString())
        return messages.size
    }

    private fun openGoogleMaps(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    }


}