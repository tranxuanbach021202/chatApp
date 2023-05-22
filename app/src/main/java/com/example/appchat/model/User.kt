package com.example.appchat.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val profileImg: String? = "",
    val name:String? = "",
    val email: String? ="",
    val address: String? ="",
    val phone: String? = "",
    val uid: String? ="",
    val status: String? =""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "profileImg" to profileImg,
            "name" to name,
            "email" to email,
            "address" to address,
            "phone" to phone,
            "uid" to uid,
            "status" to status
        )
    }

}
