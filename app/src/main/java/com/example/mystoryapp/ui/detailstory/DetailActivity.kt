package com.example.mystoryapp.ui.detailstory

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.databinding.ActivityDetailBinding
import com.example.mystoryapp.ui.main.main1.MainViewModel
import com.example.mystoryapp.ui.main.main2.ViewModelFactory
import com.example.mystoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID) ?: run {
            Log.e(TAG, "No Story ID passed to activity")
            finish()
            return
        }

        displayStoryDetails(storyId)
    }

    private fun displayStoryDetails(storyId: String) {
        showLoading(true)
        showError(false)

        lifecycleScope.launch {
            try {
                val storyDetails = viewModel.fetchStoryDetail(storyId)
                bindDataToUI(storyDetails)
                showLoading(false)
            } catch (exception: Exception) {
                showLoading(false)
                when (exception) {
                    is UnknownHostException -> {
                        showError(true, "No internet connection. Please check your network and try again.")
                    }
                    else -> {
                        showError(true, "Failed to load story: ${exception.localizedMessage}")
                    }
                }
                Log.e(TAG, "Failed to load story details: ${exception.localizedMessage}", exception)
            }
        }
    }

    private fun bindDataToUI(storyDetail: DetailStoryResponse) {
        storyDetail.story?.let { story ->
            with(binding) {
                contentGroup.visibility = View.VISIBLE
                tvItemNameDesc.text = story.name
                tvItemDescriptionDesc.text = story.description
                Glide.with(this@DetailActivity)
                    .load(story.photoUrl)
                    .into(imgItemPhotoDesc)
            }
        } ?: run {
            showError(true, "Story data is not available")
            Log.e(TAG, "Story data is null")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(isError: Boolean, message: String = "") {
        with(binding) {
            errorGroup.visibility = if (isError) View.VISIBLE else View.GONE
            contentGroup.visibility = if (isError) View.GONE else View.VISIBLE
            tvError.text = message
            btnRetry.setOnClickListener {
                intent.getStringExtra(EXTRA_STORY_ID)?.let { storyId ->
                    displayStoryDetails(storyId)
                }
            }
        }
    }

    companion object {
        private const val TAG = "DetailActivity"
        const val EXTRA_STORY_ID = "STORY_ID"
    }
}