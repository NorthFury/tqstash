package tqstash

import java.io.File

fun readTranslations(file: File): Map<String, String> {
    val arcFile = ArcFile(file)

    val translationsMap = mutableMapOf<String, String>()
    TRANSLATION_FILES.forEach { fileName ->
        arcFile.inputStream(fileName)?.use {
            it.reader(charset("UTF-16")).useLines {
                it.forEach { line ->
                    if (!line.startsWith("//") && !line.isBlank()) {
                        val (key, value) = line.split('=')
                        translationsMap.put(key, value)
                    }
                }
            }
        }
    }

    return translationsMap
}

private val TRANSLATION_FILES = setOf(
        // tags
        "commonequipment.txt",
        "xcommonequipment.txt",
        "monsters.txt",
        "xmonsters.txt",
        "quest.txt",
        "xquest.txt",
        "skills.txt",
        "xskills.txt",
        "uniqueequipment.txt",
        "xuniqueequipment.txt",
        // ui
        "ui.txt",
        "xui.txt"
)
