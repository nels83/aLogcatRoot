package rs.pedjaapps.alogcatroot.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class LogDumper
{

    public LogDumper()
    {
    }

    public String dump(boolean html)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        Process p = null;

        try
        {
            List<String> cmds = new ArrayList<>();
            if(Prefs.hasRootAccess())
            {
                cmds.add("su");
                cmds.add("-c");
            }
            cmds.add("logcat");
            cmds.add("-d");
            cmds.add("-v");
            cmds.add(Prefs.getFormat().getValue());
            cmds.add("-b");
            cmds.add(Prefs.getBuffer().getValue());
            cmds.add("*:" + Prefs.getLevel());
            p = Runtime.getRuntime().exec(cmds.toArray(new String[cmds.size()]));

            br = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);

            String line;
            Pattern filterPattern = Prefs.getFilterPattern();
            Format format = Prefs.getFormat();

            Level lastLevel = Level.V;

            while ((line = br.readLine()) != null)
            {
                if (filterPattern != null
                        && !filterPattern.matcher(line).find())
                {
                    continue;
                }

                if (!html)
                {
                    sb.append(line);
                    sb.append('\n');
                }
                else
                {
                    Level level = format.getLevel(line);
                    if (level == null)
                    {
                        level = lastLevel;
                    }
                    else
                    {
                        lastLevel = level;
                    }
                    sb.append("<font color=\"").append(level.getHexColor()).append("\" face=\"sans-serif\"><b>");
                    sb.append(TextUtils.htmlEncode(line));
                    sb.append("</b></font><br/>\n");

                }
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            Log.e("alogcat", "error reading log", e);
            return null;
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    Log.e("alogcat", "error closing stream", e);
                }
            }
            if (p != null)
            {
                p.destroy();
            }
        }
    }
}
