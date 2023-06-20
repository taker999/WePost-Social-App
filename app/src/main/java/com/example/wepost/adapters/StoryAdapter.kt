package com.example.wepost.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wepost.R
import com.example.wepost.databinding.StoryRvDesignBinding
import com.example.wepost.models.StoryModel
import com.example.wepost.models.UserModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory


class StoryAdapter(options: FirestoreRecyclerOptions<StoryModel>) : FirestoreRecyclerAdapter<StoryModel, StoryAdapter.StoryViewHolder>(
    options
) {

    private lateinit var context: Context

    class StoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = StoryRvDesignBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val viewHolder = StoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.story_rv_design, parent, false))
        context = parent.context
        return viewHolder
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int, model: StoryModel) {

        CoroutineScope(Dispatchers.IO).launch {
            if (model.stories.size != 0) {
                val lastStory = model.stories[model.stories.size - 1]


                val db = FirebaseFirestore.getInstance()
                val usersCollection = db.collection("users")

                val user = usersCollection.document(model.storyBy).get().await()
                    .toObject(UserModel::class.java)!!

                withContext(Dispatchers.Main) {
                    try {
                        Picasso.get().load(lastStory.image).resize(500, 500).placeholder(R.drawable.placeholder)
                            .into(holder.binding.storyImage)
                    } catch (e: Exception) {

                    }
                    try {
                        Picasso.get().load(user.profilePhoto).resize(100, 100).placeholder(R.drawable.user)
                            .into(holder.binding.profilePic)
                    } catch (e: Exception) {

                    }
                    holder.binding.statusCircle.setPortionsCount(model.stories.size)
                    holder.binding.userName.text = user.name

                    holder.binding.storyImage.setOnClickListener {
                        val myStories: ArrayList<MyStory> = ArrayList()

                        for (userStories in model.stories) {
                            myStories.add(
                                MyStory(
                                    userStories.image
                                )
                            )
                        }

                        StoryView.Builder((context as AppCompatActivity).supportFragmentManager)
                            .setStoriesList(myStories)// Required
                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(user.name) // Default is Hidden
                            .setSubtitleText("") // Default is Hidden
                            .setTitleLogoUrl(user.profilePhoto) // Default is Hidden
                            .setStoryClickListeners(object : StoryClickListeners {
                                override fun onDescriptionClickListener(position: Int) {
                                    //your action
                                }

                                override fun onTitleIconClickListener(position: Int) {
                                    //your action
                                }
                            }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show()
                    }
                }

            }
        }

    }

}