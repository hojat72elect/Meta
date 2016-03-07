package de.fatox.meta;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import de.fatox.meta.api.AssetProvider;

public class MetaAssetProvider implements AssetProvider {
    private AssetManager assetManager = new AssetManager();

    public MetaAssetProvider() {
        BitmapFontLoader.BitmapFontParameter parameter = new BitmapFontLoader.BitmapFontParameter();
        parameter.genMipMaps = true;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;

        load("fonts/meta.fnt", BitmapFont.class);
        assetManager.finishLoading();
    }

    @Override
    public <T> T get(String fileName, Class<T> type) {
        return assetManager.get(fileName, type);
    }

    @Override
    public <T> void load(String fileName, Class<T> type) {
        assetManager.load(fileName, type);
    }

    @Override
    public <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
        assetManager.load(fileName, type, parameter);
    }
}
