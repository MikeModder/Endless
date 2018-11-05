package me.artuto.endless;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;

/**
 * @author Artuto
 */

public enum Locale
{
    EN_US("en_US", "English (US)", "English (US)", "\uD83C\uDDFA\uD83C\uDDF8", new String[]{"english", "en"}),
    ES_MX("es_MX", "Spanish (Mexico)", "Español (México)", "\uD83C\uDDF2\uD83C\uDDFD", new String[]{"spanish", "español", "es"}),
    DE_DE("de_DE", "German (Germany)", "Deutsch, (Deutschland)", "\uD83C\uDDE9\uD83C\uDDEA", new String[]{"german", "deutsch", "de"}),
    FR_FR("fr_FR", "French (France)", "Francais (France)", "\uD83C\uDDEB\uD83C\uDDF7", new String[]{"french", "francais", "fr"});

    private java.util.Locale locale;
    private ResourceBundle bundle;
    private String code, englishName, flag, localizedName;
    private String[] aliases;

    private ClassLoader loader;

    Locale(String code, String englishName, String localizedName, String flag, String[] aliases)
    {
        File folder = new File("lang");
        URL[] url = new URL[0];
        try {url = new URL[]{folder.toURI().toURL()};}
        catch(MalformedURLException e) {e.printStackTrace();}
        this.loader = new URLClassLoader(url);

        this.locale = new java.util.Locale(code);
        this.bundle = ResourceBundle.getBundle("Endless", locale, loader);
        this.code = code;
        this.englishName = englishName;
        this.flag = flag;
        this.localizedName = localizedName;
        this.aliases = aliases;
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }

    public java.util.Locale getLocale()
    {
        return locale;
    }

    public ResourceBundle getBundle()
    {
        return bundle;
    }

    public String getCode()
    {
        return code;
    }

    public String getEnglishName()
    {
        return englishName;
    }

    public String getFlag()
    {
        return flag;
    }

    public String getLocalizedName()
    {
        return localizedName;
    }

    public String[] getAliases()
    {
        return aliases;
    }

    public void reload()
    {
        this.bundle = ResourceBundle.getBundle("Endless", getLocale(), getClassLoader());
        Endless.LOG.debug("Reloaded {}", getCode());
    }
}
