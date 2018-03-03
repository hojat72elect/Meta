package de.fatox.meta.assets

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile

enum class XPKTypes(val letter: String, val cb: Class<out Disposable>?) {

    INVALID("", null), SOUND("s", Sound::class.java), MUSIC("m", Music::class.java), TEXTURE("t", Texture::class.java), SHADER("x", Texture::class.java)
}

object XPKLoader {
    const val EXTENSION = "xpk"


    fun detectFileType(letter: String): XPKTypes {
        XPKTypes.values().forEach {
            if (it.letter == letter) {
                return it
            }
        }
        return XPKTypes.INVALID
    }

    fun getList(fileHandle: FileHandle): Array<XPKFileHandle> {
        val array = Array<XPKFileHandle>()
        val sevenZFile = SevenZFile(fileHandle.file())
        var it = sevenZFile.nextEntry
        do {
            val xpkFileHandle = XPKFileHandle(array, 0, fileHandle, it, it.name)
            array.add(xpkFileHandle)
            it = sevenZFile.nextEntry
        } while (it != null)
        sevenZFile.close()
        return array
    }

    fun loadEntry(file: FileHandle, entry: SevenZArchiveEntry): ByteArray? {
        val s7f = SevenZFile(file.file())
        s7f.use {
            var itr = it.nextEntry
            do {
                if (itr.name == entry.name) {
                    val content = ByteArray(itr.size.toInt())
                    s7f.read(content, 0, itr.size.toInt())
                    return content
                }
                itr = it.nextEntry
            } while (itr != null)
        }
        return null
    }


}