package com.example.mystoryapp.data.dao

import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.paging.PagingDataAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mystoryapp.R
import com.example.mystoryapp.data.response.ListStoryItemLocal
import com.example.mystoryapp.databinding.ItemStoryBinding
import com.example.mystoryapp.ui.detailstory.DetailActivity

class ListStoriesAdapter : PagingDataAdapter<ListStoryItemLocal, ListStoriesAdapter.ListStoriesHolder>(
    DIFF_CALLBACK
) {
    class ListStoriesHolder(private var binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(stories: ListStoryItemLocal) {
            stories.photoUrl.let {
                Glide.with(binding.imgItemPhoto.context)
                    .load(it)
                    .placeholder(R.drawable.failed_img)
                    .timeout(5000)
                    .error(R.drawable.failed_img)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.imgItemPhoto)
            }
            binding.tvItemName.text = stories.name
            binding.tvItemDescription.text = stories.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoriesHolder {
        val binding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListStoriesHolder(binding)
    }

    override fun onBindViewHolder(holder: ListStoriesHolder, position: Int) {
        val stories = getItem(position)
        Log.d("Adapter", "Item at position $position: $stories")
        if (stories != null) {
            holder.bind(stories)

            holder.itemView.setOnClickListener {
                val intentDetail = Intent(holder.itemView.context, DetailActivity::class.java)
                intentDetail.putExtra("detailId", stories.id)
                holder.itemView.context.startActivity(intentDetail)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItemLocal>() {
            override fun areItemsTheSame(oldItem: ListStoryItemLocal, newItem: ListStoryItemLocal): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItemLocal, newItem: ListStoryItemLocal): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}