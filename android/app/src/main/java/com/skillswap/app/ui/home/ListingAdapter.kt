package com.skillswap.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skillswap.app.R
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
            val context = binding.root.context
            binding.tvTitle.text = listing.title
            binding.tvCategory.text = listing.category
            binding.tvDescription.text = listing.description.orEmpty()

            val ownerName = listing.ownerId?.name
            if (!ownerName.isNullOrBlank()) {
                binding.tvOwner.text = context.getString(R.string.offered_by, ownerName)
                binding.tvOwner.visibility = View.VISIBLE
            } else {
                binding.tvOwner.visibility = View.GONE
            }

            if (!listing.imageUrl.isNullOrBlank()) {
                binding.tvImageUrl.text = context.getString(R.string.blob_asset, listing.imageUrl)
                binding.tvImageUrl.visibility = View.VISIBLE
            } else {
                binding.tvImageUrl.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SkillListing>() {
            override fun areItemsTheSame(old: SkillListing, new: SkillListing) = old._id == new._id
            override fun areContentsTheSame(old: SkillListing, new: SkillListing) = old == new
        }
    }
}
