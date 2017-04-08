package de.fatox.meta;

import de.fatox.meta.api.dao.MetaAudioVideoData;
import de.fatox.meta.api.dao.MetaData;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.modules.MetaEditorModule;
import de.fatox.meta.modules.MetaUIModule;
import de.fatox.meta.screens.MetaEditorScreen;

public class EditorMeta extends Meta {
    @Inject
    private MetaData metaData;

    public EditorMeta() {
        super();
        addModule(new MetaEditorModule());
        addModule(new MetaUIModule());
    }

    @Override
    public void create() {
        inject(this);
        if(!metaData.has("audioVideoData")) {
            metaData.save("audioVideoData", new MetaAudioVideoData());
        }
        MetaAudioVideoData audioVideoData = metaData.get("audioVideoData", MetaAudioVideoData.class);
        audioVideoData.apply();
        changeScreen(new MetaEditorScreen());
    }

}
