package com.example.wepost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.adapters.CommentAdapter
import com.example.wepost.databinding.ActivityCommentBinding
import com.example.wepost.models.CommentModel
import com.example.wepost.models.NotificationModel
import com.example.wepost.models.PostModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var postId: String
    private lateinit var postedBy: String
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        setSupportActionBar(binding.toolbar2)
        this.title = "Comment"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        postId = intent.getStringExtra("postId")!!
        postedBy = intent.getStringExtra("postedBy")!!

        val postsCollection = db.collection("posts")
        val usersCollection = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
            val post = postsCollection.document(postId).get().await().toObject(PostModel::class.java)!!
            val userId = post.postedBy
            val user = usersCollection.document(userId).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(post.postImage).resize(500,500).placeholder(R.drawable.placeholder).into(binding.postImage)
                } catch (e: Exception) {

                }

                try {
                    Picasso.get().load(user.profilePhoto).resize(100,100).placeholder(R.drawable.user).into(binding.profilePic)
                } catch (e: Exception) {

                }

                if (post.likedBy.contains(auth.uid)) {
                    binding.likeButton.setImageDrawable(ContextCompat.getDrawable(binding.likeButton.context, R.drawable.ic_liked))
                } else {
                    binding.likeButton.setImageDrawable(ContextCompat.getDrawable(binding.likeButton.context, R.drawable.ic_unliked))
                }

                binding.description.text = post.postDescription
                binding.likeNumber.text = post.likedBy.size.toString()

                binding.userName.text = user.name
                binding.comment.text = post.commentCount.toString()
            }

        }

        binding.commentBtn.setOnClickListener {
            val commentBody = binding.commentET.text.toString().trim()
            binding.commentET.setText("")
            if (commentBody.isNotBlank()) {
                val comment = CommentModel(commentBody, System.currentTimeMillis(), auth.uid.toString())

                CoroutineScope(Dispatchers.IO).launch {
                    postsCollection.document(postId).collection("comments").document().set(comment)
                    val post = postsCollection.document(postId).get().await().toObject(PostModel::class.java)!!
                    post.commentCount++
                    postsCollection.document(postId).set(post)


                    val notification = NotificationModel(Firebase.auth.uid.toString(), System.currentTimeMillis(), "comment", postId, postedBy)
                    val notificationsCollection = FirebaseFirestore.getInstance().collection("notification")
                    notificationsCollection.document(postedBy).collection(postedBy).document().set(notification)

                    val notificationCollection = notificationsCollection.document(postedBy).collection(postedBy)

                    notificationCollection.addSnapshotListener { snapshot, e ->
                        val documents = snapshot?.documents
                        documents?.forEach{
                            val notifications = it.toObject(NotificationModel::class.java)!!
                            notifications.notificationId = it.id
                            notificationCollection.document(it.id).set(notifications)
                        }

                    }
                }
            }
        }

        val commentsCollection = postsCollection.document(postId).collection("comments")
        val query = commentsCollection.orderBy("commentedAt", Query.Direction.ASCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<CommentModel>().setQuery(query, CommentModel::class.java).build()
        adapter = CommentAdapter(recyclerViewOptions)
        binding.commentRv.adapter = adapter
        binding.commentRv.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}