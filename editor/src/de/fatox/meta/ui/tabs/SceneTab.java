package de.fatox.meta.ui.tabs;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTable;
import de.fatox.meta.Meta;
import de.fatox.meta.api.ui.UIRenderer;
import de.fatox.meta.camera.ArcCamControl;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.input.MetaInput;
import de.fatox.meta.shader.MetaSceneHandle;
import de.fatox.meta.ui.components.SceneWidget;

/**
 * Created by Frotty on 13.06.2016.
 */
public class SceneTab extends MetaTab {
    private MetaSceneHandle metaSceneHandle;
    private Table table;
    @Inject
    private PerspectiveCamera perspectiveCamera;
    @Inject
    private UIRenderer uiRenderer;
    private ArcCamControl camControl = new ArcCamControl();
    @Inject
    private MetaInput metaInput;

    public SceneTab(MetaSceneHandle sceneHandle) {
        Meta.inject(this);
        this.metaSceneHandle = sceneHandle;
        table = new VisTable();
        table.add(new SceneWidget(metaSceneHandle)).grow();
    }

    @Override
    public String getTabTitle() {
        return metaSceneHandle.getSceneFile().name();
    }

    @Override
    public Table getContentTable() {
        return table;
    }

    @Override
    public void onShow() {
        metaInput.addAdapterForScreen(camControl);
    }

    @Override
    public void onHide() {
        metaInput.removeAdapterFromScreen(camControl);
    }
}
