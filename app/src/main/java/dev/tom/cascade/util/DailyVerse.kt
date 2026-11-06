package dev.tom.cascade.util

import dev.tom.cascade.model.Verse

fun verseForDay(epochDay: Long, verses: List<Verse>): Verse =
    verses[Math.floorMod(epochDay, verses.size.toLong()).toInt()]
