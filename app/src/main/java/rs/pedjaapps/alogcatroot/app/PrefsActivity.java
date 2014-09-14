package rs.pedjaapps.alogcatroot.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    private ListPreference mLevelPreference;
    private ListPreference mFormatPreference;
    private ListPreference mBufferPreference;
    private ListPreference mTextsizePreference;
    private ListPreference mBackgroundColorPreference;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        mLevelPreference = (ListPreference) getPreferenceScreen().findPreference(Prefs.LEVEL_KEY);
        mFormatPreference = (ListPreference) getPreferenceScreen().findPreference(Prefs.FORMAT_KEY);
        mBufferPreference = (ListPreference) getPreferenceScreen().findPreference(Prefs.BUFFER_KEY);
        mTextsizePreference = (ListPreference) getPreferenceScreen().findPreference(Prefs.TEXTSIZE_KEY);
        mBackgroundColorPreference = (ListPreference) getPreferenceScreen().findPreference(Prefs.BACKGROUND_COLOR_KEY);

        setResult(Activity.RESULT_OK);
    }

    private void setLevelTitle()
    {
        mLevelPreference.setTitle(getString(R.string.level) + " (" + Prefs.getLevel().getTitle(this) + ")");
    }

    private void setFormatTitle()
    {
        mFormatPreference.setTitle(getString(R.string.format) + " (" + Prefs.getFormat().getTitle(this) + ")");
    }

    private void setBufferTitle()
    {
        mBufferPreference.setTitle(getString(R.string.buffer) + " (" + Prefs.getBuffer().getTitle(this) + ")");
    }

    private void setTextsizeTitle()
    {
        mTextsizePreference.setTitle(getString(R.string.text_size) + " (" + Prefs.getTextsize().getTitle(this) + ")");
    }

    private void setBackgroundColorTitle()
    {
        mBackgroundColorPreference.setTitle(getString(R.string.background_color) + " (" + Prefs.getBackgroundColor().getTitle(this) + ")");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setLevelTitle();
        setFormatTitle();
        setBufferTitle();
        setTextsizeTitle();
        setBackgroundColorTitle();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        switch (key)
        {
            case Prefs.LEVEL_KEY:
                setLevelTitle();
                break;
            case Prefs.FORMAT_KEY:
                setFormatTitle();
                break;
            case Prefs.BUFFER_KEY:
                setBufferTitle();
                break;
            case Prefs.TEXTSIZE_KEY:
                setTextsizeTitle();
                break;
            case Prefs.BACKGROUND_COLOR_KEY:
                setBackgroundColorTitle();
                break;
        }
    }
}
