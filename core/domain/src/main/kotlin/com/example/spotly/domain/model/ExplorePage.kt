package com.example.spotly.domain.model

import java.time.Instant

data class ExploreCursor(val createdAt: Instant, val id: String)
data class ExplorePage(val posts: List<Post>, val next: ExploreCursor?)
