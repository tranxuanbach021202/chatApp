package com.example.appchat.notification

import android.os.AsyncTask
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
class SendNotificationTask(private val deviceToken: String, private val title: String, private val body: String) :
    AsyncTask<Void, Void, Boolean>() {
    private val serverKey = "AAAACmmVsyk:APA91bFMG3Zo4nOKochWQk47RTdkExXxguWgvwAZU1z9foKKGCmpzxBWu1PO7YKuh2fq9Kg1wKrEBfGiJLNnadh1WDOF46Ezr7oY5NXktfq9kFiBX13Uydg0hlh0df4EJ6ADSeMlUFcG" // Thay YOUR_SERVER_KEY bằng Server Key của bạn
    override fun doInBackground(vararg params: Void?): Boolean {
        val json = JSONObject()
        json.put("to", deviceToken)

        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)

        json.put("notification", notification)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .addHeader("Authorization", "key=$serverKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        return response.isSuccessful
    }
    override fun onPostExecute(result: Boolean) {
        // Xử lý khi gửi thông báo hoàn thành
    }
}