package com.example.wepost.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wepost.MainActivity
import com.example.wepost.R
import com.example.wepost.databinding.FragmentAddPostBinding
import com.example.wepost.models.PostModel
import com.example.wepost.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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


class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var uri: Uri
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage
        dialog = ProgressDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddPostBinding.inflate(inflater, container, false)

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setTitle("Post uploading")
        dialog.setMessage("Please wait...")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val usersCollection = db.collection("users")
        CoroutineScope(Dispatchers.IO).launch {
            val user = usersCollection.document(auth.uid.toString()).get().await().toObject(UserModel::class.java)!!
            withContext(Dispatchers.Main) {
                try {
                    Picasso.get().load(user.profilePhoto).resize(100,100).placeholder(R.drawable.user).into(binding.profilePic)
                    binding.name.text = user.name
                    binding.profession.text = user.profession
                } catch (e: Exception) {

                }
            }
        }

        binding.postDescription.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val description = binding.postDescription.text.toString().trim()
                if (description != "") {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(context as Context, R.drawable.follow_btn_bg))
                    binding.postBtn.setTextColor(requireContext().resources.getColor(R.color.white))
                    binding.postBtn.isEnabled = true
                } else {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(context as Context, R.drawable.follow_active_btn))
                    binding.postBtn.setTextColor(requireContext().resources.getColor(R.color.green))
                    binding.postBtn.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        } )

        binding.addImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 10)
        }

        binding.postBtn.setOnClickListener {
            dialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val reference = storage.reference.child("posts").child(auth.uid.toString()).child(System.currentTimeMillis().toString())
            try {

                val bmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                val data = baos.toByteArray()

                reference.putBytes(data).await()

            } catch (e: Exception) {
                try {
                    reference.putFile(uri).await()
                } catch (e: Exception) {

                }
            }

            val post = PostModel(auth.uid.toString(), binding.postDescription.text.toString(), System.currentTimeMillis())
            try {
                val downLoadUrl = reference.downloadUrl.await()
                post.postImage = downLoadUrl.toString()
            } catch (e: Exception) {

            }
            db.collection("posts").document().set(post).await()
            db.collection("posts").addSnapshotListener { snapshot, e ->
                val documents = snapshot?.documents
                documents?.forEach{
                    val posts = it.toObject(PostModel::class.java)!!
                    posts.postId = it.id
                    db.collection("posts").document(it.id).set(posts)
                }

            }
            withContext(Dispatchers.Main) {
                dialog.dismiss()

                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.container, HomeFragment())
                (activity as MainActivity).binding.readableBottomBar.selectItem(0)
                transaction.commit()
            }
        }

        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.data != null) {
            try {
                uri = data.data as Uri
                binding.showPostImage.setImageURI(uri)
            } catch (e: Exception) {

            }

            binding.showPostImage.visibility = View.VISIBLE

            binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.follow_btn_bg))
            binding.postBtn.setTextColor(requireContext().resources.getColor(R.color.white))
            binding.postBtn.isEnabled = true
        }
    }

}