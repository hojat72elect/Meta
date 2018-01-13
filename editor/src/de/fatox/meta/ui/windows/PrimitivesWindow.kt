package de.fatox.meta.ui.windows

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.InputEvent
import de.fatox.meta.Primitives
import de.fatox.meta.api.graphics.Renderer
import de.fatox.meta.entity.Meta3DEntity
import de.fatox.meta.injection.Inject
import de.fatox.meta.injection.Singleton
import de.fatox.meta.ui.MetaEditorUI
import de.fatox.meta.ui.components.MetaClickListener
import de.fatox.meta.ui.components.MetaIconTextButton
import de.fatox.meta.ui.tabs.SceneTab

/**
 * Created by Frotty on 20.05.2016.
 */
@Singleton
class PrimitivesWindow : MetaWindow("Primitives", true, true) {
    private val boxButton: MetaIconTextButton

    @Inject
    private val metaEditorUI: MetaEditorUI? = null
    @Inject
    private val primitives: Primitives? = null
    @Inject
    private val renderer: Renderer? = null

    init {
        this.boxButton = MetaIconTextButton("Box", assetProvider.getDrawable("ui/appbar.box.png"))
        boxButton.addListener(object : MetaClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                for (i in 0..99) {
                    val entity = Meta3DEntity(Vector3(MathUtils.random(-10, 10).toFloat(), MathUtils.random(-0.5f, 5.0f), MathUtils.random(-10, 10).toFloat()),
                            primitives!!.sphereFilled)

                    val currentTab = metaEditorUI!!.currentTab
                    (currentTab as? SceneTab)?.sceneHandle?.entityManager?.addStaticEntity(entity)
                    entity.actorModel.transform.scl(4f)
                    entity.actorModel.transform.rotate(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0, 360).toFloat())
                    entity.actorModel.materials.get(0).set(ColorAttribute.createDiffuse(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f))
                    entity.actorModel.materials.get(0).set(TextureAttribute.createNormal(assetProvider.get("models/crates_n.png", Texture::class.java)))
                }
                renderer!!.rebuildCache()
            }
        })

        contentTable.add(boxButton).size(64f)
    }


}
