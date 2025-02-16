package com.example.mystoryapp.ui.main.main1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.R
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.databinding.ActivityMainBinding
import com.example.mystoryapp.ui.main.main2.ViewModelFactory
import com.example.mystoryapp.ui.main.main2.WelcomeActivity
import com.example.mystoryapp.ui.maps.MapsActivity
import com.example.mystoryapp.ui.story.UploadStoryActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: MainAdapter
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyAdapter = MainAdapter()

        setupObservers()
        setupRecyclerView()
        setupFab()
        animateViews()
    }

    override fun onResume() {
        super.onResume()
        storyAdapter.refresh()
    }

    private fun setupObservers() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin && user.token.isBlank()) {
                navigateToWelcome()
            }
        }

        lifecycleScope.launch {
            viewModel.storyPager.collectLatest { pagingData ->
                Log.d("MainActivity", "Submitting new paging data to adapter")
                storyAdapter.submitData(pagingData)
            }
        }

        viewModel.isErr.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showToast(errorMessage)
            }
        }

        viewModel.needRefresh.observe(this) { needRefresh ->
            if (needRefresh) {
                storyAdapter.refresh()
            }
        }
    }


    private fun setupRecyclerView() {
        binding.rvUser.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPLOAD_RESULT_CODE && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Upload successful, refreshing stories")
            lifecycleScope.launch {
                viewModel.refreshStories()
                storyAdapter.refresh()
            }
        }
    }


    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, UploadStoryActivity::class.java))
        }
    }

    private fun animateViews() {
        binding.rvUser.translationY = -300f
        binding.rvUser.alpha = 0f
        binding.fabAdd.translationY = 300f
        binding.fabAdd.alpha = 0f

        binding.rvUser.animate().translationY(0f).alpha(1f).duration = 1000
        binding.fabAdd.animate().translationY(0f).alpha(1f).duration = 1000
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.map -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Logout")
            setMessage("Are you sure you want to log out?")
            setPositiveButton("Yes") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                viewModel.logout()
                navigateToWelcome()
            } catch (exception: Exception) {
                Log.e(TAG, "Logout failed: ${exception.message}", exception)
                showToast("Unable to logout. Please try again.")
            }
        }
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    companion object {
        private const val UPLOAD_RESULT_CODE = 100
        private const val TAG = "MainActivity"
    }
}