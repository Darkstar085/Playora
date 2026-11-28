package com.sipun.playora.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onFinish: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AmbientGlowBackground()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            val isShortHeight = maxHeight < 520.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopHeaderRow(
                    showSkip = pagerState.currentPage < 2,
                    compact = isShortHeight,
                    onSkip = onFinish,
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    when (page) {
                        0 -> WelcomePage()
                        1 -> PowerfulPlaybackPage()
                        2 -> YourVideosYourWayPage()
                    }
                }

                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = if (isShortHeight) 10.dp else 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PageIndicatorDots(
                        pageCount = 3,
                        currentPage = pagerState.currentPage,
                    )

                    Spacer(Modifier.height(if (isShortHeight) 10.dp else 20.dp))

                    val buttonText = when (pagerState.currentPage) {
                        0 -> stringResource(R.string.onboarding_get_started)
                        1 -> stringResource(R.string.onboarding_next)
                        else -> stringResource(R.string.onboarding_start_watching)
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isShortHeight) 44.dp else 54.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                spotColor = primaryColor.copy(alpha = 0.35f),
                            ),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = onPrimaryColor,
                        ),
                        contentPadding = PaddingValues(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            primaryColor,
                                            primaryColor.copy(alpha = 0.85f),
                                        ),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = buttonText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                    ),
                                    color = onPrimaryColor,
                                )
                                Icon(
                                    imageVector = AppIcons.ArrowForward,
                                    contentDescription = null,
                                    tint = onPrimaryColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeaderRow(
    showSkip: Boolean,
    compact: Boolean,
    onSkip: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (compact) 6.dp else 12.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 4.dp,
            ),
    ) {
        if (compact) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.75f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Play,
                        contentDescription = null,
                        tint = onPrimaryColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.75f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Play,
                        contentDescription = null,
                        tint = onPrimaryColor,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = stringResource(R.string.onboarding_video_player).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 2.5.sp,
                    ),
                    color = primaryColor,
                )
            }
        }

        if (showSkip) {
            Surface(
                onClick = onSkip,
                modifier = Modifier.align(if (compact) Alignment.CenterEnd else Alignment.TopEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Welcome to ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Playora")
                    }
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FeatureCard(
                    icon = AppIcons.Play,
                    title = stringResource(R.string.onboarding_smooth_playback),
                    subtitle = stringResource(R.string.onboarding_smooth_playback_desc),
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    icon = AppIcons.Lock,
                    title = stringResource(R.string.onboarding_private_secure),
                    subtitle = stringResource(R.string.onboarding_private_secure_desc),
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    icon = AppIcons.Sensitivity,
                    title = stringResource(R.string.onboarding_customizable),
                    subtitle = stringResource(R.string.onboarding_customizable_desc),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 8.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(primaryColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 9.5.sp,
                lineHeight = 13.5.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
    }
}

@Composable
private fun PowerfulPlaybackPage() {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Powerful ")
                    withStyle(SpanStyle(color = primaryColor)) {
                        append("Playback")
                    }
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_playback_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(185.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = -4.5f
                            scaleX = 0.98f
                            scaleY = 0.96f
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                ),
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.7f),
                                    primaryColor.copy(alpha = 0.25f),
                                ),
                            ),
                            shape = RoundedCornerShape(20.dp),
                        ),
                ) {
                    LandscapeThumbnail(
                        modifier = Modifier.fillMaxSize(),
                        accentColor = primaryColor,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                AppIcons.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(
                                    AppIcons.Rotation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Icon(
                                    AppIcons.Caption,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Icon(
                                    AppIcons.MoreVert,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                AppIcons.Play,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "12:18",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .padding(horizontal = 8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                    )
                                }
                                Text(
                                    "24:56",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    AppIcons.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Icon(
                                    AppIcons.Replay,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Rounded.Pause,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Icon(
                                    AppIcons.FastForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Icon(
                                    AppIcons.Focus,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            val formats = listOf(
                FormatInfo("MP4", AppIcons.Movie),
                FormatInfo("MKV", AppIcons.Movie),
                FormatInfo("WEBM", AppIcons.Video),
                FormatInfo("AVI", AppIcons.Play),
                FormatInfo("MOV", AppIcons.Search),
                FormatInfo(stringResource(R.string.onboarding_and_more), AppIcons.More),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                formats.forEach { format ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp),
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = format.icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(17.dp),
                        )
                        Text(
                            text = format.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YourVideosYourWayPage() {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Your Videos, ")
                    withStyle(SpanStyle(color = primaryColor)) {
                        append("Your Way")
                    }
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_privacy_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(190.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 210.dp, height = 125.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.6f),
                                    primaryColor.copy(alpha = 0.2f),
                                ),
                            ),
                            shape = RoundedCornerShape(18.dp),
                        ),
                ) {
                    LandscapeThumbnail(
                        modifier = Modifier.fillMaxSize(),
                        accentColor = primaryColor,
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.Play,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color.White.copy(alpha = 0.25f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(3.dp)
                                .background(primaryColor),
                        )
                    }
                }

                ShieldBadge(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 38.dp, y = 14.dp),
                    accentColor = primaryColor,
                )

                FloatingPill(
                    icon = AppIcons.Lock,
                    title = "PIN Lock",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 4.dp, y = 4.dp),
                )
                FloatingPill(
                    icon = AppIcons.Sensitivity,
                    title = "Customize",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp),
                )
                FloatingPill(
                    icon = AppIcons.Appearance,
                    title = "Theming",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 4.dp, y = (-4).dp),
                )
                FloatingPill(
                    icon = AppIcons.Settings,
                    title = "Controls",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ChecklistItem(text = stringResource(R.string.onboarding_check_pin))
                ChecklistItem(text = stringResource(R.string.onboarding_check_theming))
                ChecklistItem(text = stringResource(R.string.onboarding_check_controls))
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.Lock,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.onboarding_vault_tip_title),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            ),
                            color = primaryColor,
                        )
                        Text(
                            text = stringResource(R.string.onboarding_vault_tip_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShieldBadge(
    modifier: Modifier = Modifier,
    accentColor: Color,
) {
    Box(
        modifier = modifier.size(width = 68.dp, height = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 0f)
                cubicTo(w * 0.85f, 0f, w, h * 0.12f, w, h * 0.38f)
                cubicTo(w, h * 0.72f, w * 0.55f, h * 0.96f, w * 0.5f, h)
                cubicTo(w * 0.45f, h * 0.96f, 0f, h * 0.72f, 0f, h * 0.38f)
                cubicTo(0f, h * 0.12f, w * 0.15f, 0f, w * 0.5f, 0f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.78f),
                    ),
                ),
            )
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.6f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Icon(
            imageVector = AppIcons.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun LandscapeThumbnail(
    modifier: Modifier = Modifier,
    accentColor: Color,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E1B4B),
                    Color(0xFF312E81),
                    accentColor.copy(alpha = 0.35f),
                ),
            ),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFDE047).copy(alpha = 0.3f),
                    accentColor.copy(alpha = 0.15f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.45f, h * 0.45f),
                radius = w * 0.28f,
            ),
            radius = w * 0.28f,
            center = Offset(w * 0.45f, h * 0.45f),
        )

        val distantRidge = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.2f, h * 0.44f)
            lineTo(w * 0.42f, h * 0.62f)
            lineTo(w * 0.68f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.56f)
            lineTo(w, h * 0.48f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = distantRidge,
            color = Color(0xFF1E1B4B).copy(alpha = 0.9f),
        )

        val frontMountain = Path().apply {
            moveTo(0f, h * 0.84f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.58f, h * 0.76f)
            lineTo(w * 0.8f, h * 0.46f)
            lineTo(w, h * 0.72f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = frontMountain,
            color = Color(0xFF0B0F19),
        )

        val snowCap = Path().apply {
            moveTo(w * 0.8f, h * 0.46f)
            lineTo(w * 0.75f, h * 0.56f)
            lineTo(w * 0.85f, h * 0.56f)
            close()
        }
        drawPath(
            path = snowCap,
            color = Color.White.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun FloatingPill(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ChecklistItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun PageIndicatorDots(
    pageCount: Int,
    currentPage: Int,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 6.dp,
                label = "indicator_dot_width",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) primaryColor else inactiveColor),
            )
        }
    }
}

@Composable
private fun AmbientGlowBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.10f),
                radius = width * 0.75f,
            ),
            radius = width * 0.75f,
            center = Offset(width * 0.85f, height * 0.10f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(width * 0.15f, height * 0.90f),
                radius = width * 0.80f,
            ),
            radius = width * 0.80f,
            center = Offset(width * 0.15f, height * 0.90f),
        )
    }
}

private data class FormatInfo(
    val name: String,
    val icon: ImageVector,
)