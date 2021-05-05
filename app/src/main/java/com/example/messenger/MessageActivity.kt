package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_activity_row.view.*

class MessageActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        displayMessageActivity.adapter = adapter
        displayMessageActivity.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatActivity::class.java)

            val row = item as MessageActivityRow
            intent.putExtra(NewMessage.USER_KEY,row.chatPartnerUser)

            startActivity(intent)
        }
        
        listenForMessages()

        fetchCurrentUser()

        verifyUserLogin()
    }

    class MessageActivityRow(val chatMessage: ChatActivity.ChatMessage): Item<GroupieViewHolder>(){

        var chatPartnerUser: User? = null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.messageMessageActivity.text = chatMessage.text

            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)

                    viewHolder.itemView.usernameMessageActivity.text = chatPartnerUser?.username

                    val targetImage = viewHolder.itemView.imageMessageActivity

                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImage)

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        override fun getLayout(): Int {
            return R.layout.message_activity_row
        }

    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    val messageMap = HashMap<String, ChatActivity.ChatMessage>()

    private fun refreshMessages(){
        adapter.clear()
        messageMap.values.forEach {
            adapter.add(MessageActivityRow(it))
        }
    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatActivity.ChatMessage::class.java) ?: return

                messageMap[snapshot.key!!] = chatMessage
                refreshMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatActivity.ChatMessage::class.java) ?: return

                messageMap[snapshot.key!!] = chatMessage
                refreshMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun verifyUserLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this,SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.newMessage -> {
                val intent = Intent(this,NewMessage::class.java)
                startActivity(intent)
            }
            R.id.btnSignout -> {

                val intent = Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}