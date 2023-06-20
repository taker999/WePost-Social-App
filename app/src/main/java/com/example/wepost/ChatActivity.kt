package com.example.wepost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.adapters.StoryAdapter
import com.example.wepost.chat.adapters.UsersAdapter
import com.example.wepost.databinding.ActivityChatBinding
import com.example.wepost.models.StoryModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: UsersAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val usersCollection = db.collection("users")
        val query = usersCollection.whereNotEqualTo("uid",  auth.uid)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query, UserModel::class.java).build()
        adapter = UsersAdapter(recyclerViewOptions)
        binding.chatsRV.adapter = adapter
        binding.chatsRV.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}