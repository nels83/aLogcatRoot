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

    public FilterDialog(LogActivity logActivity)
    {
        super(logActivity);

        mLogActivity = logActivity;

        LayoutInflater factory = LayoutInflater.from(mLogActivity);
        final View view = factory.inflate(R.layout.filter_dialog, null);

        final EditText filterEdit = (EditText) view
                .findViewById(R.id.filter_edit);
        filterEdit.setText(Prefs.getFilter());

        final TextView patternErrorText = (TextView) view.findViewById(R.id.pattern_error_text);
        patternErrorText.setVisibility(View.GONE);

        final CheckBox patternCheckBox = (CheckBox) view
                .findViewById(R.id.pattern_checkbox);
        patternCheckBox.setChecked(Prefs.isFilterPattern());
        CompoundButton.OnCheckedChangeListener occl = new CompoundButton.OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked)
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
        setTitle(R.string.filter_dialog_title);

        setButton(BUTTON_POSITIVE, mLogActivity.getResources().getString(R.string.ok),
                new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FilterDialog fd = (FilterDialog) dialog;
                        String f = filterEdit.getText().toString();
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

                        Prefs.setFilter(filterEdit.getText().toString());
                        Prefs.setFilterPattern(patternCheckBox.isChecked());

                        mLogActivity.setFilterMenu();
                        dismiss();
                        mLogActivity.reset(false);
                    }
                });
        setButton(BUTTON_NEUTRAL, mLogActivity.getResources().getString(R.string.clear),
                new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FilterDialog fd = (FilterDialog) dialog;

                        Prefs.setFilter(null);
                        filterEdit.setText(null);

                        Prefs.setFilterPattern(false);
                        patternCheckBox.setChecked(false);

                        fd.mError = false;

                        mLogActivity.setFilterMenu();
                        dismiss();
                        mLogActivity.reset(false);
                    }
                });
        setButton(BUTTON_NEGATIVE, mLogActivity.getResources().getString(R.string.cancel),
                new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FilterDialog fd = (FilterDialog) dialog;

                        filterEdit.setText(Prefs.getFilter());
                        patternCheckBox.setChecked(Prefs.isFilterPattern());

                        fd.mError = false;
                        dismiss();
                    }
                });

    }
}
