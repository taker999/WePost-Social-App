package com.example.wepost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.adapters.LikeAdapter
import com.example.wepost.databinding.ActivityLikeBinding
import com.example.wepost.models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LikeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikeBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: LikeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val postId = intent.getStringExtra("postId")

        val items = ArrayList<String>()

        adapter = LikeAdapter()
        binding.likeRV.layoutManager = LinearLayoutManager(this)
        binding.likeRV.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val postCollection = db.collection("posts")
            val post = postCollection.document(postId.toString()).get().await().toObject(PostModel::class.java)
            for (i in 0 until post?.likedBy?.size as Int) {
                items.add(post.likedBy[i])
            }
            withContext(Dispatchers.Main) {
                adapter.update(items)
            }
        }

    }
}