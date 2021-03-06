package com.example.whatsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.ChatActivity
import com.example.whatsapp.EmptyViewHolder
import com.example.whatsapp.R
import com.example.whatsapp.UsersViewHolder
import com.example.whatsapp.databinding.ListItemBinding
import com.example.whatsapp.model.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.Exception



private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class PeopleFragment : Fragment() {

    lateinit var mRecyclerView: RecyclerView

    lateinit var mAdapter:FirestorePagingAdapter<User,RecyclerView.ViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name",Query.Direction.DESCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chats,container,false)
    }

    private fun setupAdapter(){
        val config=PagedList.Config.Builder()
            .setPrefetchDistance(2)
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()
        val options=FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database,config, User::class.java)
            .build()
        mAdapter=object :FirestorePagingAdapter<User,RecyclerView.ViewHolder>(options){
            private lateinit var binding: ListItemBinding
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                binding= ListItemBinding.inflate(layoutInflater)
                return when (viewType) {
                    NORMAL_VIEW_TYPE -> {
                        UsersViewHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                    }
                    else -> EmptyViewHolder(layoutInflater.inflate(R.layout.empty_view, parent, false))

                }

            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UsersViewHolder){
                    holder.bind(user = model) { name: String, photo: String, id: String ->

                        startActivity(
                            ChatActivity.createChatActivity(
                                requireContext(),
                                id,
                                name,
                                photo
                            )
                        )
                    }
                }else{
//                    Todo -> Something
                }

            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                    }

                    LoadingState.LOADING_MORE -> {
                    }

                    LoadingState.LOADED -> {
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    LoadingState.FINISHED -> {
                    }
                }
            }
            override fun onError(e: Exception) {
                super.onError(e)
            }
            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView=view.findViewById(R.id.recyclerView)
        mRecyclerView.apply{
            layoutManager=LinearLayoutManager(requireContext())
            adapter=mAdapter } }
}


