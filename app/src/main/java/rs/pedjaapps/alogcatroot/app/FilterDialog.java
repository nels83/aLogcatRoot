package rs.pedjaapps.alogcatroot.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterDialog extends AlertDialog
{
    private boolean mError = false;
    private LogActivity mLogActivity;

    @Override
    public void dismiss()
    {
        if (!mError)
        {
            super.dismiss();
        }
    }

    public FilterDialog(LogActivity logActivity, final boolean filter)
    {
        super(logActivity);

        mLogActivity = logActivity;

        LayoutInflater factory = LayoutInflater.from(mLogActivity);
        final View view = factory.inflate(R.layout.filter_dialog, null);

        final EditText tvInput = (EditText) view.findViewById(R.id.tvInput);
        tvInput.setText(filter ? Prefs.getFilter() : Prefs.getSearch());

        final TextView patternErrorText = (TextView) view.findViewById(R.id.pattern_error_text);
        patternErrorText.setVisibility(View.GONE);

        final CheckBox patternCheckBox = (CheckBox) view.findViewById(R.id.pattern_checkbox);
        patternCheckBox.setChecked(filter ? Prefs.isFilterPattern() : Prefs.isSearchPattern());
        CompoundButton.OnCheckedChangeListener occl = new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!isChecked)
                {
                    patternErrorText.setVisibility(View.GONE);
                    mError = false;
                }
            }

        };
        patternCheckBox.setOnCheckedChangeListener(occl);

        setView(view);
        setTitle(filter ? R.string.filter_dialog_title : R.string.search_dialog_title);

        setButton(BUTTON_POSITIVE, mLogActivity.getResources().getString(R.string.ok), new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                FilterDialog fd = (FilterDialog) dialog;
                String f = tvInput.getText().toString();
                if (patternCheckBox.isChecked())
                {
                    try
                    {
                        Pattern.compile(f);
                    }
                    catch (PatternSyntaxException e)
                    {
                        patternErrorText.setVisibility(View.VISIBLE);
                        fd.mError = true;
                        return;
                    }
                }

                fd.mError = false;
                patternErrorText.setVisibility(View.GONE);

                if (filter)
                {
                    Prefs.setFilter(tvInput.getText().toString());
                    Prefs.setFilterPattern(patternCheckBox.isChecked());
                }
                else
                {
                    Prefs.setSearch(tvInput.getText().toString());
                    Prefs.setSearchPattern(patternCheckBox.isChecked());
                }

                if(filter)mLogActivity.setFilterMenu();
                else mLogActivity.setSearchMenu();
                dismiss();
                mLogActivity.reset(false);
            }
        });
        setButton(BUTTON_NEUTRAL, mLogActivity.getResources().getString(R.string.clear), new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                FilterDialog fd = (FilterDialog) dialog;

                if(filter)Prefs.setFilter(null);
                else Prefs.setSearch(null);
                tvInput.setText(null);

                if(filter)Prefs.setFilterPattern(false);
                else Prefs.setSearchPattern(false);
                patternCheckBox.setChecked(false);

                fd.mError = false;

                if(filter)mLogActivity.setFilterMenu();
                else mLogActivity.setSearchMenu();
                dismiss();
                mLogActivity.reset(false);
            }
        });
        setButton(BUTTON_NEGATIVE, mLogActivity.getResources().getString(R.string.cancel), new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                FilterDialog fd = (FilterDialog) dialog;

                tvInput.setText(filter ? Prefs.getFilter() : Prefs.getSearch());
                patternCheckBox.setChecked(filter ? Prefs.isFilterPattern() : Prefs.isSearchPattern());

                fd.mError = false;
                dismiss();
            }
        });

    }
}
