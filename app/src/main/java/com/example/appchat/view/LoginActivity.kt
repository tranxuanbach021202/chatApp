package com.example.appchat.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appchat.R
import com.example.appchat.databinding.ActivityLoginBinding
import com.example.appchat.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInAccount: GoogleSignInClient
    private lateinit var database : DatabaseReference
    private lateinit var sharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val google = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val checkbox = binding.txtRemember
        sharedPreferences = getSharedPreferences("myapp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var email : String? = sharedPreferences.getString("email", null)
        var password : String? = sharedPreferences.getString("password", null)
        val savePassword : Boolean = sharedPreferences.getBoolean("savePassword", false)
        googleSignInAccount = GoogleSignIn.getClient(this, google)


        if (email != null) {
            binding.etEmail.setText(email.toString())
        }

        Log.d("savePassword", savePassword.toString())

        if (savePassword) {
            binding.txtRemember.isChecked = true
            binding.etPass.setText(password)
        }


        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPass.text.toString()
            if (email!!.isNotEmpty() && password!!.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (checkbox.isChecked) {
                            editor.putBoolean("savePassword", true)
                            editor.apply()
                        } else {
                            editor.putBoolean("savePassword", false)
                            editor.apply()
                        }
                        saveLogin(email, password)
                        editor.putBoolean("isLoggedIn", true)
                        editor.apply()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Login ko thanh cong", Toast.LENGTH_SHORT).show()
            }
        }
        binding.txtForgotPass.setOnClickListener {

        }
        binding.frameStackgoogle.setOnClickListener {
            signInGoogle()
        }

        binding.txtRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.txtForgotPass.setOnClickListener {
            forgotPassword()
        }




    }

//    private fun StoreDataUsingSharePref(email: String, pass: String) {
//        val editor = getSharedPreferences("myAppPrefs", MODE_PRIVATE).edit()
//        editor.putString("svEmail", email)
//        editor.putString("svPassword", pass)
//        editor.apply()
//
//    }

    private fun saveLogin(email : String, password: String) {
        sharedPreferences  = getSharedPreferences("myapp", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    private fun getLoggedIn() : HashMap<String, Any>{
        sharedPreferences = getSharedPreferences("myapp", Context.MODE_PRIVATE)
        val isLoggedIn: Boolean = sharedPreferences.getBoolean("isLoggedIn", false)
        val userHashMap =  HashMap<String, Any>()
        if (isLoggedIn) {
            val email: String? = sharedPreferences.getString("email", null)
            val password: String? = sharedPreferences.getString("password", null)

            if (email != null && password != null) {
                userHashMap["email"] = email
                userHashMap["password"] = password
            } else {
                userHashMap["email"] = "null"
                userHashMap["password"] = "null"
            }
        }
        return userHashMap
    }


    private fun signInGoogle() {
        val signIn = googleSignInAccount.signInIntent
        launcher.launch(signIn)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
    }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if(account!=null) {
                updateUI(account)
            } else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    fun forgotPassword() {
        val email = binding.etEmail.text.toString()
        if(email == "") {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Please check your email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Reset password failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }


}