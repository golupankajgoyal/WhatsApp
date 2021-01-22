package com.example.whatsapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp.adapters.ChatAdapter
import com.example.whatsapp.databinding.ActivityChatBinding
import com.example.whatsapp.model.*
import com.example.whatsapp.utils.KeyboardVisibilityUtil
import com.example.whatsapp.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider

const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"

class ChatActivity : AppCompatActivity() {

    private val mLinearLayout: LinearLayoutManager by lazy { LinearLayoutManager(this) }
    lateinit var chatAdapter:ChatAdapter
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private lateinit var binding: ActivityChatBinding
    private val friendId: String by lazy {
        intent.getStringExtra(USER_ID)
    }
    private val name: String by lazy {
        intent.getStringExtra(USER_NAME)
    }
    private val image: String by lazy {
        intent.getStringExtra(USER_THUMB_IMAGE)
    }
    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    private lateinit var currentUser: User
    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding=ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keyboardVisibilityHelper = KeyboardVisibilityUtil(binding.rootView) {
            binding.msgRv.scrollToPosition(mutableItems.size - 1)
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }

        chatAdapter = ChatAdapter(mutableItems, mCurrentUid)

        binding.msgRv.apply {
            layoutManager = mLinearLayout
            adapter = chatAdapter
        }

        val emojiPopup = EmojiPopup.Builder.fromRootView(binding.rootView).build(binding.msgEdtv)
        binding.smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }

        listenMessages()
        { msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }

//        binding.swipeToLoad.setOnRefreshListener {
//            val workerScope = CoroutineScope(Dispatchers.Main)
//            workerScope.launch {
//                delay(2000)
//                swipeToLoad.isRefreshing = false
//            }
//        }

        binding.apply {
            nameTv.text = name
            Picasso.get().load(image).into(userImgView)
        }

        binding.sendBtn.setOnClickListener {
            binding.msgEdtv.text?.let {
                if (it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }

    }

    private fun updateMessage(msg: Message) {
        val position = mutableItems.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        mutableItems[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key
        checkNotNull(id) { "Cannot be null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {

        }.addOnFailureListener {

        }
        updateLastMessage(msgMap,mCurrentUid)
    }

    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status)).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this,"Failed To Liked a Message",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLastMessage(message: Message, mCurrentUid: String) {
        val inboxMap = Inbox(
            message.msg,
            friendId,
            name,
            image,
            message.sentAt,
            0
        )

        getInbox(mCurrentUid, friendId).setValue(inboxMap)

        getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = message.senderId
                    name = currentUser.name
                    image = currentUser.thumbImage
                    count = 1
                }
                if (value?.from == message.senderId) {
                    inboxMap.count = value.count + 1
                }
                getInbox(friendId, mCurrentUid).setValue(inboxMap)
            }

        })
    }


    private fun listenMessages(newMsg: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(data: DataSnapshot, p1: String?) {

                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, true)
                }

                override fun onChildAdded(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, false)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }

            })

    }

    private fun addMessage(event: Message) {
        val eventBefore = mutableItems.lastOrNull()

        // Add date header if it's a different day
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(event.sentAt)) || eventBefore == null) {
            mutableItems.add(DateHeader(event.sentAt, this))
        }
        mutableItems.add(event)

        chatAdapter.notifyItemInserted(mutableItems.size)
        binding.msgRv.scrollToPosition(mutableItems.size + 1)
    }


    private fun updateReadCount() {
        getInbox(mCurrentUid, friendId).child("count").setValue(0)
    }

    private fun getMessages(friendId: String) = db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")


    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    override fun onResume() {
        super.onResume()
        binding.rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        binding.rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }



    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(USER_ID, id)
            intent.putExtra(USER_NAME, name)
            intent.putExtra(USER_THUMB_IMAGE, image)

            return intent
        }
    }

}