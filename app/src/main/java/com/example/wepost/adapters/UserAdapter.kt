package com.example.wepost.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.Utils
import com.example.wepost.databinding.UserSampleBinding
import com.example.wepost.models.FollowerModel
import com.example.wepost.models.NotificationModel
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

class UserAdapter(options: FirestoreRecyclerOptions<UserModel>) : FirestoreRecyclerAdapter<UserModel, UserAdapter.UserViewHolder>(
    options
) {

    private lateinit var context: Context

    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var binding = UserSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val viewHolder = UserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_sample, parent, false)
        )
        context = parent.context
        return viewHolder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: UserModel) {
        try {
            Picasso.get().load(model.profilePhoto).resize(200,200).placeholder(R.drawable.user).into(holder.binding.profilePic)
        } catch (e: java.lang.Exception) {

        }
        holder.binding.name.text = model.name
        holder.binding.profession.text = model.profession

        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val currentUser = Firebase.auth.uid.toString()

        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(model.uid).get().await().toObject(UserModel::class.java)!!
            val follow = usersCollection.document(model.uid).collection("following").document(currentUser).get().await().toObject(FollowerModel::class.java)

            withContext(Dispatchers.Main) {
                if (follow != null) {
                    holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn))
                    holder.binding.followBtn.setTextColor(context.resources.getColor(R.color.green))
                    holder.binding.followBtn.text = "Following"
                } else {
                    holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_btn_bg))
                    holder.binding.followBtn.setTextColor(context.resources.getColor(R.color.white))
                    holder.binding.followBtn.text = "Follow"
                }
            }
        }

        holder.binding.followBtn.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            val follower = FollowerModel()
            CoroutineScope(Dispatchers.Main).launch {
                val user = usersCollection.document(model.uid).get().await().toObject(UserModel::class.java)!!
                val follow = usersCollection.document(model.uid).collection("following").document(currentUser).get().await().toObject(FollowerModel::class.java)
                if (follow != null) {
                    user.followerCount--
                    usersCollection.document(model.uid).collection("following").document(currentUser).delete().await()
                } else {
                    user.followerCount++
                    follower.followedBy = currentUser
                    follower.followedAt = currentTime
                    usersCollection.document(model.uid).collection("following").document(currentUser).set(follower).await()

                    val notification = NotificationModel(Firebase.auth.uid.toString(), System.currentTimeMillis(), "follow")
                    val notificationsCollection = FirebaseFirestore.getInstance().collection("notification")
                    notificationsCollection.document(model.uid).collection(model.uid).document().set(notification)

                    val notificationCollection = notificationsCollection.document(model.uid).collection(model.uid)

                    notificationCollection.addSnapshotListener { snapshot, e ->
                        val documents = snapshot?.documents
                        documents?.forEach{
                            val notifications = it.toObject(NotificationModel::class.java)!!
                            notifications.notificationId = it.id
                            notificationCollection.document(it.id).set(notifications)
                        }

                    }
                }
                usersCollection.document(model.uid).set(user)
            }
        }
    }

}