package com.example.mystoryapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlinx.coroutines.test.runTest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystoryapp.data.dao.ListStoriesAdapter
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.ListStoryItemLocal
import com.example.mystoryapp.ui.main.main1.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import org.junit.Assert

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
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
        val dummyQuote = Dummy.generateDummyQuoteResponse()
        val data: PagingData<ListStoryItemLocal> = QuotePagingSource.snapshot(dummyQuote)
        val expectedQuote = MutableLiveData<PagingData<ListStoryItemLocal>>()
        expectedQuote.value = data
        Mockito.`when`(storyManager.getStoriesPaging()).thenReturn(expectedQuote)

        val mainViewModel = MainViewModel(userManager, storyManager)
        val actualQuote = mainViewModel.getStoryPager().first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyQuote.size, differ.snapshot().size)
        Assert.assertEquals(dummyQuote[0], differ.snapshot()[0])
    }

    companion object {
        private val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItemLocal> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<ListStoryItemLocal>>()
        expectedQuote.value = data
        Mockito.`when`(storyManager.getStoriesPaging()).thenReturn(expectedQuote)
        val mainViewModel = MainViewModel(userManager, storyManager)
        val actualQuote = mainViewModel.getStoryPager().first()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

class QuotePagingSource : PagingSource<Int, ListStoryItemLocal>() {
    companion object {
        fun snapshot(items: List<ListStoryItemLocal>): PagingData<ListStoryItemLocal> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItemLocal>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItemLocal> {
        return LoadResult.Page(emptyList(), null, null)
    }
}

