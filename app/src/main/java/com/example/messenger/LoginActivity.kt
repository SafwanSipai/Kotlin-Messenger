package com.example.messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var btnLogin: Button = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            Login()
        }
    }

    fun Login(){
        val email = email_edit_text_login.text.toString();
        val password = password_edit_text_login.text.toString();

        if (email.isEmpty() || password.isEmpty() ){
            Toast.makeText(this,"Please fill all the enteries",Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Toast.makeText(this,"Login Successfull",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
            }
    }
}