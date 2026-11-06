package dev.tom.cascade.data

sealed interface ButtonAction {
    data object Search : ButtonAction
    data object Voice : ButtonAction
    data class OpenApp(val key: String) : ButtonAction
}

fun encodeButtonAction(action: ButtonAction): String = when (action) {
    ButtonAction.Search -> "search"
    ButtonAction.Voice -> "voice"
    is ButtonAction.OpenApp -> "app:${action.key}"
}

fun decodeButtonAction(
    value: String?,
    fallback: ButtonAction = ButtonAction.Search,
): ButtonAction = when {
    value == "search" -> ButtonAction.Search
    value == "voice" -> ButtonAction.Voice
    value != null && value.startsWith("app:") && value.length > 4 ->
        ButtonAction.OpenApp(value.removePrefix("app:"))
    else -> fallback
}
