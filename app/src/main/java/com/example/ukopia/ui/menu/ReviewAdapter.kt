package com.example.ukopia.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukopia.R
import com.example.ukopia.models.ReviewApiItem
import java.util.Locale

class ReviewAdapter(private var reviews: List<ReviewApiItem>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.tv_review_author)
        val rating: TextView = itemView.findViewById(R.id.tv_review_rating)
        val comment: TextView = itemView.findViewById(R.id.tv_review_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.author.text = review.nama
        holder.rating.text = String.format(Locale.ROOT, "%.1f", review.rating)
        holder.comment.text = review.komentar
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<ReviewApiItem>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}