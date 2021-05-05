package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatLog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        sendMessage.setOnClickListener {
            performSendMessage()
        }

    }

    class ChatMessage(val id: String,val text: String,val fromId: String,val toId: String,val timestamp: Long){
        constructor() : this("","","","",-1)
    }

    private fun performSendMessage() {
        val text = enterMessage.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return

        if (toId == null) return
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!,text,fromId,toId,System.currentTimeMillis()/1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Toast.makeText(this,"Message Sent",Toast.LENGTH_SHORT).show()
                enterMessage.text.clear()
                chatLog.scrollToPosition(adapter.itemCount - 1)
            }.addOnFailureListener {
                Toast.makeText(this,"Message was not Sent Successfully",Toast.LENGTH_SHORT).show()
            }
        toReference.setValue(chatMessage)

        val messageActivityRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId").push()
        messageActivityRef.setValue(chatMessage)

        val messageActivityToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId").push()
        messageActivityToRef.setValue(chatMessage)
    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null ){
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        if (toUser != null){
                            val currentUser = MessageActivity.currentUser ?: return
                            adapter.add(ChatToItem(chatMessage.text,currentUser))
                        }
                    }else{
                        if (toUser != null){
                            adapter.add(ChatFromItem(chatMessage.text,toUser!!))
                        }
                    }
                }

                chatLog.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.fromMessage.text = text

        val uri = user.profileImageUrl;
        val targetImage = viewHolder.itemView.fromPhoto
        Picasso.get().load(uri).into(targetImage)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text:String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.toMessage.text = text

        val uri = user.profileImageUrl;
        val targetImage = viewHolder.itemView.selfPhoto
        Picasso.get().load(uri).into(targetImage)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}