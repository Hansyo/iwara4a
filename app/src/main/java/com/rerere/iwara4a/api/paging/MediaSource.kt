package com.rerere.iwara4a.api.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rerere.iwara4a.model.index.MediaPreview
import com.rerere.iwara4a.model.index.MediaQueryParam
import com.rerere.iwara4a.model.index.MediaType
import com.rerere.iwara4a.model.session.SessionManager
import com.rerere.iwara4a.repo.MediaRepo

private const val TAG = "MediaSource"

class MediaSource(
    private val mediaType: MediaType,
    private val mediaRepo: MediaRepo,
    private val sessionManager: SessionManager,
    private val mediaQueryParam: MediaQueryParam
) : PagingSource<Int, MediaPreview>() {
    override fun getRefreshKey(state: PagingState<Int, MediaPreview>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaPreview> {
        val page = params.key ?: 0

        Log.i(TAG, "load: Trying to load media list: $page")

        val response = mediaRepo.getMediaList(
            sessionManager.session,
            mediaType,
            page,
            mediaQueryParam.sortType,
            mediaQueryParam.filters
        )
        return if (response.isSuccess()) {
            val data = response.read()
            Log.i(
                TAG,
                "load: Success load media list (datasize=${data.mediaList.size}, hasNext=${data.hasNext})"
            )
            LoadResult.Page(
                data = data.mediaList,
                prevKey = if (page <= 0) null else page - 1,
                nextKey = if (data.hasNext) page + 1 else null
            )
        } else {
            LoadResult.Error(Exception(response.errorMessage()))
        }
    }
}