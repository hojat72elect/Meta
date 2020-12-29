package de.fatox.meta.ide

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import de.fatox.meta.Meta.Companion.inject
import de.fatox.meta.api.ui.UIManager
import de.fatox.meta.injection.Inject
import de.fatox.meta.ui.windows.AssetDiscovererWindow

/**
 * Created by Frotty on 07.06.2016.
 */
class AssetDiscoverer {
    @Inject
    private val uiManager: UIManager? = null

    @Inject
    private val projectManager: ProjectManager? = null
    var root: FileHandle? = null
        private set
    var currentFolder: FileHandle? = null
        private set
    var currentChildFolders: Array<FileHandle>? = null
        private set
    var currentChildFiles: Array<FileHandle>? = null
        private set
    private val fileOpenListeners = ObjectMap<String, AssetOpenListener>()
    fun openFolder(fileHandle: FileHandle?) {
        currentFolder = fileHandle
        refresh()
    }

    fun openChild(name: String?) {
        currentFolder = currentFolder!!.child(name)
        refresh()
    }

    fun refresh() {
        currentChildFolders = Array()
        currentChildFiles = Array()
        for (child in currentFolder!!.list()) {
            if (child.isDirectory) {
                currentChildFolders!!.add(child)
            } else {
                currentChildFiles!!.add(child)
            }
        }
        val window = uiManager!!.getWindow(AssetDiscovererWindow::class.java)
        if (window != null) {
            window.refresh()
        }
    }

    fun openFile(fileHandle: FileHandle?) {
        if (fileOpenListeners.containsKey(fileHandle!!.extension())) {
            fileOpenListeners.get(fileHandle.extension()).onOpen(fileHandle)
        }
    }

    fun addOpenListener(extension: String, listener: AssetOpenListener) {
        fileOpenListeners.put(extension, listener)
    }

    fun setRoot(path: String) {
        currentFolder = if (path.length <= 0) {
            projectManager.getCurrentProjectRoot()
        } else {
            projectManager.getCurrentProjectRoot().child(path)
        }
        this.root = currentFolder
        refresh()
    }

    fun setRoot(root: FileHandle?) {
        this.root = root
        currentFolder = root
        refresh()
    }

    init {
        inject(this)
    }
}