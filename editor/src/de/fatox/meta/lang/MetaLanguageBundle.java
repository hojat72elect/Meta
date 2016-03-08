package de.fatox.meta.lang;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import de.fatox.meta.Meta;
import de.fatox.meta.api.Logger;
import de.fatox.meta.api.ide.lang.AvailableLanguages;
import de.fatox.meta.api.ide.lang.LanguageBundle;
import de.fatox.meta.api.ide.persist.Persist;
import de.fatox.meta.injection.Inject;
import de.fatox.meta.injection.Log;

import java.util.Locale;

@Persist(key = "globalConf/")
public class MetaLanguageBundle implements LanguageBundle {
    @Inject
    @Log
    private Logger log;

    public I18NBundle currentBundle;

    @Persist(key = "Current Language", defaultValue = "EN")
    private AvailableLanguages currentLanguage;


    public MetaLanguageBundle() {
        System.out.println("test1");
        Meta.inject(this);
        loadBundle(AvailableLanguages.EN);
    }

    @Override
    public void loadBundle(AvailableLanguages lang) {
        System.out.println("test3");
        FileHandle baseFileHandle = Gdx.files.internal("lang/MetagineBundle");
        Locale locale = new Locale(lang.toString().toLowerCase(), lang.toString(), "");
        try {
            currentBundle = I18NBundle.createBundle(baseFileHandle, locale);
        } catch (Exception e) {
            log.error("MetaLanguageBundle", e.getLocalizedMessage());
        }
        System.out.println("test4");
        log.debug("MetaLanguageBundle", "Loaded: " + lang);
        log.debug("MetaLanguageBundle", "Current Locale: " + currentBundle.getLocale());
    }

    /**
     * Returns the S
     *
     * @param key
     */
    @Override
    public String get(String key) {
        return currentBundle.get(key);
    }
}