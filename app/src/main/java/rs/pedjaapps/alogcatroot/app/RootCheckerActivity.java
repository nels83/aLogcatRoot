package rs.pedjaapps.alogcatroot.app;

import android.app.Activity;
import android.content.*;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

public class RootCheckerActivity extends Activity
{

    Handler handler;
    TextView tvLoading;
    int dotCount = 0;
    boolean canContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if(ALogcatApplication.isRotoCheckedThisSession())
        {
            startActivity(new Intent(this, LogActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_root_checker);
        tvLoading = (TextView)findViewById(R.id.tvLoading);

        handler = new Handler();
        new ATCheckRoot().execute();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(handler != null)handler.postDelayed(loaderRunnable, 1000);
        if(canContinue)
        {
            startActivity(new Intent(this, LogActivity.class));
            finish();
        }
        else
        {
            canContinue = true;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(handler != null)handler.removeCallbacks(loaderRunnable);
        canContinue = false;
    }

    private  Runnable loaderRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            StringBuilder builder = new StringBuilder(getString(R.string.checking_root));
            for(int i = 0; i < dotCount; i++)
            {
                builder.append(".");
            }
            tvLoading.setText(builder.toString());
            dotCount++;
            if(dotCount > 3)dotCount = 0;
            handler.postDelayed(loaderRunnable, 1000);
        }
    };

    private class ATCheckRoot extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            Prefs.setHasRootAccess(RootTools.isAccessGiven());
            ALogcatApplication.setRotoCheckedThisSession(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if(canContinue)
            {
                startActivity(new Intent(RootCheckerActivity.this, LogActivity.class));
                finish();
            }
            else
            {
                canContinue = true;
            }
        }
    }
}
