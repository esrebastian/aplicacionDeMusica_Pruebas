package com.example.proyectopruebaappmusia1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingFullScreen(
    viewModel: MusicPlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    var sliderPosition by remember(currentSong?.id) { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreenBg, CardGreenBg.copy(alpha = 0.3f), DarkGreenBg)
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { /* Empty title */ },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.close_player),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                AlbumArtImage(
                    albumArtId = currentSong?.albumArt,
                    contentDescription = stringResource(R.string.album_art),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = currentSong?.title ?: stringResource(R.string.select_song),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSong?.artist ?: stringResource(R.string.unknown_artist),
                            color = SecondaryText,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                    val isFavorite = currentSong?.id in favoriteIds
                    IconButton(onClick = { viewModel.toggleFavorite(currentSong) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.Favorite,
                            contentDescription = stringResource(R.string.favorite_content_description),
                            tint = if (isFavorite) AccentGreen else SecondaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderPosition.coerceIn(0f, 1f),
                        onValueChange = {
                            isUserSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo(sliderPosition.coerceIn(0f, 1f))
                            isUserSeeking = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGreen,
                            activeTrackColor = AccentGreen,
                            inactiveTrackColor = Color(0xFF2D3D32)
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentPosition), color = SecondaryText, fontSize = 12.sp)
                        Text(formatTime(duration), color = SecondaryText, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousSong() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = stringResource(R.string.previous), tint = Color.White, modifier = Modifier.size(48.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.play_pause),
                            tint = DarkGreenBg,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.nextSong() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = stringResource(R.string.next), tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
