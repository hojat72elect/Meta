package de.fatox.meta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.fatox.meta.api.AssetProvider;
import de.fatox.meta.api.model.MetaAudioVideoData;
import de.fatox.meta.assets.MetaData;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.modules.MetaEditorModule;
import de.fatox.meta.modules.MetaUIModule;
import de.fatox.meta.screens.MetaEditorScreen;
import de.fatox.meta.sound.MetaMusicPlayer;

public class EditorMeta extends Meta {
    @Inject
    private MetaData metaData;

    @Inject
    private AssetProvider assetProvider;

    private MetaMusicPlayer metaMusicPlayer = new MetaMusicPlayer();

    public EditorMeta() {
        super();
        addModule(new MetaEditorModule());
        addModule(new MetaUIModule());
    }

    @Override
    public void create() {
        inject(this);
        assetProvider.loadAssetsFromFolder(Gdx.files.internal("data/"));
        if (!metaData.has("audioVideoData")) {
            metaData.save("audioVideoData", new MetaAudioVideoData());
        }
        assetProvider.load("roxpack.atlas", TextureAtlas.class);
        metaMusicPlayer.addMusicToPool("Safro_Dreamscape.mp3");
        MetaAudioVideoData audioVideoData = metaData.get("audioVideoData", MetaAudioVideoData.class);
        audioVideoData.apply();
        changeScreen(new MetaEditorScreen());

//        String convert = MDXConverter.INSTANCE.convert(Gdx.files.internal("models/tcBox.mdx"));
//        System.out.println(convert);
    }

}
