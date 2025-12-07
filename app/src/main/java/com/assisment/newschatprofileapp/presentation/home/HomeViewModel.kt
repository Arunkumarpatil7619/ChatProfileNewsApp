package com.assisment.newschatprofileapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.domain.usecase.GetCachedArticlesUseCase
import com.assisment.newschatprofileapp.domain.usecase.GetTopHeadlinesUseCase
import com.assisment.newschatprofileapp.domain.usecase.SearchArticlesUseCase
import com.assisment.newschatprofileapp.presentation.common.ConnectivityObserver
import com.assisment.newschatprofileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val getCachedArticlesUseCase: GetCachedArticlesUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null
    private var headlinesJob: Job? = null
    private var cacheJob: Job? = null
    private var connectivityJob: Job? = null

    init {
        observeConnectivity()
        loadInitialData()
    }

    private fun loadInitialData() {
        // Start by observing cache, so we immediately show something when offline
        observeCachedArticles()
        // Try to load headlines (will fall back to cache inside repository if needed)
        loadTopHeadlines()
    }

    private fun observeConnectivity() {
        connectivityJob?.cancel()
        connectivityJob = viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val isOnline = status == ConnectivityObserver.Status.Available

                // Update state as soon as we know connectivity
                _state.update {
                    it.copy(isOnline = isOnline)
                }

                if (isOnline) {
                    // Always refresh when coming online (force refresh to get fresh data)
                    // Cancel any running cache observer while we refresh from network
                    cacheJob?.cancel()
                    loadTopHeadlines(forceRefresh = true)
                } else {
                    // When going offline, observe cached articles
                    observeCachedArticles()
                }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> {
                if (_state.value.isOnline) {
                    loadTopHeadlines(forceRefresh = true)
                } else {
                    // When offline, reload cached data
                    observeCachedArticles()
                }
            }
            is HomeEvent.Search -> {
                _searchQuery.value = event.query
                searchArticles(event.query)
            }
            is HomeEvent.ClearSearch -> {
                _searchQuery.value = ""
                _state.update {
                    it.copy(
                        searchResults = emptyList(),
                        searchState = Resource.Success(emptyList())
                    )
                }
                // Reload appropriate data
                if (_state.value.isOnline) {
                    loadTopHeadlines()
                } else {
                    observeCachedArticles()
                }
            }
            is HomeEvent.LoadMore -> {
                if (!_state.value.isLoadingMore &&
                    _state.value.canLoadMore &&
                    _state.value.isOnline) {
                    loadMoreArticles()
                }
            }
            is HomeEvent.ToggleFeatured -> {
                _state.update {
                    it.copy(showFeaturedSection = event.show)
                }
            }
        }
    }

    private fun loadTopHeadlines(forceRefresh: Boolean = false) {
        headlinesJob?.cancel()
        headlinesJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isRefreshing = forceRefresh,
                    error = null,
                    // if forcing refresh we keep currentPage reset to 1
                    currentPage = 1
                )
            }

            getTopHeadlinesUseCase(
                page = 1,
                forceRefresh = forceRefresh
            ).catch { e ->
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to load articles",
                        isLoading = false,
                        isRefreshing = false,
                        articles = Resource.Error(e.message ?: "Failed to load")
                    )
                }
                // If online loading fails, try to show cached data
                observeCachedArticles()
            }.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val articles = resource.data ?: emptyList()
                        val featured = articles.take(5).map { it.copy(isFeatured = true) }
                        val regular = articles.drop(5)

                        _state.update {
                            it.copy(
                                articles = Resource.Success(regular),
                                featuredArticles = featured,
                                currentPage = 1,
                                canLoadMore = articles.size == 20, // API returns 20 per page
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                articles = resource,
                                isLoading = false,
                                isRefreshing = false,
                                error = resource.message
                            )
                        }
                        // If online fails, show cached data
                        observeCachedArticles()
                    }
                    is Resource.Loading -> {
                        // Loading handled above
                    }
                }
            }
        }
    }

    private fun observeCachedArticles() {
        cacheJob?.cancel()
        cacheJob = viewModelScope.launch {
            getCachedArticlesUseCase()
                .catch { e ->
                    _state.update {
                        it.copy(
                            cachedArticles = emptyList(),
                            error = if (!_state.value.isOnline) "No cached articles available" else null
                        )
                    }
                }
                .collect { cachedArticles ->
                    // Only show cached when offline OR when there is no online article data
                    val shouldShowCache = !_state.value.isOnline || _state.value.articles is Resource.Loading || _state.value.articles.data.isNullOrEmpty()
                    if (shouldShowCache) {
                        val featured = cachedArticles.take(5).map { it.copy(isFeatured = true) }
                        val regular = cachedArticles.drop(5)

                        _state.update {
                            it.copy(
                                cachedArticles = cachedArticles,
                                featuredArticles = featured,
                                articles = Resource.Success(regular),
                                canLoadMore = false, // No pagination for cached
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                }
        }
    }

    private fun searchArticles(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotEmpty()) {
                _state.update {
                    it.copy(
                        searchState = Resource.Loading(),
                        isLoading = true
                    )
                }

                delay(300) // Debounce

                searchArticlesUseCase(query)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                searchState = Resource.Error(e.message ?: "Search failed"),
                                searchResults = emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    .collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val results = resource.data ?: emptyList()
                                _state.update {
                                    it.copy(
                                        searchState = resource,
                                        searchResults = results,
                                        isLoading = false,
                                        canLoadMore = false
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _state.update {
                                    it.copy(
                                        searchState = resource,
                                        searchResults = emptyList(),
                                        isLoading = false
                                    )
                                }
                            }
                            is Resource.Loading -> {
                                // Loading handled by isLoading
                            }
                        }
                    }
            } else {
                _state.update {
                    it.copy(
                        searchState = Resource.Success(emptyList()),
                        searchResults = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadMoreArticles() {
        if (_state.value.isLoadingMore || !_state.value.canLoadMore || !_state.value.isOnline) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoadingMore = true)
            }

            try {
                val nextPage = _state.value.currentPage + 1

                getTopHeadlinesUseCase(
                    page = nextPage,
                    forceRefresh = false
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val newArticles = resource.data ?: emptyList()
                            val currentArticles = _state.value.articles.data ?: emptyList()

                            _state.update {
                                it.copy(
                                    articles = Resource.Success(currentArticles + newArticles),
                                    currentPage = nextPage,
                                    canLoadMore = newArticles.size == 20, // API returns 20 per page
                                    isLoadingMore = false
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    isLoadingMore = false,
                                    error = resource.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            // Loading handled by isLoadingMore
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Failed to load more"
                    )
                }
            }
        }
    }
}

sealed class HomeEvent {
    object Refresh : HomeEvent()
    data class Search(val query: String) : HomeEvent()
    object ClearSearch : HomeEvent()
    object LoadMore : HomeEvent()
    data class ToggleFeatured(val show: Boolean) : HomeEvent()
}

data class HomeState(
    val articles: Resource<List<Article>> = Resource.Loading(),
    val cachedArticles: List<Article> = emptyList(),
    val featuredArticles: List<Article> = emptyList(),
    val searchResults: List<Article> = emptyList(),
    val searchState: Resource<List<Article>> = Resource.Success(emptyList()),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val showFeaturedSection: Boolean = true,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false,
    val error: String? = null
)
