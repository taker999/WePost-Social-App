package com.example.wepost

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.chat.adapters.ChatAdapter
import com.example.wepost.chat.models.Message
import com.example.wepost.databinding.ActivityChatDetailBinding
import com.example.wepost.models.UserModel
import com.example.wepost.network.ApiClient
import com.example.wepost.network.ApiService
import com.example.wepost.utilities.Constants
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        compositeDisposable = CompositeDisposable()

        val senderId = auth.uid
        val receiverId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
        val profilePic = intent.getStringExtra("profilePic")

        binding.userName.text = userName
        try {
            Picasso.get().load(profilePic).resize(100,100).placeholder(R.drawable.user).into(binding.profilePic)
        } catch (e: Exception) {

        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        val senderRoom = senderId + receiverId
        val receiverRoom = receiverId + senderId

        val chatsCollection = db.collection("chats").document("chat").collection(senderRoom)
        val query = chatsCollection.orderBy("time", Query.Direction.ASCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message::class.java).build()
        adapter = ChatAdapter(recyclerViewOptions, receiverId.toString())
        binding.chatsRV.adapter = adapter
//        binding.chatsRV.layoutManager = LinearLayoutManager(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true;
        binding.chatsRV.layoutManager = layoutManager


        val chatCollections = db.collection("chats")
        binding.sendBtn.setOnClickListener {
            val text = binding.chatET.text.toString().trim()
            if (text != "") {
//                binding.chatET.setText("")
                val message = Message(senderId.toString(), text, System.currentTimeMillis())

                CoroutineScope(Dispatchers.IO).launch {
                    chatCollections.document("chat").collection(senderRoom).document().set(message)

                    chatCollections.document("chat").collection(senderRoom).addSnapshotListener { snapshot, e ->
                        val documents = snapshot?.documents
                        documents?.forEach{
                            val chats = it.toObject(Message::class.java)!!
                            chats.messageId = it.id
                            chatCollections.document("chat").collection(senderRoom).document(it.id).set(chats)
                        }

                    }

                    chatCollections.document("chat").collection(receiverRoom).document().set(message)

                    chatCollections.document("chat").collection(receiverRoom).addSnapshotListener { snapshot, e ->
                        val documents = snapshot?.documents
                        documents?.forEach{
                            val chats = it.toObject(Message::class.java)!!
                            chats.messageId = it.id
                            chatCollections.document("chat").collection(receiverRoom).document(it.id).set(chats)
                        }

                    }
                }
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        val receiverUser = db.collection(Constants().KEY_COLLECTION_USERS).document(receiverId.toString()).get().await().toObject(UserModel::class.java)!!
                        val user = db.collection(Constants().KEY_COLLECTION_USERS).document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
                        withContext(Dispatchers.Main) {
                            val tokens = JSONArray()
                            tokens.put(receiverUser.fcmToken)

                            val data = JSONObject()
                            data.put(Constants().KEY_USER_ID, user.uid)
                            data.put(Constants().KEY_NAME, user.name)
                            data.put(Constants().KEY_FCM_TOKEN, user.fcmToken)
                            data.put(Constants().KEY_MESSAGE, binding.chatET.text.toString().trim())

                            val body = JSONObject()
                            body.put(Constants().REMOTE_MSG_DATA, data)
                            body.put(Constants().REMOTE_MSG_REGISTRATION_IDS, tokens)

                            sendNotification(body.toString())
                        }
                    }

                } catch (e: Exception) {
//                    showToast(e.message.toString())
                }
            }
        }
    }

    private fun sendNotification(messageBody: String) {
        ApiClient().getClient().create(ApiService::class.java).sendMessage(
            Constants().getRemoteMsgHeaders(), messageBody
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body().toString())
                            val results = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error = results[0] as JSONObject
    //                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
    //                    showToast("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
    //                showToast(t.message.toString())
            }

        })
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}