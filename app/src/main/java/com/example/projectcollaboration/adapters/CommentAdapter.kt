package com.example.projectcollaboration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcollaboration.R
import com.example.projectcollaboration.models.Comment
import com.example.projectcollaboration.utils.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvCommentText: TextView = view.findViewById(R.id.tvCommentText)
        val tvCommentTime: TextView = view.findViewById(R.id.tvCommentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // Set comment text
        holder.tvCommentText.text = comment.text

        // Format and set time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.tvCommentTime.text = dateFormat.format(Date(comment.timestamp))

        // Get and set user name
        FirebaseUtils.getUserById(comment.userId) { user ->
            if (user != null) {
                holder.tvUserName.text = user.name
            } else {
                holder.tvUserName.text = "Unknown User"
            }
        }
    }

    override fun getItemCount() = comments.size
}
