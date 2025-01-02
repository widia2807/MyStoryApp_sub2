package com.example.mystoryapp

import com.example.mystoryapp.data.response.ListStoryItemLocal

object Dummy {
    fun generateDummyQuoteResponse(): List<ListStoryItemLocal> {
        val items: MutableList<ListStoryItemLocal> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItemLocal(
                i.toString(),
                "createdAt $i",
                "name $i",
                "description $i",
                "lon $i",
                "id $i",
                "lat $i",
            )
            items.add(story)
        }
        return items
    }
}

