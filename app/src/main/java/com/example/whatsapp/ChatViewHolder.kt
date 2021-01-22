package com.example.whatsapp

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.databinding.ActivityChatBinding
import com.example.whatsapp.databinding.FragmentChatsBinding
import com.example.whatsapp.databinding.ListItemBinding
import com.example.whatsapp.model.Inbox
import com.example.whatsapp.utils.formatAsListItem
import com.squareup.picasso.Picasso

class ChatViewHolder(private val itembinding: ListItemBinding) : RecyclerView.ViewHolder(itembinding.root) {

    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itembinding) {
            countTv.isVisible = item.count > 0
            countTv.text = item.count.toString()
            timeTv.text = item.time.formatAsListItem(root.context)

            titleTv.text = item.name
            subTitleTv.text = item.msg
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.defaultavatar)
                .error(R.drawable.defaultavatar)
                .into(userImgView)
            root.setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}