package com.example.appchat.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appchat.R
import com.example.appchat.databinding.ActivityProfileEditBinding
import com.example.appchat.databinding.ActivityTwoBinding
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTwoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences: SharedPreferences = getSharedPreferences("myapp", Context.MODE_PRIVATE)
        val isLogged : Boolean = sharedPreferences.getBoolean("isLoggedIn", false)
        val email : String? = sharedPreferences.getString("email", null)
        val password : String? = sharedPreferences.getString("password", null)
        firebaseAuth = FirebaseAuth.getInstance()
        Log.d("IsLogged", isLogged.toString())
        if (isLogged) {
            if (email!!.isNotEmpty() && password!!.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Login ko thanh cong", Toast.LENGTH_SHORT).show()
            }
        } else {
            setContentView(R.layout.activity_two)
            binding = ActivityTwoBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            binding.btnSignup.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        }

    }
}