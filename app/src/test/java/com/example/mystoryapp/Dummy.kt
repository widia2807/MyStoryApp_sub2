package com.example.mystoryapp

import com.example.mystoryapp.data.response.ListStoryItemLocal

object Dummy {
    fun generateDummyQuoteResponse(): List<ListStoryItemLocal> {
        val items: MutableList<ListStoryItemLocal> = arrayListOf(
            ListStoryItemLocal(
                photoUrl = "https://example.com/image1.jpg",
                createdAt = "2024-01-01T12:00:00Z",
                name = "Story 1",
                description = "Description 1",
                lon = "120.0",
                lat = "-7.0",
                id = "1"
            ),
            ListStoryItemLocal(
                photoUrl = "https://example.com/image2.jpg",
                createdAt = "2024-01-02T12:00:00Z",
                name = "Story 2",
                description = "Description 2",
                lon = "121.0",
                lat = "-8.0",
                id = "2"
            )
        )
        // Data tambahan dinamis
        for (i in 3..100) {
            val story = ListStoryItemLocal(
                photoUrl = "https://example.com/image$i.jpg",
                createdAt = "createdAt $i",
                name = "name $i",
                description = "description $i",
                lon = "$i",
                lat = "-$i",
                id = "$i"
            )
            items.add(story)
        }
        return items
    }
}
