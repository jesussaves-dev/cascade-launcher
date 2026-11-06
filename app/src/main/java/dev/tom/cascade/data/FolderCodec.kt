package dev.tom.cascade.data

import dev.tom.cascade.model.Folder

object FolderCodec {
    private const val RECORD = '\u001E'
    private const val FIELD = '\u001F'

    fun encode(folders: List<Folder>): String = folders.joinToString(RECORD.toString()) { folder ->
        (listOf(folder.id, sanitizeFolderName(folder.name)) + folder.appKeys).joinToString(FIELD.toString())
    }

    fun decode(value: String?): List<Folder> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(RECORD).mapNotNull { record ->
            val fields = record.split(FIELD)
            if (fields.size < 2) return@mapNotNull null
            val id = fields[0]
            val name = fields[1]
            if (id.isBlank() || name.isBlank()) return@mapNotNull null
            Folder(id = id, name = name, appKeys = fields.drop(2))
        }
    }

}

fun sanitizeFolderName(name: String): String =
    name.replace(Regex("[\\u001E\\u001F\\r\\n]"), " ").trim()
