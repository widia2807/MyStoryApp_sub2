package com.example.mystoryapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.runner.Description
import org.junit.rules.TestWatcher

class RuleDispatcher {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    inner class MainDispatcherRule : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    val mainDispatcherRule = MainDispatcherRule()
}