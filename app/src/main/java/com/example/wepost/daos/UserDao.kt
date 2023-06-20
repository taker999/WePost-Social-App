package com.example.wepost.daos

import com.example.wepost.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: UserModel?) {
        user?.let {
            CoroutineScope(Dispatchers.IO).launch {
                userCollection.document(user.uid).set(it)
            }
        }
    }

}