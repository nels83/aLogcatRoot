package rs.pedjaapps.alogcatroot.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.StreamGobbler;

public class LogCat implements StreamGobbler.OnLineListener
{
    private static final long CAT_DELAY = 1;

    private Level mLevel = null;
    private String mFilter = null;
    private Pattern mFilterPattern = null;
    private boolean mIsFilterPattern;
    private Buffer mBuffer;
    Format mFormat;

    private boolean mRunning;

    /**
     * Handler responsible for refreshing UI*/
    private Handler mUiUpdateHandler;

    /**
     * Handler for reading output of logcat*/
    private Handler readHandler;

    private final ArrayList<String> mLogCache = new ArrayList<String>();
    private boolean mPlay = true;
    private long lastCat = -1;

    /**
     * Runnable which executes periodically and updates UI*/
    private Runnable catRunner = new Runnable()
    {
        @Override
        public void run()
        {
            if (!mPlay)
            {
                return;
            }
            long now = System.currentTimeMillis();
            if (now < lastCat + CAT_DELAY)
            {
                return;
            }
            lastCat = now;
            cat();
        }
    };
    private ScheduledExecutorService EX;

    /**
     * Main root shell*/
    private Shell.Interactive mShell;

    public LogCat(Handler handler)
    {
        if(handler == null)throw new IllegalArgumentException("Handler cannot be null");
        mUiUpdateHandler = handler;

        HandlerThread ht = new HandlerThread("logcat_read_handler");
        ht.start();
        readHandler = new Handler(ht.getLooper());

    }

    @Override
    public void onLine(String line)
    {
        if (!mRunning)
        {
            return;
        }
        if (line.length() == 0)
        {
            return;
        }
        if (mIsFilterPattern)
        {
            if (mFilterPattern != null && !mFilterPattern.matcher(line).find())
            {
                return;
            }
        }
        else
        {
            if (mFilter != null && !line.toLowerCase().contains(mFilter.toLowerCase()))
            {
                return;
            }
        }
        synchronized (mLogCache)
        {
            mLogCache.add(line);
        }
    }

    public void start(boolean clear)
    {
        stop();

        mRunning = true;

        mLogCache.clear();
        Message m = Message.obtain(mUiUpdateHandler, LogActivity.CLEAR_WHAT);
        mUiUpdateHandler.sendMessage(m);

        mLevel = Prefs.getLevel();
        mIsFilterPattern = Prefs.isFilterPattern();
        mFilter = Prefs.getFilter();
        mFilterPattern = Prefs.getFilterPattern();
        mFormat = Prefs.getFormat();
        mBuffer = Prefs.getBuffer();

        List<String> commands = new ArrayList<>();
        if(clear)commands.add("logcat -c");

        StringBuilder commandBuilder = new StringBuilder();

        commandBuilder.append("logcat -v ");
        commandBuilder.append(mFormat.getValue());
        commandBuilder.append(" ");
        if (mBuffer != Buffer.MAIN)
        {
            commandBuilder.append("-b ");
            commandBuilder.append(mBuffer.getValue());
            commandBuilder.append(" ");
        }
        commandBuilder.append("*:").append(mLevel);

        commands.add(commandBuilder.toString());

        //build shell
        Shell.Builder builder = new Shell.Builder();
        builder.setHandler(readHandler);
        builder.setMinimalLogging(true);
        builder.setWantSTDERR(false);
        builder.useSU();
        builder.setOnSTDOUTLineListener(this);
        builder.addCommand(commands);
        mShell = builder.open();

        EX = Executors.newScheduledThreadPool(1);
        EX.scheduleAtFixedRate(catRunner, CAT_DELAY, CAT_DELAY, TimeUnit.SECONDS);
    }

    public void stop()
    {
        mRunning = false;
        if(mShell != null) mShell.kill();
        // Log.d("alogcat", "stopping ...");

        if (EX != null && !EX.isShutdown())
        {
            EX.shutdown();
            EX = null;
        }
    }

    private void cat()
    {
        Message m;

        if (mLogCache.size() > 0)
        {
            synchronized (mLogCache)
            {
                if (mLogCache.size() > 0)
                {
                    m = Message.obtain(mUiUpdateHandler, LogActivity.CAT_WHAT);
                    m.obj = mLogCache.clone();
                    mLogCache.clear();
                    mUiUpdateHandler.sendMessage(m);
                }
            }
        }
    }

    public boolean isRunning()
    {
        return mRunning;
    }

    public boolean isPlay()
    {
        return mPlay;
    }

    public void setPlay(boolean play)
    {
        mPlay = play;
    }
}
