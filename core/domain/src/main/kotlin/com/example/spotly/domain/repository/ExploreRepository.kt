package com.example.spotly.domain.repository

import com.example.spotly.domain.model.*

interface ExploreRepository {
    suspend fun load(excludedAuthors: Set<String>, cursor: ExploreCursor?): AppResult<ExplorePage>
}
