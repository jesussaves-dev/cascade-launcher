package dev.tom.cascade.data

import dev.tom.cascade.model.Folder

fun moveApp(folders: List<Folder>, folderId: String, appKey: String): List<Folder> =
    folders.map { folder ->
        when {
            folder.id == folderId && appKey !in folder.appKeys -> folder.copy(appKeys = folder.appKeys + appKey)
            folder.id == folderId -> folder
            else -> folder.copy(appKeys = folder.appKeys - appKey)
        }
    }

fun deleteFolder(folders: List<Folder>, folderId: String): List<Folder> =
    folders.filterNot { it.id == folderId }
