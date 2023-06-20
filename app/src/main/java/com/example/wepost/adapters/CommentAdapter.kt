package com.example.wepost.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.Utils
import com.example.wepost.databinding.CommentSampleBinding
import com.example.wepost.models.CommentModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class CommentAdapter(options: FirestoreRecyclerOptions<CommentModel>) : FirestoreRecyclerAdapter<CommentModel, CommentAdapter.CommentViewHolder>(
    options
) {

    class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = CommentSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val viewHolder = CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.comment_sample, parent, false))
        return viewHolder
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: CommentModel) {
//        holder.binding.comment.text = model.comment
        val time = TimeAgo.using(model.commentedAt)
        holder.binding.time.text = time

        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(model.commentedBy).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.profilePhoto).placeholder(R.drawable.user).into(holder.binding.profilePic)
                } catch (e: Exception) {

                }
                holder.binding.comment.text = Html.fromHtml("<b>"+user.name+"</b>" + ": " + model.comment)
            }

        }
    }

}