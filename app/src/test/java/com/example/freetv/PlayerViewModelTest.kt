package com.example.freetv

import com.example.freetv.screens.SharedTvViewModel
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PlayerViewModelTest {

    private lateinit var viewModel: SharedTvViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SharedTvViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSleepTimer activates timer and updates remaining time`() = runTest {
        viewModel.startSleepTimer(1)
        
        assertTrue(viewModel.isTimerActive.value)
        assertEquals(60000L, viewModel.timeRemaining.value)

        advanceTimeBy(1001)
        runCurrent()
        
        assertEquals(59000L, viewModel.timeRemaining.value)
    }

    @Test
    fun `cancelSleepTimer deactivates timer`() = runTest {
        viewModel.startSleepTimer(5)
        viewModel.cancelSleepTimer()

        assertFalse(viewModel.isTimerActive.value)
        assertNull(viewModel.timeRemaining.value)
    }

    @Test
    fun `timer finishing emits finished event`() = runTest {
        viewModel.startSleepTimer(1)
        
        val events = mutableListOf<Unit>()
        val job = launch {
            viewModel.timerFinishedEvent.collect {
                events.add(it)
            }
        }

        advanceTimeBy(61000)
        runCurrent()

        assertFalse(viewModel.isTimerActive.value)
        assertEquals(1, events.size)
        
        job.cancel()
    }
    
    @Test
    fun `starting new timer replaces old one`() = runTest {
        viewModel.startSleepTimer(10)
        assertEquals(600000L, viewModel.timeRemaining.value)
        
        viewModel.startSleepTimer(1)
        assertEquals(60000L, viewModel.timeRemaining.value)
    }
}
