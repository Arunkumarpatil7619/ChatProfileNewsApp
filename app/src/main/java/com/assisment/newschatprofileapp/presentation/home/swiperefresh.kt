package com.assisment.newschatprofileapp.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val refreshHeight = 80.dp
    val refreshHeightPx = with(LocalDensity.current) { refreshHeight.toPx() }
    val pullThreshold = refreshHeightPx * 0.8f

    val pullProgress = remember { Animatable(0f) }
    val isRefreshingState = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRefreshing) {
        isRefreshingState.value = isRefreshing
        if (!isRefreshing) {
            pullProgress.animateTo(0f, tween(300))
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && source == NestedScrollSource.Drag && !isRefreshingState.value) {
                    val newProgress = (pullProgress.value - available.y / refreshHeightPx).coerceAtLeast(0f)


                    coroutineScope.launch {
                        pullProgress.snapTo(newProgress)
                    }

                    if (newProgress >= 1f && !isRefreshingState.value) {
                        isRefreshingState.value = true
                        onRefresh()
                    }

                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0 && !isRefreshingState.value) {
                    coroutineScope.launch {
                        pullProgress.snapTo(0f)
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        content()

        if (pullProgress.value > 0 || isRefreshingState.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(refreshHeight)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshingState.value) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(if (pullProgress.value > 0 || isRefreshingState.value) refreshHeight else 0.dp)
                .fillMaxWidth()
        )
    }
}