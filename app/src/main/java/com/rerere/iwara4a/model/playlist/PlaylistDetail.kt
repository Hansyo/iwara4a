package com.rerere.iwara4a.model.playlist

import com.rerere.iwara4a.model.index.MediaPreview

class PlaylistDetail(
    val title: String,
    val nid: Int,
    val videolist: List<MediaPreview>
)