package com.assisment.newschatprofileapp.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.utils.Resource
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Get colors from MaterialTheme which uses YOUR custom theme
    val primaryColor = MaterialTheme.colorScheme.primary  // Red in your theme
    val backgroundColor = MaterialTheme.colorScheme.background  // White in light, Black in dark
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground  // Black in light, White in dark
    val surfaceColor = MaterialTheme.colorScheme.surface  // White in light, Dark surface in dark
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface  // Black in light, White in dark
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant  // F5F5F5 in light, 2D2D2D in dark
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant  // 757575 in light, B0B0B0 in dark
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary  // White in both themes
    val outlineColor = MaterialTheme.colorScheme.outline  // E0E0E0 in light, 424242 in dark
    val secondaryColor = MaterialTheme.colorScheme.secondary  // 424242 in light, B0B0B0 in dark

    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            coroutineScope.launch {
                scrollState.animateScrollTo(0)
            }
        }
    }

    // Handle pagination
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { scrollPosition ->
                val maxScroll = scrollState.maxValue
                // Load more when scrolled to 80% of the list
                if (scrollPosition >= maxScroll - 200 &&
                    !state.isLoadingMore &&
                    state.canLoadMore &&
                    state.isOnline &&
                    searchQuery.isEmpty()) {
                    coroutineScope.launch {
                        viewModel.onEvent(HomeEvent.LoadMore)
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Offline Indicator
        AnimatedVisibility(
            visible = !state.isOnline,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = "Offline",
                        tint = onPrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You're offline. Showing cached articles.",
                        color = onPrimaryColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "News",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.onEvent(HomeEvent.Search(it))
            },
            label = {
                Text(
                    "Search news...",
                    color = onSurfaceVariantColor
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = onSurfaceVariantColor
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.onEvent(HomeEvent.ClearSearch)
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = onSurfaceVariantColor
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                focusedBorderColor = primaryColor,
                cursorColor = primaryColor,
                focusedLabelColor = primaryColor,
                containerColor = surfaceColor,
                unfocusedLabelColor = onSurfaceVariantColor,
                focusedTextColor = onBackgroundColor,
                unfocusedTextColor = onBackgroundColor,
                focusedLeadingIconColor = primaryColor,
                unfocusedLeadingIconColor = onSurfaceVariantColor,
                focusedTrailingIconColor = primaryColor,
                unfocusedTrailingIconColor = onSurfaceVariantColor
            ),
            shape = RoundedCornerShape(24.dp)
        )

        // Show Featured Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.showFeaturedSection) "Hide Featured" else "Show Featured",
                style = MaterialTheme.typography.bodyMedium.copy(color = onBackgroundColor)
            )

            Switch(
                checked = state.showFeaturedSection,
                onCheckedChange = {
                    viewModel.onEvent(HomeEvent.ToggleFeatured(it))
                },
                thumbContent = if (state.showFeaturedSection) {
                    {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Visible",
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = onPrimaryColor
                        )
                    }
                } else {
                    null
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                    checkedBorderColor = primaryColor,
                    uncheckedThumbColor = surfaceVariantColor,
                    uncheckedTrackColor = outlineColor,
                    uncheckedBorderColor = outlineColor
                )
            )
        }

        // SwipeRefresh
        val refreshState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing && state.isOnline)
        SwipeRefresh(
            state = refreshState,
            onRefresh = { viewModel.onEvent(HomeEvent.Refresh) },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = 80.dp,
                    backgroundColor = primaryColor,
                    contentColor = onPrimaryColor
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Determine which articles to show
            val articlesToShow = when {
                searchQuery.isNotEmpty() -> state.searchResults
                !state.isOnline -> state.cachedArticles
                else -> state.articles.data ?: emptyList()
            }

            if (articlesToShow.isEmpty() && state.isLoading && searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 8.dp)
                ) {
                    // Featured Section - only show when not searching and online
                    if (state.showFeaturedSection &&
                        state.featuredArticles.isNotEmpty() &&
                        searchQuery.isEmpty() &&
                        state.isOnline) {
                        FeaturedSection(articles = state.featuredArticles)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Articles List - Like RecyclerView items
                    articlesToShow.forEach { article ->
                        NewsCard(article = article)
                    }

                    // Loading more indicator - ONLY when NOT searching and ONLINE
                    if (state.isLoadingMore && searchQuery.isEmpty() && state.isOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = primaryColor
                            )
                        }
                    }

                    // End of list - ONLY when NOT searching
                    if (!state.canLoadMore &&
                        articlesToShow.isNotEmpty() &&
                        searchQuery.isEmpty() &&
                        !state.isLoadingMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "You've reached the end",
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryColor
                            )
                        }
                    }

                    // Empty state
                    if (articlesToShow.isEmpty() && !state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Newspaper,
                                    contentDescription = "No articles",
                                    tint = onSurfaceVariantColor,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "No articles found for \"$searchQuery\""
                                    } else if (!state.isOnline) {
                                        "No cached articles found"
                                    } else {
                                        "No articles available"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceVariantColor,
                                    textAlign = TextAlign.Center
                                )
                                if (!state.isOnline) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.onEvent(HomeEvent.Refresh) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryColor,
                                            contentColor = onPrimaryColor
                                        )
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedSection(articles: List<Article>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Featured News",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Use Row with HorizontalScroll
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            articles.forEach { article ->
                FeaturedCard(article = article)
            }
        }
    }
}

@Composable
fun FeaturedCard(article: Article) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!article.urlToImage.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(article.urlToImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "No Image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ),
                            startY = 100f,
                            endY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = article.sourceName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatPublishedDate(article.publishedAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsCard(
    article: Article,
    onArticleClick: (Article) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
            contentColor = onSurfaceColor
        ),
        onClick = { onArticleClick(article) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Featured badge
            if (article.isFeatured) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Badge(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "FEATURED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Article Image with fixed size
            if (!article.urlToImage.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(article.urlToImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (article.isFeatured) 0.dp else 16.dp,
                                bottomEnd = if (article.isFeatured) 0.dp else 16.dp
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(primaryColor.copy(alpha = 0.1f))
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (article.isFeatured) 0.dp else 16.dp,
                                bottomEnd = if (article.isFeatured) 0.dp else 16.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No Image",
                            tint = primaryColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Image Available",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryColor
                        )
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = onSurfaceColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                article.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Source with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = "Source",
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.sourceName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Date with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Published at",
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatPublishedDate(article.publishedAt),
                            style = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariantColor),
                            maxLines = 1
                        )
                    }
                }

                // Author and ID row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    // Author with icon
                    article.author?.let { author ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Author",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "By ${author.take(30)}${if (author.length > 30) "..." else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(color = primaryColor),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } ?: Spacer(modifier = Modifier.weight(1f))

                    // Article ID

                }

                // URL indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.05f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "URL",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = article.url
                            .replace("https://", "")
                            .replace("http://", "")
                            .take(40) + if (article.url.length > 40) "..." else "",
                        style = MaterialTheme.typography.labelSmall.copy(color = primaryColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// Helper function to format the published date
private fun formatPublishedDate(publishedAt: String): String {
    return try {
        if (publishedAt.length > 10) {
            publishedAt.substring(0, 10)
        } else {
            publishedAt
        }
    } catch (e: Exception) {
        publishedAt
    }
}