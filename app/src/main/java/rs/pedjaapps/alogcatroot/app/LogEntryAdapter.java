package rs.pedjaapps.alogcatroot.app;

import android.app.Activity;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LogEntryAdapter extends ArrayAdapter<LogEntry>
{
    private String mSearch = null;
    private Pattern mSearchPattern = null;
    private boolean mIsSearchPattern;

    private Activity mActivity;
    private List<LogEntry> entries;

    public LogEntryAdapter(Activity activity, int resourceId, List<LogEntry> entries)
    {
        super(activity, resourceId, entries);
        this.mActivity = activity;
        this.entries = entries;
        mIsSearchPattern = Prefs.isSearchPattern();
        mSearch = Prefs.getSearch();
        mSearchPattern = Prefs.getSearchPattern();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LogEntry entry = entries.get(position);
        TextView tv;
        if (convertView == null)
        {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            tv = (TextView) inflater.inflate(R.layout.entry, null);
        }
        else
        {
            tv = (TextView) convertView;
        }

        tv.setText(entry.getText());
        tv.setTextColor(entry.getLevel().getColor());
        if (mIsSearchPattern)
        {
            if (mSearchPattern != null && mSearchPattern.matcher(entry.getText()).find())
            {
                tv.setBackgroundColor(Color.GRAY);
            }
        }
        else
        {
            if (mSearch != null && entry.getText().toLowerCase().contains(mSearch.toLowerCase()))
            {
                tv.setBackgroundColor(Color.GRAY);
            }
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Prefs.getTextsize().getValue());

        return tv;
    }

    public void remove(int position)
    {
        LogEntry entry = entries.get(position);
        remove(entry);
    }

    public boolean areAllItemsEnabled()
    {
        return false;
    }

    public boolean isEnabled(int position)
    {
        return false;
    }

    public LogEntry get(int position)
    {
        return entries.get(position);
    }

    public List<LogEntry> getEntries()
    {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public void clear()
    {
        mIsSearchPattern = Prefs.isSearchPattern();
        mSearch = Prefs.getSearch();
        mSearchPattern = Prefs.getSearchPattern();
        super.clear();
    }

    @Override
    public void notifyDataSetChanged()
    {
        mIsSearchPattern = Prefs.isSearchPattern();
        mSearch = Prefs.getSearch();
        mSearchPattern = Prefs.getSearchPattern();
        super.notifyDataSetChanged();
    }
}

