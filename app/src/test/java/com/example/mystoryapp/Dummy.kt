package com.example.mystoryapp

import com.example.mystoryapp.data.response.ListStoryItem

object Dummy {
    fun generateDummyStories(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 1..10) {
            val story = ListStoryItem(
                id = i.toString(),
                name = "Story $i",
                description = "Description $i",
                photoUrl = "https://example.com/photo$i.jpg",
                createdAt = "2024-01-0${i}T10:00:00Z",
                lat = i.toDouble(),
                lon = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}