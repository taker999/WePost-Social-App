package com.example.wepost.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.CommentActivity
import com.example.wepost.MainActivity
import com.example.wepost.R
import com.example.wepost.databinding.NotificationSampleBinding
import com.example.wepost.fragments.ProfileFragment
import com.example.wepost.models.NotificationModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class NotificationAdapter(options: FirestoreRecyclerOptions<NotificationModel>) :
    FirestoreRecyclerAdapter<NotificationModel, NotificationAdapter.NotificationViewHolder>(options) {

    private lateinit var context: Context

    class NotificationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = NotificationSampleBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val viewHolder = NotificationViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notification_sample, parent, false)
        )
        context = parent.context
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int,
        model: NotificationModel
    ) {

        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val type = model.type
        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(model.notificationBy).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.profilePhoto).resize(200,200).placeholder(R.drawable.user).into(holder.binding.profilePic)
                } catch (e: Exception) {

                }

                if (type == "like") {
                    holder.binding.notification.text = Html.fromHtml("<b>"+user.name+"</b>" + ": liked your post")
                } else if (type == "comment") {
                    holder.binding.notification.text = Html.fromHtml("<b>"+user.name+"</b>" + ": commented on your post")
                } else {
                    holder.binding.notification.text = Html.fromHtml("<b>"+user.name+"</b>" + ": started following you")
                }

                holder.binding.time.text = TimeAgo.using(model.notificationAt)
            }

        }

        holder.binding.openNotification.setOnClickListener {
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"))
            if (type != "follow") {

                CoroutineScope(Dispatchers.IO).launch {
                    val notificationsCollection = FirebaseFirestore.getInstance().collection("notification").document(model.postedBy).collection(model.postedBy).document(model.notificationId).get().await().toObject(NotificationModel::class.java)!!
                    notificationsCollection.isOpened = true
                    FirebaseFirestore.getInstance().collection("notification").document(model.postedBy).collection(model.postedBy).document(model.notificationId).set(notificationsCollection)

                }

//                notificationsCollection.document(model.postedBy).collection(model.postedBy).document(model.)

                val intent = Intent(context, CommentActivity::class.java)
                intent.putExtra("postId", model.postId)
                intent.putExtra("postedBy", model.postedBy)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val notificationsCollection = FirebaseFirestore.getInstance().collection("notification").document(Firebase.auth.uid.toString()).collection(Firebase.auth.uid.toString()).document(model.notificationId).get().await().toObject(NotificationModel::class.java)!!
                    notificationsCollection.isOpened = true
                    FirebaseFirestore.getInstance().collection("notification").document(Firebase.auth.uid.toString()).collection(Firebase.auth.uid.toString()).document(model.notificationId).set(notificationsCollection)

                }

                val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container, ProfileFragment())
                (context as MainActivity).binding.readableBottomBar.selectItem(4)
                transaction.commit()
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            if (model.isOpened) {
                withContext(Dispatchers.Main) {
                    holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

}