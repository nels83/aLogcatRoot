package rs.pedjaapps.alogcatroot.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Prefs
{
    public static final String LEVEL_KEY = "level";
    public static final String FORMAT_KEY = "format";
    public static final String BUFFER_KEY = "buffer";
    public static final String TEXTSIZE_KEY = "textsize";
    public static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    public static final String FILTER_PATTERN_KEY = "filterPattern";
    public static final String SEARCH_PATTERN_KEY = "searchPattern";
    public static final String SHARE_HTML_KEY = "shareHtml";
    public static final String KEEP_SCREEN_ON_KEY = "keepScreenOn";

    private static SharedPreferences sharedPrefs = null;

    static
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ALogcatApplication.getContext());
    }

    private static String getString(String key, String def)
    {
        return sharedPrefs.getString(key, def);
    }

    private static void setString(String key, String val)
    {
        Editor e = sharedPrefs.edit();
        e.putString(key, val);
        e.apply();
    }

    private static boolean getBoolean(String key, boolean def)
    {
        return sharedPrefs.getBoolean(key, def);
    }

    private static void setBoolean(String key, boolean val)
    {
        Editor e = sharedPrefs.edit();
        e.putBoolean(key, val);
        e.apply();
    }

    public static Level getLevel()
    {
        return Level.valueOf(getString(LEVEL_KEY, "V"));
    }

    public static void setLevel(Level level)
    {
        setString(LEVEL_KEY, level.toString());
    }

    public static Format getFormat()
    {
        String f = getString(FORMAT_KEY, "BRIEF");

        // UPGRADE
        // can remove at some point

        if (!f.equals(f.toUpperCase()))
        {
            f = f.toUpperCase();
            setString(FORMAT_KEY, f);
        }

        return Format.valueOf(f);
    }

    public static void setFormat(Format format)
    {
        setString(FORMAT_KEY, format.toString());
    }

    public static Buffer getBuffer()
    {
        return Buffer.valueOf(getString(BUFFER_KEY, "MAIN"));
    }

    public static void setBuffer(Buffer buffer)
    {
        setString(BUFFER_KEY, buffer.toString());
    }

    public static Textsize getTextsize()
    {
        return Textsize.valueOf(getString(TEXTSIZE_KEY, "MEDIUM"));
    }

    public static void setTextsize(Textsize textsize)
    {
        setString(TEXTSIZE_KEY, textsize.toString());
    }

    public static String getFilter()
    {
        return getString("filter", null);
    }

    public static String getSearch()
    {
        return getString("search", null);
    }

    public static Pattern getFilterPattern()
    {
        if (!isFilterPattern())
        {
            return null;
        }

        String p = getString("filter", null);
        if (p == null)
        {
            return null;
        }
        try
        {
            return Pattern.compile(p, Pattern.CASE_INSENSITIVE);
        }
        catch (PatternSyntaxException e)
        {
            setString("filter", null);
            Log.w("alogcat", "invalid filter pattern found, cleared");
            return null;
        }
    }

    public static Pattern getSearchPattern()
    {
        if (!isSearchPattern())
        {
            return null;
        }

        String p = getString("search", null);
        if (p == null)
        {
            return null;
        }
        try
        {
            return Pattern.compile(p, Pattern.CASE_INSENSITIVE);
        }
        catch (PatternSyntaxException e)
        {
            setString("search", null);
            Log.w("alogcat", "invalid search pattern found, cleared");
            return null;
        }
    }

    public static void setFilter(String filter)
    {
        setString("filter", filter);
    }

    public static void setSearch(String search)
    {
        setString("search", search);
    }

    public static BackgroundColor getBackgroundColor()
    {
        String c = getString(BACKGROUND_COLOR_KEY, "WHITE");
        BackgroundColor bc;

        try
        {
            bc = BackgroundColor.valueOf(c);
        }
        catch (IllegalArgumentException iae)
        {
            bc = BackgroundColor.valueOfHexColor(c);
        }
        if (bc == null)
        {
            return BackgroundColor.WHITE;
        }

        return bc;
    }

    public static boolean isShareHtml()
    {
        return getBoolean(SHARE_HTML_KEY, false);
    }

    public static boolean isKeepScreenOn()
    {
        return getBoolean(KEEP_SCREEN_ON_KEY, false);
    }

    public static void setKeepScreenOn(boolean keep)
    {
        setBoolean(KEEP_SCREEN_ON_KEY, keep);
    }

    public static boolean isFilterPattern()
    {
        return getBoolean(FILTER_PATTERN_KEY, false);
    }

    public static void setFilterPattern(boolean filterPattern)
    {
        setBoolean(FILTER_PATTERN_KEY, filterPattern);
    }

    public static boolean isSearchPattern()
    {
        return getBoolean(SEARCH_PATTERN_KEY, false);
    }

    public static void setSearchPattern(boolean searchPattern)
    {
        setBoolean(SEARCH_PATTERN_KEY, searchPattern);
    }
}
