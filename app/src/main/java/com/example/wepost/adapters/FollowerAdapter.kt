package com.example.wepost.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.databinding.FriendsRvSampleBinding
import com.example.wepost.models.FollowerModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FollowerAdapter(options: FirestoreRecyclerOptions<FollowerModel>) : FirestoreRecyclerAdapter<FollowerModel, FollowerAdapter.FollowerViewHolder>(
    options
) {

    class FollowerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: FriendsRvSampleBinding = FriendsRvSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val viewHolder = FollowerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.friends_rv_sample, parent, false)
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int, model: FollowerModel) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
//            val followedBy = usersCollection.document(Firebase.auth.uid.toString()).collection("following").get().await().toString()
            val user = usersCollection.document(model.followedBy).get().await().toObject(UserModel::class.java)
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user?.profilePhoto).resize(50,50).placeholder(R.drawable.user).into(holder.binding.profilePic)
                } catch (e: Exception) {

                }

            }
        }
    }

}