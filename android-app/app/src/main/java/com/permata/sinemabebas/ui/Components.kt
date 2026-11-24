package com.permata.sinemabebas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.permata.sinemabebas.data.Title

@Composable
fun PosterCard(t: Title, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = t.posterUrl,
            contentDescription = t.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface),
        )
        Text(
            t.name, maxLines = 2, overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (t.year.isNotBlank()) Text(t.year, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
    }
}

/** Grid of posters that asks for the next page when the user nears the end (infinite scroll). */
@Composable
fun PosterGrid(
    items: List<Title>,
    loading: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (Title) -> Unit,
    showHeader: Boolean = false,
    header: @Composable () -> Unit = {},
) {
    val state = rememberLazyGridState()
    val nearEnd by remember {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(nearEnd, items.size) { if (nearEnd && !loading && error == null && items.isNotEmpty()) onLoadMore() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        state = state,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (showHeader) item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { header() }
        items(items, key = { it.id }) { PosterCard(it, onClick = { onOpen(it) }) }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                when {
                    loading -> CircularProgressIndicator(Modifier.size(28.dp))
                    error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gagal memuat: $error", fontSize = 13.sp)
                        TextButton(onClick = onRetry) { Text("Coba lagi") }
                    }
                }
            }
        }
    }
}
