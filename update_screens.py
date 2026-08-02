import re

def update_detail_screen(file_path, is_tv=False):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add imports
    imports = """
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.yourname.icepacklist.feature.home.domain.WatchProvider
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.Review
"""
    content = content.replace('import com.yourname.icepacklist.feature.home.domain.VideoResult\n', 'import com.yourname.icepacklist.feature.home.domain.VideoResult\n' + imports.strip() + '\n')

    # Update Success state destructuring
    if is_tv:
        success_find = """
                TvDetailContent(
                    tvShow = state.tvShow,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,"""
        success_repl = """
                TvDetailContent(
                    tvShow = state.tvShow,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,
                    watchProviders = state.watchProviders,
                    keywords = state.keywords,
                    reviews = state.reviews,"""
        content = content.replace(success_find, success_repl)
        
        content_sig_find = """
private fun TvDetailContent(
    tvShow: TvShowDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<TvShow>,"""
        content_sig_repl = """
@OptIn(ExperimentalLayoutApi::class)
private fun TvDetailContent(
    tvShow: TvShowDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<TvShow>,
    watchProviders: List<WatchProvider>,
    keywords: List<Keyword>,
    reviews: List<Review>,"""
        content = content.replace(content_sig_find, content_sig_repl)
        
    else:
        success_find = """
                DetailContent(
                    movie = state.movie,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,"""
        success_repl = """
                DetailContent(
                    movie = state.movie,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,
                    watchProviders = state.watchProviders,
                    keywords = state.keywords,
                    reviews = state.reviews,"""
        content = content.replace(success_find, success_repl)
        
        content_sig_find = """
private fun DetailContent(
    movie: MovieDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<Movie>,"""
        content_sig_repl = """
@OptIn(ExperimentalLayoutApi::class)
private fun DetailContent(
    movie: MovieDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<Movie>,
    watchProviders: List<WatchProvider>,
    keywords: List<Keyword>,
    reviews: List<Review>,"""
        content = content.replace(content_sig_find, content_sig_repl)


    # Inject Available On before Cast
    cast_find = """
                // Cast Section
                if (credits.cast.isNotEmpty()) {"""
                
    available_on_code = """
                // Available On Section
                if (watchProviders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Available On",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(watchProviders) { provider ->
                            Column(
                                modifier = Modifier.width(64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = provider.logoPath?.let { "https://image.tmdb.org/t/p/w92$it" },
                                    contentDescription = provider.providerName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = provider.providerName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
"""
    content = content.replace(cast_find, available_on_code + cast_find)

    # Inject Crew after Cast
    # For TV, Cast section ends right before Details Section.
    # Wait, in DetailScreen:
    #                 }
    #             }
    #
    #             // Director Section
    crew_find = """
                // Details Section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.details),"""
                    
    # The prompt says: "Filter job values: Director, Writer, Screenplay, Creator, Executive Producer. Same layout as Cast. Max 6 items."
    crew_code = """
                // Crew Section
                val crewJobs = listOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")
                val crewList = credits.crew.filter { it.job in crewJobs }.take(6)
                if (crewList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Crew",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(crewList, key = { it.id.toString() + it.job }) { person ->
                            CrewItemCard(
                                person = person,
                                onClick = { onPersonClick(person.id) }
                            )
                        }
                    }
                }
"""
    content = content.replace(crew_find, crew_code + "\n" + crew_find)

    # Inject Keywords after Details
    # Details ends right before Trailer Section
    keywords_find = """
                // Trailer Section
                if (videos.isNotEmpty()) {"""
                
    keywords_code = """
                // Keywords Section
                if (keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Keywords",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keywords.take(8).forEach { keyword ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = keyword.name,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
"""
    content = content.replace(keywords_find, keywords_code + "\n" + keywords_find)

    # Inject Reviews at the very end of item column, after Recommendations Section
    reviews_find = """
            }
        }
    }
    
    if (showMyListSheet && entryState != null) {"""
    
    reviews_code = """
                // Reviews Section
                if (reviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Reviews",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        reviews.take(3).forEach { review ->
                            ReviewItem(review = review)
                        }
                    }
                }
"""
    # For TV screen, it is missing the extra space
    reviews_find2 = """
            }
        }
    }

    if (showMyListSheet && entryState != null) {"""

    if reviews_find in content:
        content = content.replace(reviews_find, reviews_code + reviews_find)
    elif reviews_find2 in content:
        content = content.replace(reviews_find2, reviews_code + reviews_find2)

    # Append CrewItemCard and ReviewItem components at the bottom
    extra_components = """
@Composable
private fun CrewItemCard(person: com.yourname.icepacklist.feature.home.domain.Crew, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = person.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            error = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = person.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = person.job ?: "",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewItem(review: com.yourname.icepacklist.feature.home.domain.Review) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val avatarPath = review.authorDetails?.avatarPath
            if (avatarPath != null) {
                val imageUrl = if (avatarPath.startsWith("/http")) avatarPath.substring(1) else "https://image.tmdb.org/t/p/w185$avatarPath"
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.author.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = review.author,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                review.authorDetails?.rating?.let { rating ->
                    Text(
                        text = "★ $rating",
                        color = Color(0xFFFFC107),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.content,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )
        if (!expanded && review.content.length > 150) {
            Text(
                text = "Read More",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { expanded = true }
            )
        }
    }
}
"""
    content = content + "\n" + extra_components

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

update_detail_screen(r"c:\Users\tapman\Desktop\icepack list\app\src\main\java\com\yourname\icepacklist\feature\detail\ui\DetailScreen.kt", is_tv=False)
update_detail_screen(r"c:\Users\tapman\Desktop\icepack list\app\src\main\java\com\yourname\icepacklist\feature\detail\ui\TvDetailScreen.kt", is_tv=True)
print("Done")
