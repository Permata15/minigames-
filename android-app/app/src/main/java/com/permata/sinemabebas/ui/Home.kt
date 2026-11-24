package com.permata.sinemabebas.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.permata.sinemabebas.data.ArchiveRepository
import com.permata.sinemabebas.data.Title
import kotlinx.coroutines.delay

/** Netflix-style home: rotating hero banner + horizontal rows per category. */
@Composable
fun HomeScreen(
    vm: CatalogViewModel,
    history: List<Title>,
    onOpen: (Title) -> Unit,
    onSeeAll: (String) -> Unit,
) {
    val cats = ArchiveRepository.categories
    val featured = vm.category(cats.first().key)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Hero(featured.items.take(6), onOpen) }
        if (history.isNotEmpty()) item {
            CategoryRow("Lanjutkan Menonton", history, loading = false, onOpen = onOpen, onSeeAll = null)
        }
        items(cats, key = { it.key }) { c ->
            val list = vm.category(c.key)
            CategoryRow(c.label, list.items, list.loading && list.items.isEmpty(), onOpen) { onSeeAll(c.key) }
        }
    }
}

@Composable
private fun Hero(picks: List<Title>, onOpen: (Title) -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(picks.size) {
        while (picks.size > 1) { delay(6_000); index = (index + 1) % picks.size }
    }
    Box(Modifier.fillMaxWidth().height(440.dp)) {
        if (picks.isEmpty()) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3A0A10), Bg))))
        } else {
            Crossfade(targetState = picks[index % picks.size], animationSpec = tween(900), label = "hero") { t ->
                Box(Modifier.fillMaxSize()) {
                    // Blurred, enlarged poster as the backdrop; sharp poster in front.
                    AsyncImage(
                        t.posterUrl, null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().scale(1.2f).blur(24.dp),
                    )
                    Box(Modifier.fillMaxSize().background(Color(0x55000000)))
                    AsyncImage(
                        t.posterUrl, t.name, contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 56.dp)
                            .width(190.dp)
                            .aspectRatio(2f / 3f)
                            .shadow(24.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color(0x99000000), 0.18f to Color.Transparent, 0.62f to Color.Transparent, 1f to Bg),
            ),
        )
        // Brand bar
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("SINEMA", color = Accent, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp)
            Text(" BEBAS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp)
        }
        val t = picks.getOrNull(index % picks.size.coerceAtLeast(1))
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (t != null) {
                Text(
                    t.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, color = Color.White,
                )
                if (t.year.isNotBlank()) Text("Film • ${t.year}", fontSize = 13.sp, color = Color.White.copy(alpha = .7f))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onOpen(t) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    ) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(4.dp))
                        Text("Putar", fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(
                        onClick = { onOpen(t) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0x55FFFFFF), contentColor = Color.White),
                    ) {
                        Icon(Icons.Default.Info, null); Spacer(Modifier.width(4.dp)); Text("Info")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    picks.indices.forEach { i ->
                        val w by animateFloatAsState(if (i == index % picks.size) 18f else 6f, label = "dot")
                        Box(
                            Modifier.height(6.dp).width(w.dp).clip(CircleShape)
                                .background(if (i == index % picks.size) Accent else Color.White.copy(alpha = .35f)),
                        )
                    }
                }
            } else {
                CircularProgressIndicator(color = Accent)
            }
        }
    }
}

@Composable
private fun CategoryRow(
    label: String,
    items: List<Title>,
    loading: Boolean,
    onOpen: (Title) -> Unit,
    onSeeAll: (() -> Unit)?,
) {
    Column(Modifier.padding(top = 18.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .then(if (onSeeAll != null) Modifier.clickable(onClick = onSeeAll) else Modifier)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            if (onSeeAll != null) {
                Text("Semua", fontSize = 13.sp, color = Color.White.copy(alpha = .6f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = .6f))
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (loading) items(6) { Shimmer() }
            else items(items.take(20), key = { label + it.id }) { t -> PressablePoster(t) { onOpen(t) } }
        }
    }
}

@Composable
private fun PressablePoster(t: Title, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")
    AsyncImage(
        t.posterUrl, t.name, contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(112.dp)
            .aspectRatio(2f / 3f)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
    )
}

@Composable
private fun Shimmer() {
    val alpha by androidx.compose.animation.core.rememberInfiniteTransition(label = "sh").animateFloat(
        0.25f, 0.6f,
        androidx.compose.animation.core.infiniteRepeatable(tween(800), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "a",
    )
    Box(
        Modifier.width(112.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = alpha * 0.25f)),
    )
}

/** Full grid for one category ("Semua"). */
@Composable
fun CategoryScreen(vm: CatalogViewModel, key: String, onBack: () -> Unit, onOpen: (Title) -> Unit) {
    BackHandler(onBack = onBack)
    val cat = ArchiveRepository.categories.first { it.key == key }
    val list = vm.category(key)
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
            Text(cat.label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        PosterGrid(list.items, list.loading, list.error, { vm.loadMore(list) }, { vm.retry(list) }, onOpen)
    }
}
