package com.example.wepost.chat.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.Utils
import com.example.wepost.chat.models.Message
import com.example.wepost.databinding.SampleReceiverBinding
import com.example.wepost.databinding.SampleSenderBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatAdapter(options: FirestoreRecyclerOptions<Message>, var recId: String) : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(
    options
) {

    private val SENDER_VIEW_TYPE = 1
    private val RECEIVER_VIEW_TYPE = 2
    private lateinit var context: Context

    class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SampleReceiverBinding.bind(itemView)
    }

    class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = SampleSenderBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        if (viewType == SENDER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_sender, parent, false)
            return SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_receiver, parent, false)
            return ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {

//        val message = FirestoreRecyclerOptions<Message>.snapshots.get(position).uid

        return if (snapshots[position].uid == Firebase.auth.uid) {
            SENDER_VIEW_TYPE
        } else {
            RECEIVER_VIEW_TYPE
        }

//        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {

        holder.itemView.setOnLongClickListener{
            AlertDialog.Builder(context).setTitle("Delete").setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes") { dialog, which ->
                    val database = FirebaseFirestore.getInstance()
                    val sender = Firebase.auth.uid + recId
                    CoroutineScope(Dispatchers.IO).launch {
                        database.collection("chats").document("chat").collection(sender).document(model.messageId).delete()
                    }
                }.setNeutralButton("delete for everyone?") { dialog, which ->
                    val database = FirebaseFirestore.getInstance()
                    val sender = Firebase.auth.uid + recId
                    val receiver = recId + Firebase.auth.uid
                    CoroutineScope(Dispatchers.IO).launch {
                        database.collection("chats").document("chat").collection(sender).document(model.messageId).delete()
                        val chats = database.collection("chats").document("chat").collection(receiver).get().await().toObjects(Message::class.java)
                        var string = ""
                        for (i in 0 until chats.size) {
                            if (chats[i].time == model.time) {
                                string = chats[i].messageId
                            }
                        }
                        database.collection("chats").document("chat").collection(receiver).document(string).delete()
                    }
                }.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }.show()

            true
        }

        if (holder.javaClass == SenderViewHolder::class.java) {
            (holder as SenderViewHolder).binding.senderText.text = model.message
            holder.binding.senderTime.text = Utils.getTimeAgo(model.time)
        } else {
            (holder as ReceiverViewHolder).binding.receiverText.text = model.message
            holder.binding.receiverTime.text = Utils.getTimeAgo(model.time)
        }
    }

}