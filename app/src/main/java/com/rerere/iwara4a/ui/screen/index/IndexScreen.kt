package com.rerere.iwara4a.ui.screen.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.FeaturedVideo
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.rounded.FeaturedVideo
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.rerere.iwara4a.R
import com.rerere.iwara4a.ui.public.IwaraTopBar
import com.rerere.iwara4a.ui.screen.index.page.ImageListPage
import com.rerere.iwara4a.ui.screen.index.page.RecommendPage
import com.rerere.iwara4a.ui.screen.index.page.SubPage
import com.rerere.iwara4a.ui.screen.index.page.VideoListPage
import com.rerere.iwara4a.ui.theme.uiBackGroundColor
import com.rerere.iwara4a.util.DataState
import com.rerere.iwara4a.util.getVersionName
import com.rerere.iwara4a.util.openUrl
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun IndexScreen(navController: NavController, indexViewModel: IndexViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // 更新提醒
    val updateDialog = rememberMaterialDialogState()
    val currentVersion = remember {
        context.getVersionName()
    }
    val update by indexViewModel.updateChecker.collectAsState()
    MaterialDialog(
        dialogState = updateDialog,
        buttons = {
            button("前往Github更新") {
                updateDialog.hide()
                context.openUrl("https://github.com/re-ovo/iwara4a/releases/latest")
            }
            button("忽略") {
                updateDialog.hide()
            }
        }
    ) {
        if (update is DataState.Success) {
            title("APP有更新: ${update.read().name}")
            message("更新内容:\n${update.read().body}")
        }
    }

    val pagerState = rememberPagerState(0)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            IwaraTopBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        val painter = rememberImagePainter(indexViewModel.self.profilePic)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .placeholder(
                                    visible = painter.state is ImagePainter.State.Loading,
                                    highlight = PlaceholderHighlight.shimmer()
                                )
                        ) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painter,
                                contentDescription = null
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(visible = update is DataState.Success && update.read().name != currentVersion) {
                        IconButton(onClick = {
                            updateDialog.show()
                        }) {
                            Icon(Icons.Default.Update, null)
                        }
                    }

                    IconButton(onClick = {
                        navController.navigate("message")
                    }) {
                        BadgedBox(
                            badge = {
                                androidx.compose.animation.AnimatedVisibility(visible = indexViewModel.self.messages > 0) {
                                    Badge {
                                        Text(text = indexViewModel.self.messages.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Message, null)
                        }
                    }

                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, null)
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                currentPage = pagerState.currentPage,
                scrollToPage = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(it)
                    }
                }
            )
        },
        drawerContent = {
            IndexDrawer(navController, indexViewModel, scaffoldState)
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            count = 4
        ) { page ->
            when (page) {
                0 -> {
                    SubPage(navController, indexViewModel)
                }
                1 -> {
                    RecommendPage(indexViewModel)
                }
                2 -> {
                    VideoListPage(navController, indexViewModel)
                }
                3 -> {
                    ImageListPage(navController, indexViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalPagerApi
@Composable
private fun BottomBar(currentPage: Int, scrollToPage: (Int) -> Unit) {
    BottomNavigation(
        modifier = Modifier.navigationBarsWithImePadding(),
        backgroundColor = MaterialTheme.colors.uiBackGroundColor
    ) {
        BottomNavigationItem(
            selected = currentPage == 0,
            onClick = {
                scrollToPage(0)
            },
            icon = {
                Icon(imageVector = Icons.Outlined.Subscriptions, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.screen_index_bottom_sub))

            },
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )

        BottomNavigationItem(
            selected = currentPage == 1,
            onClick = {
                scrollToPage(1)
            },
            icon = {
                Icon(imageVector = Icons.Outlined.Sort, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.screen_index_bottom_sort))
            },
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )

        BottomNavigationItem(
            selected = currentPage == 2,
            onClick = {
                scrollToPage(2)
            },
            icon = {
                Icon(imageVector = Icons.Outlined.FeaturedVideo, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.screen_index_bottom_video))
            },
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )

        BottomNavigationItem(
            selected = currentPage == 3,
            onClick = {
                scrollToPage(3)
            },
            icon = {
                Icon(imageVector = Icons.Outlined.Image, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.screen_index_bottom_image))
            },
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )
    }
}