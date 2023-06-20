package com.example.wepost.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wepost.R
import com.example.wepost.adapters.FollowerAdapter
import com.example.wepost.databinding.FragmentProfileBinding
import com.example.wepost.models.FollowerModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FollowerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
        db = FirebaseFirestore.getInstance()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarProfile)

        val usersCollection = db.collection("users")
        val followersCollection = usersCollection.document(auth.uid.toString()).collection("following")
        val query = followersCollection.orderBy("followedAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<FollowerModel>().setQuery(query, FollowerModel::class.java).build()
        adapter = FollowerAdapter(recyclerViewOptions)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressBar1.visibility = View.VISIBLE
                binding.progressBar2.visibility = View.VISIBLE
            }
            val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.coverPhoto).resize(500,500).placeholder(R.drawable.placeholder).into(binding.coverPhoto)

                } catch (e: Exception) {

                }
                try {
                    Picasso.get().load(user.profilePhoto).resize(300,300).placeholder(R.drawable.user).into(binding.profilePic)
                } catch (e: Exception) {

                }
                binding.userName.text = user.name
                binding.profession.text = user.profession
                binding.followersCount.text = user.followerCount.toString()
                binding.progressBar1.visibility = View.GONE
                binding.progressBar2.visibility = View.GONE
            }

        }

        binding.editNameBtn.setOnClickListener {
            binding.editNameBtn.visibility = View.GONE
            binding.userName.visibility = View.INVISIBLE
            binding.userNameEdit.visibility = View.VISIBLE
            binding.saveNameBtn.visibility = View.VISIBLE
            binding.userNameEdit.setText(binding.userName.text, TextView.BufferType.EDITABLE)

        }

        binding.saveNameBtn.setOnClickListener {
            binding.saveNameBtn.visibility = View.GONE
            if (binding.userNameEdit.text.isNotBlank()) {
                binding.progressBar3.visibility = View.VISIBLE
                val newName = binding.userNameEdit.text.trim()
                binding.userName.text = newName

                CoroutineScope(Dispatchers.IO).launch {
                    val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
                    user.name = newName.toString()
                    usersCollection.document(user.uid).set(user)
                    withContext(Dispatchers.Main) {
                        binding.progressBar3.visibility = View.GONE
                        binding.editNameBtn.visibility = View.VISIBLE
                        binding.userName.visibility = View.VISIBLE
                        binding.userNameEdit.visibility = View.GONE
                        try {
                            Toast.makeText(activity, "Name updated...", Toast.LENGTH_SHORT).show()
                        } catch (e: java.lang.NullPointerException) {

                        }
                    }
                }
            } else {
                binding.editNameBtn.visibility = View.VISIBLE
                binding.userName.visibility = View.VISIBLE
                binding.userNameEdit.visibility = View.GONE
            }
        }

        binding.editProfessionBtn.setOnClickListener {
            binding.editProfessionBtn.visibility = View.GONE
            binding.profession.visibility = View.INVISIBLE
            binding.professionEdit.visibility = View.VISIBLE
            binding.saveProfessionBtn.visibility = View.VISIBLE
            binding.professionEdit.setText(binding.profession.text, TextView.BufferType.EDITABLE)

        }

        binding.saveProfessionBtn.setOnClickListener {
            binding.saveProfessionBtn.visibility = View.GONE
            if (binding.professionEdit.text.isNotBlank()) {
                binding.progressBar4.visibility = View.VISIBLE
                val newProfession = binding.professionEdit.text.trim()
                binding.profession.text = newProfession

                CoroutineScope(Dispatchers.IO).launch {
                    val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
                    user.profession = newProfession.toString()
                    usersCollection.document(user.uid).set(user)
                    withContext(Dispatchers.Main) {
                        binding.progressBar4.visibility = View.GONE
                        binding.editProfessionBtn.visibility = View.VISIBLE
                        binding.profession.visibility = View.VISIBLE
                        binding.professionEdit.visibility = View.GONE
                        try {
                            Toast.makeText(activity, "Profession updated...", Toast.LENGTH_SHORT).show()
                        } catch (e: java.lang.NullPointerException) {

                        }
                    }
                }
            } else {
                binding.editProfessionBtn.visibility = View.VISIBLE
                binding.profession.visibility = View.VISIBLE
                binding.professionEdit.visibility = View.GONE
            }
        }

        binding.changeUserImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 11)
        }

        binding.changeCoverPhoto.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 22)
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_profile_item, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22) {
            if (data?.data != null) {
                binding.progressBar1.visibility = View.VISIBLE
                val uri = data.data
                binding.coverPhoto.setImageURI(uri)
                val usersCollection = db.collection("users")

                CoroutineScope(Dispatchers.IO).launch {
                    val reference = storage.reference.child("cover_photo").child(auth.uid.toString())

                    try {

                        val bmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri as Uri)
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                        val dataImg = baos.toByteArray()

                        reference.putBytes(dataImg).await()

                    } catch (e: Exception) {
                        try {
                            reference.putFile(uri as Uri).await()
                        } catch (e: Exception) {

                        }
                    }

                    reference.putFile(uri as Uri).await()
                    val downloadUrl = reference.downloadUrl.await()
                    val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
                    user.coverPhoto = downloadUrl.toString()
                    usersCollection.document(user.uid).set(user)
                    withContext(Dispatchers.Main) {
                        binding.progressBar1.visibility = View.GONE
                        try {
                            Toast.makeText(activity, "Cover photo updated...", Toast.LENGTH_SHORT).show()
                        } catch (e: java.lang.NullPointerException) {

                        }

                    }
                }
            }
        } else {
            if (data?.data != null) {
                binding.progressBar2.visibility = View.VISIBLE
                val uri = data.data
                binding.profilePic.setImageURI(uri)
                val usersCollection = db.collection("users")

                CoroutineScope(Dispatchers.IO).launch {
                    val reference = storage.reference.child("profile_photo").child(auth.uid.toString())

                    try {

                        val bmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri as Uri)
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                        val dataImg = baos.toByteArray()

                        reference.putBytes(dataImg).await()

                    } catch (e: Exception) {
                        try {
                            reference.putFile(uri as Uri).await()
                        } catch (e: Exception) {

                        }
                    }

                    reference.putFile(uri as Uri).await()
                    val downloadUrl = reference.downloadUrl.await()
                    val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
                    user.profilePhoto = downloadUrl.toString()
                    usersCollection.document(user.uid).set(user)
                    withContext(Dispatchers.Main) {
                        binding.progressBar2.visibility = View.GONE
                        try {
                            Toast.makeText(activity, "Profile photo updated...", Toast.LENGTH_SHORT).show()
                        } catch (e: java.lang.NullPointerException) {

                        }

                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}