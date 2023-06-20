package com.example.wepost.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.databinding.CommentSampleBinding
import com.example.wepost.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LikeAdapter: RecyclerView.Adapter<LikeAdapter.LikeViewHolder>() {

    private val items: ArrayList<String> = ArrayList()

    class LikeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CommentSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        return LikeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.comment_sample, parent, false))
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val currentItem = items[position]

        CoroutineScope(Dispatchers.IO).launch {
            val usersCollection = FirebaseFirestore.getInstance().collection("users")
            val user = usersCollection.document(currentItem).get().await().toObject(UserModel::class.java)!!

            withContext(Dispatchers.Main) {
                holder.binding.comment.text = user.name
                holder.binding.time.text = user.profession

                try {
                    Picasso.get().load(user.profilePhoto).placeholder(R.drawable.user).into(holder.binding.profilePic)
                } catch (e: Exception) {

                }
            }

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun update(update: ArrayList<String>) {
        items.clear()
        items.addAll(update)

        notifyDataSetChanged()
    }

}