package com.example.wepost.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.wepost.adapters.NotificationAdapter
import com.example.wepost.databinding.FragmentNotificationBinding
import com.example.wepost.models.NotificationModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class NotificationFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        val binding = FragmentNotificationBinding.inflate(layoutInflater, container, false)

//        viewPager = view.findViewById(R.id.viewPager)
//        viewPager.adapter = ViewPagerAdapter(parentFragmentManager)
//
//        tabLayout = view.findViewById(R.id.tabLayout)
//        tabLayout.setupWithViewPager(viewPager)


        val notificationsCollection = FirebaseFirestore.getInstance().collection("notification").document(Firebase.auth.uid.toString()).collection(Firebase.auth.uid.toString())
        val query = notificationsCollection.orderBy("notificationAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<NotificationModel>().setQuery(query, NotificationModel::class.java).build()
        Log.d("ddd", recyclerViewOptions.toString())
        adapter = NotificationAdapter(recyclerViewOptions)
        binding.notificationRv.adapter = adapter
        binding.notificationRv.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

}