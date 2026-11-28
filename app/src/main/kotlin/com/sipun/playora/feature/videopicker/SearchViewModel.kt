package com.sipun.playora.feature.videopicker.screens.search

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.domain.GetPopularFoldersUseCase
import com.sipun.playora.core.domain.SearchMediaUseCase
import com.sipun.playora.core.domain.SearchResults
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.ui.base.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SearchViewModel(
    private val searchMediaUseCase: SearchMediaUseCase,
    private val getPopularFoldersUseCase: GetPopularFoldersUseCase,
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<SearchUiState, SearchUiEvent>() {

    data class Output(
        val navigateUp: () -> Unit,
        val playVideo: (uri: Uri, playlist: List<Uri>?) -> Unit,
        val openFolder: (String) -> Unit,
    )

    private val stateInternal = MutableStateFlow(value = SearchUiState())
    override val state: StateFlow<SearchUiState> = stateInternal.asStateFlow()

    private val searchRequests = MutableStateFlow(value = "" to false)

    init {
        collectPopularFolders()
        collectPreferences()
        collectSearchResults()
    }

    private fun collectPopularFolders() {
        viewModelScope.launch {
            getPopularFoldersUseCase(limit = 5).collect { folders ->
                stateInternal.update { it.copy(popularFolders = folders) }
            }
        }
    }

    private fun collectPreferences() {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { prefs ->
                stateInternal.update { it.copy(preferences = prefs) }
            }
        }
    }

    @OptIn(
        FlowPreview::class,
        ExperimentalCoroutinesApi::class,
    )
    private fun collectSearchResults() {
        viewModelScope.launch {
            searchRequests
                .debounce { (_, immediate) -> if (immediate) 0L else SEARCH_DEBOUNCE_MS }
                .map { (query, _) -> query }
                .distinctUntilChanged()
                .flatMapLatest { query -> searchMediaUseCase(query) }
                .collect { results ->
                    stateInternal.update {
                        it.copy(
                            searchResults = results,
                            isSearching = false,
                        )
                    }
                }
        }
    }

    override fun onAction(action: SearchUiEvent) {
        when (action) {
            is SearchUiEvent.NavigateUp -> output.navigateUp()
            is SearchUiEvent.PlayVideo -> output.playVideo(action.uri, action.playlist)
            is SearchUiEvent.OpenFolder -> output.openFolder(action.path)
            is SearchUiEvent.OnQueryChange -> onQueryChange(action.query)
            is SearchUiEvent.OnSearch -> onSearch(action.query)
        }
    }

    private fun onQueryChange(query: String) {
        stateInternal.update {
            it.copy(
                query = query,
                isSearching = query.isNotBlank(),
            )
        }
        searchRequests.value = query to false
    }

    private fun onSearch(query: String) {
        val alreadyLoadedForQuery = !stateInternal.value.isSearching &&
            stateInternal.value.query == query &&
            searchRequests.value.first == query
        stateInternal.update {
            it.copy(
                query = query,
                isSearching = query.isNotBlank() && !alreadyLoadedForQuery,
            )
        }
        searchRequests.value = query to true
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}

@Stable
data class SearchUiState(
    val query: String = "",
    val popularFolders: List<Folder> = emptyList(),
    val searchResults: SearchResults = SearchResults(),
    val isSearching: Boolean = false,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface SearchUiEvent {
    data object NavigateUp : SearchUiEvent
    data class PlayVideo(val uri: Uri, val playlist: List<Uri>? = null) : SearchUiEvent
    data class OpenFolder(val path: String) : SearchUiEvent

    data class OnQueryChange(val query: String) : SearchUiEvent
    data class OnSearch(val query: String) : SearchUiEvent
}
