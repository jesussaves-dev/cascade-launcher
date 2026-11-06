package dev.tom.cascade.data

enum class WeatherGlyph { Sun, PartlyCloudy, Cloud, Rain, Snow, Storm, Fog }

fun glyphFor(code: Int): WeatherGlyph = when (code) {
    0 -> WeatherGlyph.Sun
    1, 2 -> WeatherGlyph.PartlyCloudy
    3 -> WeatherGlyph.Cloud
    45, 48 -> WeatherGlyph.Fog
    in 51..67, in 80..82 -> WeatherGlyph.Rain
    in 71..77, 85, 86 -> WeatherGlyph.Snow
    in 95..99 -> WeatherGlyph.Storm
    else -> WeatherGlyph.Cloud
}
