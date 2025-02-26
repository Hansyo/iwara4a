package com.rerere.iwara4a.ui.screen.index.page

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pages
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rerere.iwara4a.R
import com.rerere.iwara4a.model.index.MediaPreview
import com.rerere.iwara4a.ui.public.ListSnapToTop
import com.rerere.iwara4a.ui.public.MediaPreviewCard
import com.rerere.iwara4a.ui.public.appendIndicator
import com.rerere.iwara4a.ui.public.items
import com.rerere.iwara4a.ui.screen.index.IndexViewModel
import com.rerere.iwara4a.util.noRippleClickable
import com.vanpra.composematerialdialogs.*
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun SubPage(navController: NavController, indexViewModel: IndexViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val subscriptionList = indexViewModel.subscriptionPager.collectAsLazyPagingItems()
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = subscriptionList.loadState.refresh == LoadState.Loading
    )
    val context = LocalContext.current
    var page by remember {
        mutableStateOf("1")
    }
    val pageDialog = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = pageDialog,
        buttons = {
            positiveButton("跳转") {
                page.toIntOrNull()?.let {
                    indexViewModel.subPage.value = (it - 1).coerceAtLeast(0)
                    subscriptionList.refresh()
                } ?: kotlin.run {
                    Toast.makeText(context, "请输入一个大于0的数字！", Toast.LENGTH_SHORT).show()
                }
            }

            negativeButton("取消")
        }
    ) {
        title("跳转到某页")
        customView {
            OutlinedTextField(
                value = page,
                onValueChange = {
                    page = it
                },
                isError = page.toIntOrNull() == null,
                trailingIcon = {
                    IconButton(onClick = { page = "1" }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        }
    }
    when {
        subscriptionList.loadState.refresh is LoadState.Error -> {
            SubPageError(subscriptionList)
        }
        subscriptionList.loadState.refresh == LoadState.Loading && subscriptionList.itemCount == 0 -> {
            SubPageLoading()
        }
        else -> {
            val listState = rememberLazyListState()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        pageDialog.show()
                    }) {
                        Icon(Icons.Default.Pages, null)
                    }
                }
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { subscriptionList.refresh() },
                    indicator = { s, trigger ->
                        SwipeRefreshIndicator(
                            s,
                            trigger,
                            contentColor = MaterialTheme.colors.primary
                        )
                    }
                ) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        cells = GridCells.Fixed(2),
                        state = listState
                    ) {
                        items(subscriptionList) { data ->
                            MediaPreviewCard(navController, data!!)
                        }

                        this.appendIndicator(subscriptionList)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubPageError(subscriptionList: LazyPagingItems<MediaPreview>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable {
                subscriptionList.retry()
            },
        contentAlignment = Alignment.Center
    ) {
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

@Composable
private fun SubPageLoading() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.hard_disk
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(200.dp),
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}