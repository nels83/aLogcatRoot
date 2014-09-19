package rs.pedjaapps.alogcatroot.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

public class ALogcatApplication extends Application
{
    private static final boolean DEBUG = false;
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();
        if (DEBUG)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog().penaltyDeath().build());
        }
    }

    public static Context getContext()
    {
        return context;
    }
}
