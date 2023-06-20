package com.example.wepost.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.CommentActivity
import com.example.wepost.R
import com.example.wepost.databinding.PostRvSampleBinding
import com.example.wepost.models.NotificationModel
import com.example.wepost.models.PostModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class PostAdapter(options: FirestoreRecyclerOptions<PostModel>, private val listener: IPostAdapter) : FirestoreRecyclerAdapter<PostModel, PostAdapter.PostViewHolder>(
    options
) {

    private lateinit var context: Context

    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = PostRvSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_rv_sample, parent, false))
        context = parent.context
        viewHolder.binding.likeNumber.setOnClickListener {
            listener.onLikeNumberClicked(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: PostModel) {

        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val postsCollection = db.collection("posts")

        val postImage = model.postImage
        try {
            if (postImage == "") {
                holder.binding.postImage.visibility = View.GONE
            } else {
                Picasso.get().load(model.postImage).resize(0, 1080).placeholder(R.drawable.placeholder).into(holder.binding.postImage)
            }
        } catch (e: Exception) {

        }
        val description = model.postDescription
        if (description == "") {
            holder.binding.postDescription.visibility = View.GONE
        } else {
            holder.binding.postDescription.text = model.postDescription
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(model.postedBy).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.profilePhoto).resize(100, 100).placeholder(R.drawable.user).into(holder.binding.profilePic)
                } catch (e: Exception) {

                }
                holder.binding.name.text = user.name
                holder.binding.about.text = user.profession

                if (model.likedBy.contains(Firebase.auth.uid.toString())) {
                    holder.binding.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.binding.likeButton.context, R.drawable.ic_liked))
                } else {
                    holder.binding.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.binding.likeButton.context, R.drawable.ic_unliked))
                }

                holder.binding.likeNumber.text = model.likedBy.size.toString()
                holder.binding.comment.text = model.commentCount.toString()
            }
        }

        holder.binding.likeButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                val posts = postsCollection.document(model.postId).get().await().toObject(PostModel::class.java)!!
                if (model.likedBy.contains(Firebase.auth.uid.toString())) {
                    posts.likedBy.remove(Firebase.auth.uid.toString())
                } else {
                    posts.likedBy.add(Firebase.auth.uid.toString())

                    val notification = NotificationModel(Firebase.auth.uid.toString(), System.currentTimeMillis(), "like", model.postId, model.postedBy)
                    val notificationsCollection = FirebaseFirestore.getInstance().collection("notification")
                    notificationsCollection.document(model.postedBy).collection(model.postedBy).document().set(notification)

                    val notificationCollection = notificationsCollection.document(model.postedBy).collection(model.postedBy)

                    notificationCollection.addSnapshotListener { snapshot, e ->
                        val documents = snapshot?.documents
                        documents?.forEach{
                            val notifications = it.toObject(NotificationModel::class.java)!!
                            notifications.notificationId = it.id
                            notificationCollection.document(it.id).set(notifications)
                        }

                    }
                }

                postsCollection.document(model.postId).set(posts)
            }
        }

//        holder.binding.likeNumber.setOnClickListener {
//            val intent = Intent(context, LikeActivity::class.java)
//            context.startActivity(intent)
//        }

        holder.binding.comment.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", model.postId)
            intent.putExtra("postedBy", model.postedBy)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

//        holder.binding.share.setOnClickListener {
//            val api = context.applicationContext.applicationInfo
//            val apkPath = api.sourceDir
//            Log.d("ddd", apkPath)
//
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.type = "application/vnd.android.package-archive"
//            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(apkPath)))
//            context.startActivity(Intent.createChooser(intent, "Share app using..."))
//        }

    }

}

interface IPostAdapter {
    fun onLikeNumberClicked(postId: String)
}