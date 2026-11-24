package com.permata.sinemabebas.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.permata.sinemabebas.data.ArchiveRepository
import com.permata.sinemabebas.data.Title
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Paged list of titles for one category or one search query. */
class PagedList(private val load: suspend (page: Int) -> List<Title>) {
    val items = mutableStateListOf<Title>()
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    private var page = 0
    private var end = false

    suspend fun loadMore() {
        if (loading || end) return
        loading = true; error = null
        try {
            val next = load(page + 1)
            page++
            if (next.isEmpty()) end = true
            val known = items.map { it.id }.toHashSet()
            items += next.filter { known.add(it.id) }
        } catch (e: Exception) {
            error = e.message ?: e.javaClass.simpleName
        } finally {
            loading = false
        }
    }

    fun retryNeeded() { error = null }
}

class CatalogViewModel : ViewModel() {
    private val lists = HashMap<String, PagedList>()

    fun category(key: String): PagedList = lists.getOrPut(key) {
        val cat = ArchiveRepository.categories.first { it.key == key }
        PagedList { ArchiveRepository.browse(cat.query, it) }.also { l -> viewModelScope.launch { l.loadMore() } }
    }

    fun loadMore(list: PagedList) { viewModelScope.launch { list.loadMore() } }
    fun retry(list: PagedList) { list.retryNeeded(); loadMore(list) }

    // ---- Search (debounced) ----
    var query by mutableStateOf("")
        private set
    var searchList by mutableStateOf<PagedList?>(null)
        private set
    private var searchJob: Job? = null

    fun onQuery(q: String) {
        query = q
        searchJob?.cancel()
        if (q.isBlank()) { searchList = null; return }
        searchJob = viewModelScope.launch {
            delay(450)
            val l = PagedList { ArchiveRepository.search(q, it) }
            searchList = l
            l.loadMore()
        }
    }
}
