package de.fatox.meta.ui

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.badlogic.gdx.utils.reflect.ReflectionException
import com.kotcrab.vis.ui.widget.MenuBar
import de.fatox.meta.api.DummyPosModifier
import de.fatox.meta.api.MetaInputProcessor
import de.fatox.meta.api.PosModifier
import de.fatox.meta.api.model.MetaWindowData
import de.fatox.meta.api.ui.UIManager
import de.fatox.meta.api.ui.UIRenderer
import de.fatox.meta.assets.MetaData
import de.fatox.meta.injection.MetaInject.Companion.lazyInject
import de.fatox.meta.injection.Singleton
import de.fatox.meta.input.MetaInput
import de.fatox.meta.ui.windows.MetaDialog
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by Frotty on 20.05.2016.
 */
@Singleton
class MetaUiManager : UIManager {
	private val uiRenderer: UIRenderer by lazyInject()
	private val metaData: MetaData by lazyInject()
	private val metaInput: MetaInputProcessor by lazyInject()

	private val displayedWindows = Array<Window>()
	private val cachedWindows = Array<Window>()
	private var mainMenuBar: MenuBar? = null
	private val contentTable = Table()
	private var currentScreenId: String? = "(none)"
	override var posModifier: PosModifier = DummyPosModifier

	override fun moveWindow(x: Int, y: Int) {
		posModifier.modify(x, y)
	}

	override fun resize(width: Int, height: Int) {
		uiRenderer.resize(width, height)
	}

	override fun changeScreen(screenIdentifier: String?) {
		currentScreenId = screenIdentifier
		metaInput.changeScreen()

		// Close or move currently shown windows
		for (window in displayedWindows) {
			val name = window!!.javaClass.name
			if (metaHas(name)) {
				val metaWindowData = metaGet(name, MetaWindowData::class.java)!!
				if (metaWindowData.displayed) {
					// There exists saved window metadata
					metaWindowData.set(window)
				} else {
					cacheWindow(window, true)
				}
			} else {
				cacheWindow(window, true)
			}
		}
		displayedWindows.removeAll(cachedWindows, true)
		contentTable.remove()
		contentTable.clear()
		if (mainMenuBar != null) {
			contentTable.row().height(26f)
			contentTable.add(mainMenuBar!!.table).growX().top()
			mainMenuBar!!.table.toFront()
		}
		uiRenderer.addActor(contentTable)
		restoreWindows()
	}

	private fun restoreWindows() {
		val list = metaData.getCachedHandle(currentScreenId).list()
		outer@ for (fh in list) {
			if (fh.name().endsWith("Window")) {
				try {
					val windowClass: Class<out Window> = ClassReflection.forName(fh.name()) as Class<out Window>
					val metaWindowData = metaGet(windowClass.name, MetaWindowData::class.java)!!
					for (displayedWindow in displayedWindows) {
						if (displayedWindow!!.javaClass == windowClass) {
							if (!metaWindowData.displayed) {
								cacheWindow(displayedWindow, true)
							}
							continue@outer
						}
					}
					if (metaWindowData.displayed) {
						metaWindowData.set(showWindow<Window>(windowClass))
					}
				} catch (e: ReflectionException) {
					fh.delete()
					e.printStackTrace()
				}
			}
		}
	}

	override fun addTable(table: Table?, growX: Boolean, growY: Boolean) {
		contentTable.row()
		val add = contentTable.add(table)
		if (growX) add.growX()
		if (growY) add.growY()
		contentTable.invalidate()
	}

	/**
	 * Shows an instance of the given class on the current screen.
	 * If metadata exists for the window, it will be loaded.
	 *
	 * @param windowClass The window to show
	 */
	override fun <T : Window> showWindow(windowClass: Class<out T>): T {
		log.debug("show window: " + windowClass.name)
		val window: Window? = displayWindow()
		window!!.isVisible = true
		if (metaHas(windowClass.name)) {
			// There exists metadata for this window.
			val windowData = metaGet(windowClass.name, MetaWindowData::class.java)!!
			windowData.set(window)
			if (!windowData.displayed) {
				windowData.displayed = true
				metaSave(windowClass.name, windowData)
			}
		} else {
			// First time the window has been shown on this screen
			metaSave(windowClass.name, MetaWindowData(window))
		}
		return window as T
	}

	override fun <T : MetaDialog> showDialog(dialogClass: Class<out T>): T {
		log.debug("show dialog: " + dialogClass.name)
		// Dialogs are just Window subtypes so we show it as usual
		val dialog: MetaDialog = showWindow(dialogClass)
		dialog.show()
		return dialog as T
	}

	override fun setMainMenuBar(menuBar: MenuBar?) {
		if (menuBar != null) {
			contentTable.row().height(26f)
			contentTable.add(menuBar.table).growX().top()
		} else if (mainMenuBar != null) {
			contentTable.removeActor(mainMenuBar!!.table)
		}
		mainMenuBar = menuBar
	}

	override fun <T : Window> getWindow(windowClass: Class<out T>): T {
		// TODO avoid NPE
		return getDisplayedClass(windowClass)
			?: throw GdxRuntimeException("Can't find ${windowClass.canonicalName}")
	}

	override fun closeWindow(window: Window) {
		val displayedWindow = getDisplayedInstance(window)
		if (displayedWindow != null) {
			displayedWindows.removeValue(window, true)
			val metaWindowData = metaGet(window.javaClass.name, MetaWindowData::class.java)
			if (metaWindowData != null) {
				metaWindowData.displayed = false
				metaSave(displayedWindow.javaClass.name, metaWindowData)
			}
			cacheWindow(window, false)
		}
	}

	override fun updateWindow(window: Window) {
		val name = window.javaClass.name
		if (metaHas(name)) {
			val metaWindowData = metaGet(name, MetaWindowData::class.java)!!
			metaWindowData.setFrom(window)
			metaSave(name, metaWindowData)
		}
	}

	override fun bringWindowsToFront() {
		for (window in displayedWindows) {
			window!!.toFront()
		}
		mainMenuBar!!.table.toFront()
	}

	private inline fun <reified T : Window> displayWindow(): T? {
		// Check if this window is a singleton. If it is and it is displayed, return displayed instance
		var theWindow = checkSingleton<T>()
		if (theWindow != null) {
			log.debug("singleton already displaying")
			return theWindow
		}
		// Check for a cached instance
		for (cachedWindow in cachedWindows) {
			if (cachedWindow!!.javaClass == T::class.java) {
				log.debug("found cached")
				theWindow = cachedWindow as T?
				break
			}
		}
		// If there was no cached instance we create a new one
		if (theWindow != null) {
			cachedWindows.removeValue(theWindow, true)
		} else {
			try {
				log.debug("try instance")
				theWindow = T::class.java.newInstance()
			} catch (e: InstantiationException) {
				e.printStackTrace()
			} catch (e: IllegalAccessException) {
				e.printStackTrace()
			}
		}
		uiRenderer.addActor(theWindow!!)
		displayedWindows.add(theWindow)
		return theWindow
	}

	private inline fun <reified T : Window> checkSingleton(): T? {
		if (T::class.java.isAnnotationPresent(Singleton::class.java)) {
			val displayedWindow: T? = getDisplayedClass(T::class.java)
			if (displayedWindow != null) {
				return displayedWindow
			}
		}
		return null
	}

	private fun <T : Window> getDisplayedClass(windowClass: Class<out T>): T? {
		return displayedWindows.firstOrNull { it.javaClass === windowClass } as T?
	}

	private fun getDisplayedInstance(window: Window): Window? {
		return displayedWindows.firstOrNull { it === window }
	}

	private fun cacheWindow(window: Window?, forceClose: Boolean) {
		cachedWindows.add(window)
		window!!.isVisible = false
		if (forceClose) {
			window.remove()
		}
	}

	override fun metaHas(name: String): Boolean {
		return metaData.has(currentScreenId + File.separator + name)
	}

	override fun <T> metaGet(name: String, c: Class<T>): T? {
		return metaData[currentScreenId + File.separator + name, c]
	}

	override fun metaSave(name: String, windowData: Any?) {
		val id = currentScreenId + File.separator + name
		if (TimeUtils.timeSinceMillis(metaData.getCachedHandle(id).lastModified()) > 200) {
			metaData.save(id, windowData)
		}
	}

	private val copy = Array<Window>()
	override val currentlyActiveWindows: Array<Window>
		get() {
			copy.clear()
			copy.addAll(displayedWindows)
			return copy
		}

	init {
		contentTable.apply {
			top().left()
			setPosition(0f, 0f)
			setFillParent(true)
		}
		uiRenderer.addActor(contentTable)
	}
}

private val log: Logger = LoggerFactory.getLogger(MetaUiManager::class.java)