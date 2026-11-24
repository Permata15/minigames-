package com.permata.sinemabebas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.permata.sinemabebas.data.PlayItem
import com.permata.sinemabebas.data.Prefs
import com.permata.sinemabebas.data.Title
import com.permata.sinemabebas.ui.*

class MainActivity : ComponentActivity() {
    private val vm: CatalogViewModel by viewModels()
    private lateinit var prefs: Prefs
    private var history by mutableStateOf<List<Title>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        com.permata.sinemabebas.data.ArchiveRepository.minYear = prefs.minYear
        setContent { SinemaTheme { App() } }
    }

    override fun onResume() {
        super.onResume()
        history = prefs.history()
    }

    private fun play(items: List<PlayItem>, index: Int) {
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra(PlayerActivity.EXTRA_ITEMS, PlayItem.listToJson(items))
                .putExtra(PlayerActivity.EXTRA_INDEX, index),
        )
    }

    @Composable
    private fun App() {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var open by remember { mutableStateOf<Title?>(null) }
        var category by rememberSaveable { mutableStateOf<String?>(null) }

        Scaffold(
            containerColor = Bg,
            bottomBar = {
                if (open == null && category == null) NavigationBar(containerColor = Surface) {
                    listOf(
                        Triple("Beranda", Icons.Default.Home, 0),
                        Triple("Cari", Icons.Default.Search, 1),
                        Triple("Sumber Saya", Icons.AutoMirrored.Filled.List, 2),
                        Triple("Pengaturan", Icons.Default.Settings, 3),
                    ).forEach { (label, icon, i) ->
                        NavigationBarItem(
                            selected = tab == i, onClick = { tab = i },
                            icon = { Icon(icon, label) }, label = { Text(label) },
                        )
                    }
                }
            },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                val current = open
                if (current != null) {
                    DetailScreen(current, onBack = { open = null }) { detail, items, index ->
                        prefs.addHistory(detail.title)
                        play(items, index)
                    }
                } else if (category != null) {
                    CategoryScreen(vm, category!!, onBack = { category = null }) { open = it }
                } else when (tab) {
                    0 -> HomeScreen(vm, history, onOpen = { open = it }, onSeeAll = { category = it })
                    1 -> SearchScreen(vm) { open = it }
                    2 -> SourcesScreen(prefs) { items, i -> play(items, i) }
                    else -> SettingsScreen(prefs) { year ->
                        prefs.minYear = year
                        com.permata.sinemabebas.data.ArchiveRepository.minYear = year
                        vm.onQuery(vm.query) // refresh search results with the new year filter
                    }
                }
            }
        }
    }
}
