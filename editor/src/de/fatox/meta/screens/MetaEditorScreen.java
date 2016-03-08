package de.fatox.meta.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.fatox.meta.Meta;
import de.fatox.meta.api.graphics.Renderer;
import de.fatox.meta.api.ide.ui.UIRenderer;
import de.fatox.meta.injection.Inject;

public class MetaEditorScreen extends ScreenAdapter {

    @Inject
    private UIRenderer uiRenderer;

    @Inject
    private Renderer renderer;

    @Inject
    private SpriteBatch spriteBatch;

    @Override
    public void show() {
        Meta.inject(this);
    }

    @Override
    public void render(float delta) {
        update();

        Gdx.gl.glClearColor(0.16862746f, 0.16862746f, 0.16862746f, 1);
        Gdx.gl30.glClearDepthf(1.0f);
        Gdx.gl30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        spriteBatch.begin();
        renderer.render();
        spriteBatch.end();
        uiRenderer.draw();
    }

    private void update() {
        uiRenderer.update();
    }

    @Override
    public void resize(int width, int height) {
        uiRenderer.resize(width, height);
        Gdx.input.setInputProcessor(uiRenderer.getInputProcessor());
    }
}