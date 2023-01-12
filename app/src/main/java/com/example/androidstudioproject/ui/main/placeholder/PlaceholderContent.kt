package com.example.androidstudioproject.placeholder

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()


    fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
    }
    fun clear(ITEMS: MutableList<PlaceholderItem>)
    {
        ITEMS.clear()
    }

    fun createPlaceholderItem(position: String, device: String): PlaceholderItem {
        return PlaceholderItem(position, device)
    }



    /**
     * A placeholder item representing a piece of content.
     */
    class PlaceholderItem(val mac: String, val content: String) {
        override fun toString(): String = content

    }
}