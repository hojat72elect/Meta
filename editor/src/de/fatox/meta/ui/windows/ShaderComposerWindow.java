package de.fatox.meta.ui.windows;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import de.fatox.meta.api.dao.RenderBufferData;
import de.fatox.meta.api.graphics.RenderBufferHandle;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.injection.Singleton;
import de.fatox.meta.shader.MetaShaderComposer;
import de.fatox.meta.shader.MetaShaderLibrary;
import de.fatox.meta.shader.ShaderComposition;
import de.fatox.meta.ui.components.MetaClickListener;
import de.fatox.meta.ui.components.MetaLabel;
import de.fatox.meta.ui.components.RenderBufferButton;
import de.fatox.meta.ui.dialogs.ShaderCompositionWizard;

import java.util.Objects;

/**
 * Created by Frotty on 29.07.2016.
 */
@Singleton
public class ShaderComposerWindow extends MetaWindow {
    @Inject
    private MetaShaderLibrary shaderLibrary;
    @Inject
    private MetaShaderComposer shaderComposer;

    private VisSelectBox<String> renderSelectbox;
    private VisTable bufferTable;
    private VisImageButton addButton;

    public ShaderComposerWindow() {
        super("Shader Composer", true, true);
        setupEmpty();
        shaderComposer.addListener(this::onRemoveBuffer);
        shaderLibrary.addListener(() -> {
            loadComposition(Objects.requireNonNull(shaderComposer.getCurrentComposition()));
        });
    }

    private void setupEmpty() {
        VisImageButton visImageButton = new VisImageButton(getAssetProvider().getDrawable("ui/appbar.page.add.png"));
        visImageButton.addListener(new MetaClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getUiManager().showDialog(ShaderCompositionWizard.class);
            }
        });
        visImageButton.getImage().setScaling(Scaling.fill);
        visImageButton.getImage().setSize(24, 24);

        bufferTable = new VisTable();
        bufferTable.top().left();
        renderSelectbox = new VisSelectBox<>();
        renderSelectbox.setItems(new Array<>());
        renderSelectbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ShaderComposition selectedComp = shaderComposer.getComposition(renderSelectbox.getSelected());
                if (selectedComp != null && shaderComposer.getCurrentComposition() != selectedComp) {
                    shaderComposer.setCurrentComposition(selectedComp);
                    loadComposition(selectedComp);
                }
            }
        });
        getContentTable().left();
        getContentTable().add(visImageButton).size(24).top().left().padRight(2);
        getContentTable().add(renderSelectbox).width(256).left();
        getContentTable().add().growX();
        getContentTable().row().padTop(2);
        getContentTable().add(bufferTable).colspan(3).grow();

        if (shaderComposer != null && shaderComposer.getCompositions().size > 0) {
            for (int i = 0; i < shaderComposer.getCompositions().size; i++) {
                addComposition(shaderComposer.getCompositions().get(i));
            }
        }
    }

    private void setupNewBufferButton() {
        addButton = new VisImageButton(getAssetProvider().getDrawable("ui/appbar.layer.add.png"));
        addButton.addListener(new MetaClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onAddBuffer();
            }
        });
        addButton.getImage().setAlign(Align.center);
        bufferTable.add(addButton).size(175, 128).left();
    }

    private void onAddBuffer() {
        RenderBufferHandle bufferHandle = shaderComposer.addRenderBuffer(new RenderBufferData(shaderLibrary.getDefaultShaderPath()));
        RenderBufferButton newButton = new RenderBufferButton(bufferHandle);
        bufferTable.getCell(addButton).setActor(newButton).padRight(2);
        bufferTable.add(new MetaLabel(">", 14)).center().padRight(2);
        bufferTable.add(addButton).size(175, 100).left();
        bufferTable.pack();
    }

    private void onRemoveBuffer() {
        loadComposition(Objects.requireNonNull(shaderComposer.getCurrentComposition()));
    }

    private void loadBuffers(Array<RenderBufferHandle> buffers) {
        bufferTable.clear();
        for (RenderBufferHandle buffer : buffers) {
            loadBuffer(buffer);
        }
    }

    private void loadBuffer(RenderBufferHandle buffer) {
        RenderBufferButton newButton = new RenderBufferButton(buffer);
        bufferTable.add(newButton).padRight(2);
        bufferTable.add(new MetaLabel(">", 14)).center().padRight(2);
    }

    public void addComposition(ShaderComposition shaderComposition) {
        Array<String> items = new Array<>(renderSelectbox.getItems());
        items.add(shaderComposition.getData().getName());
        renderSelectbox.setItems(items);
        renderSelectbox.setSelected(shaderComposition.getData().getName());

        loadComposition(shaderComposition);
    }

    private void loadComposition(ShaderComposition shaderComposition) {
        bufferTable.clear();
        if (shaderComposition.getData().getRenderBuffers().size > 0) {
            // Load existing
            loadBuffers(shaderComposition.getBufferHandles());
        }
        setupNewBufferButton();
    }
}
