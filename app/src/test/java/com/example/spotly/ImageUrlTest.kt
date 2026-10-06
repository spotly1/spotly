package com.example.spotly

import com.example.spotly.ui.components.optimizedPostImageUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageUrlTest {
    @Test fun preservesLocalUrisAndTransformsOnlyOurCloud() {
        assertEquals("content://photos/1", optimizedPostImageUrl("content://photos/1", 480))
        val base = "https://res.cloudinary.com/iufz7yvd/image/upload/"
        assertEquals(base + "c_limit,w_480,h_480,q_auto,f_auto/v1/spotly/posts/a/b.jpg",
            optimizedPostImageUrl(base + "v1/spotly/posts/a/b.jpg", 480))
    }
}
