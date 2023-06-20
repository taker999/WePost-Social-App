package com.example.wepost.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cooltechworks.views.shimmer.ShimmerRecyclerView
import com.example.wepost.ChatActivity
import com.example.wepost.LikeActivity
import com.example.wepost.MainActivity
import com.example.wepost.R
import com.example.wepost.adapters.IPostAdapter
import com.example.wepost.adapters.PostAdapter
import com.example.wepost.adapters.StoryAdapter
import com.example.wepost.databinding.FragmentHomeBinding
import com.example.wepost.models.PostModel
import com.example.wepost.models.StoryModel
import com.example.wepost.models.UserModel
import com.example.wepost.models.UserStoriesModel
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

class HomeFragment : Fragment(), IPostAdapter {

    private lateinit var adapter: PostAdapter
    private lateinit var adapterStory: StoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage= Firebase.storage
        dialog = ProgressDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(inflater, container, false)

//        binding.postRv.showShimmerAdapter()

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setTitle("Story Uploading")
        dialog.setMessage("Please wait...")
        dialog.setCancelable(false)

        CoroutineScope(Dispatchers.IO).launch {
            val usersCollection = db.collection("users")
            val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.profilePhoto).resize(200, 200).placeholder(R.drawable.user).into(binding.profilePic)
                } catch (e: Exception) {

                }
            }
        }

        val storiesCollection = db.collection("stories")
        val queryStory = storiesCollection.orderBy("storyAt", Query.Direction.DESCENDING)
        val recyclerViewOptionsStory = FirestoreRecyclerOptions.Builder<StoryModel>().setQuery(queryStory, StoryModel::class.java).build()
        adapterStory = StoryAdapter(recyclerViewOptionsStory)
        binding.storyRV.adapter = adapterStory
        binding.storyRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val usersCollection = db.collection("users")
        val postsCollection = db.collection("posts")
        val query = postsCollection.orderBy("postedAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<PostModel>().setQuery(query, PostModel::class.java).build()
        adapter = PostAdapter(recyclerViewOptions, this)
        binding.postRv.adapter = adapter
        binding.postRv.layoutManager = LinearLayoutManager(context)
//        binding.postRv.hideShimmerAdapter()

        binding.addStoryImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
            binding.addStoryImage.setImageURI(it)

            CoroutineScope(Dispatchers.IO).launch {
                val reference = storage.reference.child("stories").child(auth.uid.toString()).child(System.currentTimeMillis().toString())
                if (it != null) {
                    withContext(Dispatchers.Main) {
                        dialog.show()
                    }

                    try {

                        val bmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, it)
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                        val data = baos.toByteArray()

                        reference.putBytes(data).await()

                    } catch (e: Exception) {
                        try {
                            reference.putFile(it).await()
                        } catch (e: Exception) {

                        }
                    }

                    reference.putFile(it).await()
                    val downloadUrl = reference.downloadUrl.await()
                    val storyUser = storiesCollection.document(auth.uid.toString()).get().await().toObject(StoryModel::class.java)
                    if (storyUser == null) {
                        val story = StoryModel(auth.uid.toString(), System.currentTimeMillis())
                        val usersStories = UserStoriesModel(downloadUrl.toString(), System.currentTimeMillis())
                        story.stories.add(usersStories)
                        storiesCollection.document(auth.uid.toString()).set(story).await()
                    } else {
                        val usersStories = UserStoriesModel(downloadUrl.toString(), System.currentTimeMillis())
                        storyUser.storyAt = System.currentTimeMillis()
                        storyUser.stories.add(usersStories)
                        storiesCollection.document(auth.uid.toString()).set(storyUser).await()
                    }
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                    }
                }
            }
        })

        binding.chatActivityIcon.setOnClickListener {
            val intent = Intent(activity, ChatActivity::class.java)
            startActivity(intent)
        }

        binding.profilePic.setOnClickListener {
            val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            (activity as MainActivity).binding.readableBottomBar.selectItem(4)
            transaction.replace(R.id.container, ProfileFragment())
            transaction.commit()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        adapterStory.startListening()
    }

    override fun onLikeNumberClicked(postId: String) {
        val intent = Intent(context, LikeActivity::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

}