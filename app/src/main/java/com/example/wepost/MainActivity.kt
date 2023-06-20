package com.example.wepost

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.wepost.databinding.ActivityMainBinding
import com.example.wepost.fragments.*
import com.example.wepost.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.iammert.library.readablebottombar.ReadableBottomBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, HomeFragment())
        transaction.commit()

        getToken()

        binding.readableBottomBar.setOnItemSelectListener( object : ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {

                val transaction = supportFragmentManager.beginTransaction()

                when (index) {
                    0 -> {
                        transaction.replace(R.id.container, HomeFragment())
                    }
                    1 -> {
                        transaction.replace(R.id.container, SearchFragment())
                    }
                    2 -> {
                        transaction.replace(R.id.container, AddPostFragment())
                    }
                    3 -> {
                        transaction.replace(R.id.container, NotificationFragment())
                    }
                    4 -> {
                        transaction.replace(R.id.container, ProfileFragment())
                    }
                }
                transaction.commit()

            }
        })
    }

    private fun getToken() {
        CoroutineScope(Dispatchers.IO).launch {
            val token = FirebaseMessaging.getInstance().token.await()
            val user = db.collection("users").document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
            user.fcmToken = token
            db.collection("users").document(auth.uid.toString()).set(user)
        }
    }
}