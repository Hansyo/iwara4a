package com.rerere.iwara4a.ui.screen.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.rerere.iwara4a.R
import com.rerere.iwara4a.model.detail.image.ImageDetail
import com.rerere.iwara4a.ui.public.IwaraTopBar
import com.rerere.iwara4a.ui.theme.uiBackGroundColor
import com.rerere.iwara4a.util.DataState
import com.rerere.iwara4a.util.downloadImageNew
import com.rerere.iwara4a.util.noRippleClickable
import kotlinx.coroutines.launch
import me.rerere.zoomableimage.ZoomableImage

@ExperimentalPagerApi
@Composable
fun ImageScreen(
    navController: NavController,
    imageId: String,
    imageViewModel: ImageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        imageViewModel.load(imageId)
    }
    val imageDetail by imageViewModel.imageDetail.collectAsState()
    Scaffold(topBar = {
        IwaraTopBar(
            title = {
                Text(text = if (imageDetail is DataState.Success) imageDetail.read().title else "浏览图片")
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            actions = {
                if (imageDetail is DataState.Success) {
                    IconButton(onClick = {
                        imageDetail.readSafely()?.imageLinks?.forEachIndexed { i,link ->
                            context.downloadImageNew(
                                downloadUrlOfImage = link,
                                filename = "${imageId}_$i"
                            )
                        }
                    }) {
                        Icon(Icons.Default.Download, null)
                    }
                }
            },
            elevation = 0.dp
        )
    }) {
        when (imageDetail) {
            DataState.Empty,
            DataState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.paperplane))
                        LottieAnimation(
                            modifier = Modifier.size(170.dp),
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        Text(text = "加载中", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is DataState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error_state_dog))
                        LottieAnimation(
                            modifier = Modifier.size(150.dp),
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        Text(text = "加载失败，点击重试", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is DataState.Success -> {
                ImagePage(navController, imageDetail.read())
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun ImagePage(navController: NavController, imageDetail: ImageDetail) {
    val pagerState = rememberPagerState(
        0
    )
    val coroutineScope = rememberCoroutineScope()
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        if (imageDetail.imageLinks.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.uiBackGroundColor
            ) {
                repeat(imageDetail.imageLinks.size) { page ->
                    Tab(
                        selected = pagerState.currentPage == page,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(page)
                            }
                        }) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Text(text = "图片 ${page + 1}")
                        }
                    }
                }
            }
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            state = pagerState,
            count = imageDetail.imageLinks.size
        ) { page ->
            val link = imageDetail.imageLinks[page]
            ZoomableImage(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                painter = rememberImagePainter(data = link)
            )
        }
        if (imageDetail.imageLinks.size > 1) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                    color = Color.White
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .noRippleClickable {
                        navController.navigate("user/${imageDetail.authorId}")
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberImagePainter(imageDetail.authorProfilePic),
                        contentDescription = null
                    )
                }

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = imageDetail.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }
        }
    }
}