package com.albertkingdom.mybusmap.ui

import org.mockito.Mock
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.repository.FavoriteRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ArrivalTimeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var favoriteRepository: FavoriteRepositoryInterface

    private lateinit var viewModel: ArrivalTimeViewModel

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        viewModel = ArrivalTimeViewModel(favoriteRepository)
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `test checkIfSignIn updates isLogin LiveData`() = runTest {
        `when`(favoriteRepository.checkIsLogin()).thenReturn(true)

        viewModel.checkIfSignIn()

        assertEquals(true, viewModel.isLogin.value)
    }

    @Test
    fun `test getFavoriteRouteFromRemote updates listOfFavorite LiveData`() = runTest {
        val mockFavorites = listOf(
            Favorite("Route 1", "Station 1"),
            Favorite("Route 2", "Station 2")
        )
        `when`(favoriteRepository.getFavoritesFromRemote()).thenReturn(mockFavorites)

        val observer = mock(Observer::class.java) as Observer<List<Favorite>>
        viewModel.listOfFavorite.observeForever(observer)

        viewModel.getFavoriteRouteFromRemote()

        verify(observer).onChanged(mockFavorites)
    }

    @Test
    fun `test saveToRemote calls repository method`() = runTest {
        val routeName = "Test Route"

        viewModel.saveToRemote(routeName)

        verify(favoriteRepository).saveFavToRemote(routeName)
    }

    @Test
    fun `test removeFromRemote calls repository method`() = runTest {
        val routeName = "Test Route"

        viewModel.removeFromRemote(routeName)

        verify(favoriteRepository).deleteFavFromRemote(routeName)
    }

    @Test
    fun `test getFromDB updates listOfFavorite LiveData`() = runTest {
        val mockFavorites = listOf(
            Favorite("Route 1", "Station 1"),
            Favorite("Route 2", "Station 2")
        )
        `when`(favoriteRepository.getFavoritesFromLocal()).thenReturn(mockFavorites)

        val observer = mock(Observer::class.java) as Observer<List<Favorite>>
        viewModel.listOfFavorite.observeForever(observer)

        viewModel.getFromDB()

        verify(observer).onChanged(mockFavorites)
    }

    @Test
    fun `test saveToDB calls repository method`() {
        val routeName = "Test Route"

        viewModel.saveToDB(routeName)

        verify(favoriteRepository).saveToLocal(routeName)
    }

    @Test
    fun `test removeFromDB calls repository method`() {
        val routeName = "Test Route"

        viewModel.removeFromDB(routeName)

        verify(favoriteRepository).deleteFromLocal(routeName)
    }
}