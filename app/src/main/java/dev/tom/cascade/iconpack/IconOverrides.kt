package dev.tom.cascade.iconpack

fun overrideDrawable(
    overrides: Map<String, String>,
    appKey: String,
    activePack: String?,
): String? = if (activePack == null) null else overrides[appKey]
