package com.skillswap.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skillswap.app.data.model.SkillListing
import com.skillswap.app.databinding.ItemListingBinding

class ListingAdapter : ListAdapter<SkillListing, ListingAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listing: SkillListing) {
            binding.tvTitle.text = listing.title
            binding.tvCategory.text = listing.category
            binding.tvDescription.text = listing.description.orEmpty()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SkillListing>() {
            override fun areItemsTheSame(old: SkillListing, new: SkillListing) = old._id == new._id
            override fun areContentsTheSame(old: SkillListing, new: SkillListing) = old == new
        }
    }
}
