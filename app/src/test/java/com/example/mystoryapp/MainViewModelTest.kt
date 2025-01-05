package com.example.mystoryapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.ui.main.main1.MainAdapter
import com.example.mystoryapp.ui.main.main1.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()


    @Mock
    private lateinit var storyManager: StoryManager

    @Mock
    private lateinit var userManager: UserManager

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStories = Dummy.generateDummyStories()
        val expectedFlow: Flow<PagingData<ListStoryItem>> = flow {
            emit(PagingData.from(dummyStories))
        }

        Mockito.`when`(storyManager.getStoriesPaging()).thenReturn(expectedFlow)

        val mainViewModel = MainViewModel(userManager, storyManager)
        val actualPagingData = mainViewModel.storyPager.first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.getDiffCallback(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        differ.submitData(actualPagingData)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val expectedFlow: Flow<PagingData<ListStoryItem>> = flow {
            emit(PagingData.from(emptyList()))
        }

        Mockito.`when`(storyManager.getStoriesPaging()).thenReturn(expectedFlow)

        val mainViewModel = MainViewModel(userManager, storyManager)
        val actualPagingData = mainViewModel.storyPager.first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.getDiffCallback(),
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        differ.submitData(actualPagingData)

        Assert.assertEquals(0, differ.snapshot().size)
    }

    companion object {
        private val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }
}
