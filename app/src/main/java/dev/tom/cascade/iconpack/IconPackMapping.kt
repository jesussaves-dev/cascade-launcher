package dev.tom.cascade.iconpack

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class IconPackMapping private constructor(
    private val byComponent: Map<String, String>,
    private val byPackage: Map<String, String>,
) {
    fun drawableName(packageName: String, activityName: String): String? {
        val activity = if (activityName.startsWith(".")) packageName + activityName else activityName
        byComponent["$packageName/$activity"]?.let { return it }
        return byPackage[packageName]
    }

    companion object {
        fun fromItems(items: List<Pair<String, String>>): IconPackMapping {
            val byComponent = HashMap<String, String>()
            val byPackage = HashMap<String, String>()
            items.forEach { (raw, drawable) ->
                val key = normalize(raw) ?: return@forEach
                val (packageName, activity) = key
                byComponent.putIfAbsent("$packageName/$activity", drawable)
                byPackage.putIfAbsent(packageName, drawable)
            }
            return IconPackMapping(byComponent, byPackage)
        }

        fun parse(input: InputStream): IconPackMapping {
            val factory = DocumentBuilderFactory.newInstance()
            runCatching { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
            factory.isExpandEntityReferences = false
            val document = factory.newDocumentBuilder().parse(input)
            val nodes = document.getElementsByTagName("item")
            val items = buildList {
                for (index in 0 until nodes.length) {
                    val node = nodes.item(index)
                    val attributes = node.attributes ?: continue
                    val component = attributes.getNamedItem("component")?.nodeValue?.trim().orEmpty()
                    val drawable = attributes.getNamedItem("drawable")?.nodeValue?.trim().orEmpty()
                    if (component.isNotEmpty() && drawable.isNotEmpty()) add(component to drawable)
                }
            }
            return fromItems(items)
        }

        private fun normalize(raw: String): Pair<String, String>? {
            var value = raw.trim()
            if (value.startsWith("ComponentInfo{")) value = value.removePrefix("ComponentInfo{")
            if (value.endsWith("}")) value = value.dropLast(1)
            val packageName = value.substringBefore('/', "").trim()
            var activity = value.substringAfter('/', "").trim()
            if (packageName.isEmpty() || activity.isEmpty()) return null
            activity = when {
                activity.startsWith(".") -> packageName + activity
                '.' !in activity -> "$packageName.$activity"
                else -> activity
            }
            return packageName to activity
        }
    }
}
