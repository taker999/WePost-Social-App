package com.example.wepost.chat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.ChatDetailActivity
import com.example.wepost.R
import com.example.wepost.chat.models.Message
import com.example.wepost.databinding.ChatUserSampleBinding
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

class UsersAdapter(options: FirestoreRecyclerOptions<UserModel>) : FirestoreRecyclerAdapter<UserModel, UsersAdapter.UsersViewHolder>(
    options
) {

    private lateinit var context: Context

    class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ChatUserSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        context = parent.context
        return UsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_user_sample, parent, false))
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int, model: UserModel) {
        try {
            Picasso.get().load(model.profilePhoto).resize(200,200).placeholder(R.drawable.user).into(holder.binding.profilePic)
        } catch (_: Exception) {

        }

        holder.binding.name.text = model.name
        holder.binding.lastMessage.text = model.lastMessage

        CoroutineScope(Dispatchers.IO).launch {
            val message = FirebaseFirestore.getInstance().collection("chats").document("chat").collection(Firebase.auth.uid.toString() + model.uid).orderBy("time").limitToLast(1).get().await().toObjects(Message::class.java)
            withContext(Dispatchers.Main) {
                for (i in 0 until message.size) {
                    holder.binding.lastMessage.text = message[i].message
                }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatDetailActivity::class.java)
            intent.putExtra("userId", model.uid)
            intent.putExtra("profilePic", model.profilePhoto)
            intent.putExtra("userName", model.name)
            context.startActivity(intent)
        }
    }

}