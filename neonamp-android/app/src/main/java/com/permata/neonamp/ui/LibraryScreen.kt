package com.permata.neonamp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.permata.neonamp.data.Kind
import com.permata.neonamp.data.Playlist
import com.permata.neonamp.data.Track

private enum class SortMode(val label: String) { NEWEST("Terbaru"), TITLE("Judul A–Z"), ARTIST("Artis A–Z"), LONGEST("Terpanjang") }

/** Kelompok yang sedang dibuka: "all", "artists", "artist:<nama>", "pl:<id>". */
private const val ALL = "all"
private const val ARTISTS = "artists"

private fun artistOf(t: Track) = t.artist.ifBlank { "Tanpa artis" }

// ---------------- Tab: Audio offline ----------------
@Composable
internal fun AudioTab(vm: PlayerViewModel, ui: PlayerUi) {
    val library by vm.audioLibrary.collectAsStateWithLifecycle()
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf(ALL) }
    var sort by rememberSaveable { mutableStateOf(SortMode.NEWEST) }
    var sortMenu by remember { mutableStateOf(false) }
    var newGroupFor by remember { mutableStateOf<Track?>(null) }
    var showNewGroup by remember { mutableStateOf(false) }
    var addToGroup by remember { mutableStateOf<Track?>(null) }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var confirmDelete by remember { mutableStateOf<Playlist?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    val currentPlaylist = playlists.firstOrNull { "pl:${it.id}" == group }
    // Grup yang sudah dihapus → kembali ke semua
    if (group.startsWith("pl:") && currentPlaylist == null) group = ALL
    val byId = remember(library) { library.associateBy { it.id } }

    // Daftar lagu sesuai grup + pencarian + urutan
    val base: List<Track> = when {
        currentPlaylist != null -> currentPlaylist.trackIds.mapNotNull { byId[it] }
        group.startsWith("artist:") -> library.filter { artistOf(it) == group.removePrefix("artist:") }
        else -> library
    }
    val q = query.trim()
    val filtered = if (q.isEmpty()) base else base.filter {
        it.title.contains(q, ignoreCase = true) || it.artist.contains(q, ignoreCase = true)
    }
    val shown = when (sort) {
        SortMode.NEWEST -> filtered
        SortMode.TITLE -> filtered.sortedBy { it.title.lowercase() }
        SortMode.ARTIST -> filtered.sortedWith(compareBy({ artistOf(it).lowercase() }, { it.title.lowercase() }))
        SortMode.LONGEST -> filtered.sortedByDescending { it.durationMs }
    }
    val artists = remember(library) {
        library.groupBy(::artistOf).map { (name, tracks) -> name to tracks }.sortedBy { it.first.lowercase() }
    }

    Column(Modifier.fillMaxSize()) {
        // Pencarian di lagu offline
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari di ${library.size} lagu offline…", color = Neon.Dim) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Neon.Cyan) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, null, tint = Neon.Dim) }
            },
            singleLine = true,
            shape = HudShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Neon.Cyan, unfocusedBorderColor = Neon.Line,
                focusedContainerColor = Neon.Panel, unfocusedContainerColor = Neon.Panel,
                cursorColor = Neon.Cyan, focusedTextColor = Neon.Text, unfocusedTextColor = Neon.Text,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        // Grup: Semua · Artis · grup buatan sendiri · + Grup
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { HudChip("SEMUA ${library.size}", group == ALL, { group = ALL }) }
            item { HudChip("ARTIS ${artists.size}", group == ARTISTS || group.startsWith("artist:"), { group = ARTISTS }) }
            items(playlists, key = { it.id }) { p ->
                val count = p.trackIds.count { it in byId }
                HudChip("▣ ${p.name.uppercase()} $count", group == "pl:${p.id}", { group = "pl:${p.id}" })
            }
            item { HudChip("+ GRUP", false, { showNewGroup = true }) }
        }

        // Judul grup aktif + aksi
        if (currentPlaylist != null || group.startsWith("artist:")) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (currentPlaylist != null) Icons.Rounded.Folder else Icons.Rounded.Person, null, tint = Neon.Magenta, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    currentPlaylist?.name ?: group.removePrefix("artist:"),
                    color = Neon.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                )
                if (currentPlaylist != null) {
                    IconButton(onClick = { renaming = currentPlaylist }) { Icon(Icons.Rounded.Edit, "Ubah nama grup", tint = Neon.Dim) }
                    IconButton(onClick = { confirmDelete = currentPlaylist }) { Icon(Icons.Rounded.Delete, "Hapus grup", tint = Neon.Dim) }
                } else {
                    IconButton(onClick = { group = ARTISTS }) { Icon(Icons.Rounded.Close, "Kembali ke daftar artis", tint = Neon.Dim) }
                }
            }
        }

        if (group == ARTISTS) {
            ArtistList(artists) { group = "artist:$it" }
            return@Column
        }

        SectionBar(
            if (q.isEmpty()) "${shown.size} lagu" else "${shown.size} cocok dengan \"$q\"",
            enabled = shown.isNotEmpty(),
            onPlayAll = { vm.playList(shown, 0) },
            onShuffle = { vm.playList(shown.shuffled(), 0) },
            extra = {
                Box {
                    TextButton(onClick = { sortMenu = true }) {
                        Icon(Icons.Rounded.Sort, null, tint = Neon.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(sort.label, color = Neon.Cyan, fontSize = 12.sp)
                    }
                    DropdownMenu(sortMenu, onDismissRequest = { sortMenu = false }, containerColor = Neon.PanelHi) {
                        SortMode.entries.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.label, color = if (m == sort) Neon.Cyan else Neon.Text) },
                                onClick = { sort = m; sortMenu = false },
                            )
                        }
                    }
                }
            },
        )

        when {
            library.isEmpty() -> EmptyState(Icons.Rounded.MusicNote, "Belum ada audio offline", "Di tab Cari, ketuk ikon unduh → \"Unduh audio\".")
            currentPlaylist != null && base.isEmpty() -> EmptyState(
                Icons.Rounded.PlaylistAdd, "Grup masih kosong",
                "Buka \"Semua\", ketuk ⋮ di sebuah lagu → \"Masukkan ke grup\".",
            )
            shown.isEmpty() -> EmptyState(Icons.Rounded.Search, "Tidak ditemukan", "Tidak ada lagu offline yang cocok dengan \"$q\".")
        }

        TrackList(shown) { index, track ->
            TrackRow(track, ui.track?.id == track.id && ui.track?.wantVideo != true, onClick = { vm.playList(shown, index) }) {
                LibraryTrackMenu(
                    inPlaylist = currentPlaylist,
                    onAddToGroup = { addToGroup = track },
                    onRemoveFromGroup = { currentPlaylist?.let { vm.togglePlaylist(it.id, track) } },
                    onQueue = { vm.addToQueue(track) },
                    onDelete = { vm.deleteOffline(track, Kind.AUDIO) },
                )
            }
        }
    }

    // ---------- Dialog ----------
    addToGroup?.let { track ->
        AddToGroupDialog(
            track, playlists,
            onToggle = { vm.togglePlaylist(it.id, track) },
            onNew = { newGroupFor = track; addToGroup = null; showNewGroup = true },
            onDismiss = { addToGroup = null },
        )
    }
    if (showNewGroup) {
        NameDialog(
            title = "Grup baru",
            hint = "mis. Galau, Olahraga, Kerja, Santai",
            initial = "",
            onConfirm = { name -> vm.createPlaylist(name, newGroupFor); showNewGroup = false; newGroupFor = null },
            onDismiss = { showNewGroup = false; newGroupFor = null },
        )
    }
    renaming?.let { p ->
        NameDialog("Ubah nama grup", "", p.name, onConfirm = { vm.renamePlaylist(p.id, it); renaming = null }, onDismiss = { renaming = null })
    }
    confirmDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = Neon.Panel,
            title = { Text("Hapus grup \"${p.name}\"?", color = Neon.Text) },
            text = { Text("Lagu di dalamnya tidak ikut terhapus.", color = Neon.Dim) },
            confirmButton = { TextButton(onClick = { vm.deletePlaylist(p.id); group = ALL; confirmDelete = null }) { Text("Hapus", color = Neon.Magenta) } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Batal", color = Neon.Dim) } },
        )
    }
}

@Composable
private fun ArtistList(artists: List<Pair<String, List<Track>>>, onOpen: (String) -> Unit) {
    if (artists.isEmpty()) {
        EmptyState(Icons.Rounded.Person, "Belum ada artis", "Unduh beberapa lagu dulu dari tab Cari.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.fillMaxSize()) {
        items(artists, key = { it.first }) { (name, tracks) ->
            Row(
                Modifier.fillMaxWidth().clickable { onOpen(name) }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(tracks.firstOrNull()?.artworkUrl, 48.dp, corner = 10.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, color = Neon.Text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    HudLabel("${tracks.size} LAGU · ${formatTime(tracks.sumOf { it.durationMs })}")
                }
                Text("›", color = Neon.Cyan, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

@Composable
private fun LibraryTrackMenu(
    inPlaylist: Playlist?,
    onAddToGroup: () -> Unit,
    onRemoveFromGroup: () -> Unit,
    onQueue: () -> Unit,
    onDelete: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Rounded.MoreVert, "Menu lagu", tint = Neon.Dim) }
        DropdownMenu(open, onDismissRequest = { open = false }, containerColor = Neon.PanelHi) {
            DropdownMenuItem(
                text = { Text("Masukkan ke grup…", color = Neon.Text) },
                leadingIcon = { Icon(Icons.Rounded.Folder, null, tint = Neon.Cyan) },
                onClick = { open = false; onAddToGroup() },
            )
            if (inPlaylist != null) DropdownMenuItem(
                text = { Text("Keluarkan dari \"${inPlaylist.name}\"", color = Neon.Text) },
                leadingIcon = { Icon(Icons.Rounded.PlaylistRemove, null, tint = Neon.Cyan) },
                onClick = { open = false; onRemoveFromGroup() },
            )
            DropdownMenuItem(
                text = { Text("Tambah ke antrean", color = Neon.Text) },
                leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, null, tint = Neon.Cyan) },
                onClick = { open = false; onQueue() },
            )
            DropdownMenuItem(
                text = { Text("Hapus dari HP", color = Neon.Magenta) },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Neon.Magenta) },
                onClick = { open = false; onDelete() },
            )
        }
    }
}

@Composable
private fun AddToGroupDialog(
    track: Track,
    playlists: List<Playlist>,
    onToggle: (Playlist) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.Panel,
        title = { Text("Masukkan ke grup", color = Neon.Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(track.title, color = Neon.Dim, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    itemsIndexed(playlists, key = { _, p -> p.id }) { _, p ->
                        val inside = track.id in p.trackIds
                        Row(
                            Modifier.fillMaxWidth().clip(HudShape(8.dp)).clickable { onToggle(p) }.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(if (inside) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank, null, tint = if (inside) Neon.Cyan else Neon.Dim)
                            Spacer(Modifier.width(10.dp))
                            Text(p.name, color = Neon.Text, modifier = Modifier.weight(1f))
                            HudLabel("${p.trackIds.size}")
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clip(HudShape(8.dp)).border(1.dp, Neon.Line, HudShape(8.dp))
                        .background(Neon.PanelHi).clickable(onClick = onNew).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, null, tint = Neon.Cyan)
                    Spacer(Modifier.width(10.dp))
                    Text("Buat grup baru", color = Neon.Cyan, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai", color = Neon.Cyan) } },
    )
}

@Composable
private fun NameDialog(title: String, hint: String, initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.Panel,
        title = { Text(title, color = Neon.Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(40) }, singleLine = true,
                placeholder = { Text(hint, color = Neon.Dim) },
                shape = HudShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onConfirm(name) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Neon.Cyan, unfocusedBorderColor = Neon.Line,
                    cursorColor = Neon.Cyan, focusedTextColor = Neon.Text, unfocusedTextColor = Neon.Text,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Simpan", color = if (name.isNotBlank()) Neon.Cyan else Neon.Dim)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Neon.Dim) } },
    )
}
