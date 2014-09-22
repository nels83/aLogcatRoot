package rs.pedjaapps.alogcatroot.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogActivity extends ListActivity
{
    static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ");
    static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy HH:mm:ss ZZZZ");
    private static final Executor EX = Executors.newCachedThreadPool();

    private static final int PREFS_REQUEST = 1;

    private static final int MENU_FILTER = 1;
    private static final int MENU_SEARCH = 13;
    private static final int MENU_SHARE = 5;
    private static final int MENU_PLAY = 6;
    private static final int MENU_CLEAR = 8;
    private static final int MENU_SAVE = 9;
    private static final int MENU_PREFS = 10;
    private static final int MENU_JUMP_TOP = 11;
    private static final int MENU_JUMP_BOTTOM = 12;

    static final int WINDOW_SIZE = 1000;

    static final int CAT_WHAT = 0;
    static final int CLEAR_WHAT = 2;

    private ListView mLogList;
    private LogEntryAdapter mLogEntryAdapter;
    private MenuItem mPlayItem;
    private MenuItem mFilterItem;
    private MenuItem mSearchItem;

    private Level mLastLevel = Level.V;
    private LogCat mLogCat;
    private boolean mPlay = true;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CAT_WHAT:
                    final List<String> lines = (List<String>) msg.obj;
                    cat(lines);
                    break;
                case CLEAR_WHAT:
                    mLogEntryAdapter.clear();
                    break;
            }
        }
    };

    private void jumpTop()
    {
        pauseLog();
        mLogList.post(new Runnable()
        {
            public void run()
            {
                mLogList.setSelection(0);
            }
        });
    }

    private void jumpBottom()
    {
        playLog();
        mLogList.setSelection(mLogEntryAdapter.getCount() - 1);
    }

    private void cat(final String s)
    {
        if (mLogEntryAdapter.getCount() > WINDOW_SIZE)
        {
            mLogEntryAdapter.remove(0);
        }

        Format format = mLogCat.mFormat;
        Level level = format.getLevel(s);
        if (level == null)
        {
            level = mLastLevel;
        }
        else
        {
            mLastLevel = level;
        }

        final LogEntry entry = new LogEntry(s, level);
        mLogEntryAdapter.add(entry);
    }

    private void cat(List<String> lines)
    {
        for (String line : lines)
        {
            cat(line);
        }
        jumpBottom();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.log);
        getWindow().setTitle(getResources().getString(R.string.app_name));

        mLogList = (ListView) findViewById(android.R.id.list);
        mLogList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener()
        {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
            {
                MenuItem jumpTopItem = menu.add(0, MENU_JUMP_TOP, 0, R.string.jump_start_menu);
                jumpTopItem.setIcon(android.R.drawable.ic_media_previous);

                MenuItem jumpBottomItem = menu.add(0, MENU_JUMP_BOTTOM, 0, R.string.jump_end_menu);
                jumpBottomItem.setIcon(android.R.drawable.ic_media_next);
            }
        });
        mLogList.setOnScrollListener(new AbsListView.OnScrollListener()
        {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                pauseLog();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
            }
        });
        mLogCat = new LogCat(mHandler);
    }



    @Override
    public void onStart()
    {
        super.onStart();
    }

    private void init()
    {
        BackgroundColor bc = Prefs.getBackgroundColor();
        int color = bc.getColor();
        mLogList.setBackgroundColor(color);
        mLogList.setCacheColorHint(color);

        mLogEntryAdapter = new LogEntryAdapter(this, R.layout.entry, new ArrayList<LogEntry>(WINDOW_SIZE));
        setListAdapter(mLogEntryAdapter);
        reset(false);
        setKeepScreenOn();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        init();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mLogCat != null)
        {
            mLogCat.stop();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle b)
    {
        // Log.v("alogcat", "save instance");
    }

    @Override
    protected void onRestoreInstanceState(Bundle b)
    {
        // Log.v("alogcat", "restore instance");
    }

    public void reset(boolean clear)
    {
        Toast.makeText(this, R.string.reading_logs, Toast.LENGTH_SHORT).show();
        mLastLevel = Level.V;

        mPlay = true;

        if(mLogCat != null)mLogCat.start(clear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        mPlayItem = menu.add(0, MENU_PLAY, 0, R.string.pause_menu);
        mPlayItem.setIcon(android.R.drawable.ic_media_pause);
        mPlayItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        setPlayMenu();

        mSearchItem = menu.add(0, MENU_FILTER, 0, getResources().getString(R.string.filter_menu, Prefs.getFilter()));
        mSearchItem.setIcon(R.drawable.ic_action_filter);
        mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        setFilterMenu();

        mFilterItem = menu.add(0, MENU_SEARCH, 0, getResources().getString(R.string.search_menu, Prefs.getSearch()));
        mFilterItem.setIcon(android.R.drawable.ic_menu_search);
        mFilterItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        setSearchMenu();

        MenuItem clearItem = menu.add(0, MENU_CLEAR, 0, R.string.clear_menu);
        clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        clearItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem shareItem = menu.add(0, MENU_SHARE, 0, R.string.share_menu);
        shareItem.setIcon(android.R.drawable.ic_menu_share);
        shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem saveItem = menu.add(0, MENU_SAVE, 0, R.string.save_menu);
        saveItem.setIcon(android.R.drawable.ic_menu_save);
        saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem prefsItem = menu.add(0, MENU_PREFS, 0, getResources()
                .getString(R.string.prefs_menu));
        prefsItem.setIcon(android.R.drawable.ic_menu_preferences);
        prefsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return true;
    }

    public void setPlayMenu()
    {
        if (mPlayItem == null)
        {
            return;
        }
        if (mPlay)
        {
            mPlayItem.setTitle(R.string.pause_menu);
            mPlayItem.setIcon(android.R.drawable.ic_media_pause);
        }
        else
        {
            mPlayItem.setTitle(R.string.play_menu);
            mPlayItem.setIcon(android.R.drawable.ic_media_play);
        }
    }

    void setFilterMenu()
    {
        if (mFilterItem == null)
        {
            return;
        }
        int filterMenuId;
        String filter = Prefs.getFilter();
        if (filter == null || filter.length() == 0)
        {
            filterMenuId = R.string.filter_menu_empty;
        }
        else
        {
            filterMenuId = R.string.filter_menu;
        }
        mFilterItem.setTitle(getResources().getString(filterMenuId, filter));
    }

    void setSearchMenu()
    {
        if (mSearchItem == null)
        {
            return;
        }
        int searchMenuId;
        String search = Prefs.getSearch();
        if (search == null || search.length() == 0)
        {
            searchMenuId = R.string.search_menu_empty;
        }
        else
        {
            searchMenuId = R.string.search_menu;
        }
        mSearchItem.setTitle(getResources().getString(searchMenuId, search));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_FILTER:
                AlertDialog mFilterDialog = new FilterDialog(this, true);
                mFilterDialog.show();
                return true;
            case MENU_SEARCH:
                AlertDialog mSearchDialog = new FilterDialog(this, false);
                mSearchDialog.show();
                return true;
            case MENU_SHARE:
                share();
                return true;
            case MENU_SAVE:
                File f = save();
                String msg = getResources().getString(R.string.saving_log, f.toString());
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                return true;
            case MENU_PLAY:
                if (mPlay)
                {
                    pauseLog();
                }
                else
                {
                    jumpBottom();
                }
                return true;
            case MENU_CLEAR:
                reset(true);
                return true;
            case MENU_PREFS:
                Intent intent = new Intent(this, PrefsActivity.class);
                startActivityForResult(intent, PREFS_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PREFS_REQUEST:
                setKeepScreenOn();
                break;
        }
    }

    private void setKeepScreenOn()
    {
        if (Prefs.isKeepScreenOn())
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else
        {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_JUMP_TOP:
                Toast.makeText(this, "Jumping to top of log ...", Toast.LENGTH_SHORT).show();
                jumpTop();
                return true;
            case MENU_JUMP_BOTTOM:
                Toast.makeText(this, "Jumping to bottom of log ...", Toast.LENGTH_SHORT).show();
                jumpBottom();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String dump(boolean html, int maxEntries)
    {
        StringBuilder sb = new StringBuilder();
        Level lastLevel = Level.V;

        // make copy to avoid CME
        List<LogEntry> entries = new ArrayList<>( mLogEntryAdapter.getEntries());

        int offset = 0;
        for (LogEntry le : entries)
        {
            if (!html)
            {
                sb.append(le.getText());
                sb.append('\n');
            }
            else
            {
                Level level = le.getLevel();
                if (level == null)
                {
                    level = lastLevel;
                }
                else
                {
                    lastLevel = level;
                }
                sb.append("<font color=\"");
                sb.append(level.getHexColor());
                sb.append("\" face=\"sans-serif\"><b>");
                sb.append(TextUtils.htmlEncode(le.getText()));
                sb.append("</b></font><br/>\n");
            }
            if(maxEntries > 0 && offset >= maxEntries)
                break;
            offset++;
        }

        return sb.toString();
    }

    private void share()
    {
        EX.execute(new Runnable()
        {
            public void run()
            {
                boolean html = Prefs.isShareHtml();
                String content = dump(html, 1000);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                // emailIntent.setType("message/rfc822");
                if (html)
                {
                    shareIntent.setType("text/html");
                }
                else
                {
                    shareIntent.setType("text/plain");
                }

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Log: " + LOG_DATE_FORMAT.format(new Date()));
                shareIntent.putExtra(Intent.EXTRA_TEXT, html ? Html.fromHtml(content) : content);
                startActivity(Intent.createChooser(shareIntent, "Share Android Log ..."));
            }
        });

    }

    private File save()
    {
        final File path = new File(Environment.getExternalStorageDirectory(), "alogcat");
        final File file = new File(path + File.separator + "alogcat." + LOG_FILE_FORMAT.format(new Date()) + ".txt");

        // String msg = "saving log to: " + file.toString();
        // Log.d("alogcat", msg);

        EX.execute(new Runnable()
        {
            public void run()
            {
                String content = dump(false, -1);

                if (!path.exists())
                {
                    path.mkdir();
                }

                BufferedWriter bw = null;
                try
                {
                    file.createNewFile();
                    bw = new BufferedWriter(new FileWriter(file), 1024);
                    bw.write(content);
                }
                catch (IOException e)
                {
                    Log.e("alogcat", "error saving log", e);
                }
                finally
                {
                    if (bw != null)
                    {
                        try
                        {
                            bw.close();
                        }
                        catch (IOException e)
                        {
                            Log.e("alogcat", "error closing log", e);
                        }
                    }
                }
            }
        });

        return file;
    }

    private void pauseLog()
    {
        if (!mPlay)
        {
            return;
        }
        getActionBar().setSubtitle(getString(R.string.app_name_paused));
        if (mLogCat != null)
        {
            mLogCat.setPlay(false);
            mPlay = false;
        }
        setPlayMenu();
    }

    private void playLog()
    {
        if (mPlay)
        {
            return;
        }
        getActionBar().setSubtitle(null);
        if (mLogCat != null)
        {
            mLogCat.setPlay(true);
            mPlay = true;
        }
        else
        {
            reset(false);
        }
        setPlayMenu();
    }
}
