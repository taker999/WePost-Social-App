package com.example.wepost.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.R
import com.example.wepost.adapters.UserAdapter
import com.example.wepost.databinding.FragmentSearchBinding
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val binding = FragmentSearchBinding.inflate(inflater, container, false)
        val userCollections = db.collection("users")
//        val query = userCollections.orderBy("uid", Query.Direction.DESCENDING)
        val query = userCollections.whereNotEqualTo("uid",  auth.uid)

        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query, UserModel::class.java).build()
        adapter = UserAdapter(recyclerViewOptions)

        binding.usersRV.adapter = adapter
        binding.usersRV.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}