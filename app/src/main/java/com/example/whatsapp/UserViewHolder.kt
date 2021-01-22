package com.example.whatsapp

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.databinding.ListItemBinding
import com.example.whatsapp.model.User

import com.squareup.picasso.Picasso

class UsersViewHolder( private val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(user: User, onClick: (name: String, photo: String, id: String) -> Unit) {
        with(itemBinding){
            countTv.isVisible = false
            timeTv.isVisible = false
            titleTv.text = user.name
            subTitleTv.text = user.status
            Picasso.get()
                .load(user.thumbImage)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            root.setOnClickListener {
                onClick.invoke(user.name, user.thumbImage, user.uid)
            }
        }
    }

    }


class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)