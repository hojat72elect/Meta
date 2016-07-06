package de.fatox.meta.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.fatox.meta.api.dao.MetaEditorData;
import de.fatox.meta.ide.ProjectManager;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.ui.components.TextWidget;

/**
 * Created by Frotty on 05.06.2016.
 */
public class WelcomeTab extends MetaTab {
    private VisTable visTable = new VisTable();
    @Inject
    private MetaEditorData metaEditorData;
    @Inject
    private ProjectManager projectManager;

    public WelcomeTab() {
        super(false, false);
        visTable.top();
        visTable.row().height(128);
        visTable.add(new TextWidget("Meta"));
        visTable.row().height(64);
        visTable.add();
        visTable.row();
        VisLabel visLabel = new VisLabel("Welcome to the Meta Engine\nCreate or load a project\n\nRecent projects:");
        visLabel.setAlignment(Align.center);

        visTable.add(visLabel).pad(16);

        for (String lastProj : metaEditorData.getLastProjectFiles()) {
            visTable.row();
            LinkLabel linkLabel = new LinkLabel(lastProj.substring(0, lastProj.lastIndexOf("/")));
            linkLabel.setListener(url -> projectManager.loadProject(Gdx.files.absolute(lastProj)));
            visTable.add(linkLabel).center().pad(2);
        }
    }

    @Override
    public void onDisplay() {
    }

    @Override
    public String getTabTitle() {
        return "Home";
    }

    @Override
    public Table getContentTable() {
        return visTable;
    }
}
