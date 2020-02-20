package com.aware.ui.esms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.time.LocalTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.R;
import com.aware.providers.ESM_Provider;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cdolin on 15/04/19.
 */
public class ESM_Range extends ESM_Question {

    public static final String TAG = "AWARE:: ESM_Range";
    public static final String esm_checkboxes = "esm_checkboxes";

    public static final long hour = 3600000;

    private Calendar start = Calendar.getInstance();
    private Calendar end = Calendar.getInstance();
    private DateFormat dateFormat =  DateFormat.getTimeInstance(DateFormat.SHORT);
    private long step = hour;


    public ESM_Range() throws JSONException {
        this.setType(ESM.TYPE_ESM_RANGE);
        start.setTimeInMillis(0);
        end.setTimeInMillis(3600000 * 24);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void resetCheckboxes() throws JSONException {
        JSONArray intervals = new JSONArray();
        long temp = start.getTimeInMillis();
        while (temp < end.getTimeInMillis()) {
            String start_str = dateFormat.format(temp);
            temp += step;
            String end_str = dateFormat.format(temp);
            String interval_string = start_str + " - " + end_str;
            intervals.put(interval_string);
        }
        this.esm.put(esm_checkboxes, intervals);
    }

    public JSONArray getCheckboxes() throws JSONException {
        if (!this.esm.has(esm_checkboxes)) {
            this.resetCheckboxes();
        }
        return this.esm.getJSONArray(esm_checkboxes);
    }

    public ESM_Range setStart(long start_time) throws JSONException {
        this.start.setTimeInMillis(start_time);
        this.resetCheckboxes();
        return this;
    }

    public long getStart() throws JSONException {
        return this.start.getTimeInMillis();
    }

    public ESM_Range setEnd(long end_time) throws JSONException {
        this.end.setTimeInMillis(end_time);
        this.resetCheckboxes();
        return this;
    }

    public long getEnd() throws JSONException {
        return this.end.getTimeInMillis();
    }

    public ESM_Range setStep(long step) throws JSONException {
        this.step = step;
        this.resetCheckboxes();
        return this;
    }

    public long getStep() throws JSONException {
        return this.step;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final ArrayList<String> selected_options = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ui = inflater.inflate(R.layout.esm_range, null);
        builder.setView(ui);

        esm_dialog = builder.create();
        esm_dialog.setCanceledOnTouchOutside(false);

        try {
            TextView esm_title = (TextView) ui.findViewById(R.id.esm_title);
            esm_title.setText(getTitle());

            TextView esm_instructions = (TextView) ui.findViewById(R.id.esm_instructions);
            esm_instructions.setText(getInstructions());

            final LinearLayout checkboxes = (LinearLayout) ui.findViewById(R.id.esm_checkboxes);
            checkboxes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            final JSONArray checks = getCheckboxes();
            for (int i = 0; i < checks.length(); i++) {
                final CheckBox checked = new CheckBox(getActivity());

                checked.setText(checks.getString(i));
                checked.setOnDragListener(new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View view, DragEvent dragEvent) {
                        if (dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED ||
                                dragEvent.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                            return true;
                        } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                            checked.toggle();
                        }
                        return false;
                    }
                });
                checked.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ClipData clipData = ClipData.newPlainText(null, checked.getText());
                        View.DragShadowBuilder shadowBuilder = new EmptyDragShadowBuilder();

                        view.startDragAndDrop(clipData, shadowBuilder, null, 0);
                        return false;
                    }
                });
                checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selected_options.add(buttonView.getText().toString());
                        } else {
                            selected_options.remove(buttonView.getText().toString());
                        }
                    }
                });
                checkboxes.addView(checked);
            }
            Button cancel_checkbox = (Button) ui.findViewById(R.id.esm_cancel);
            cancel_checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    esm_dialog.cancel();
                }
            });
            Button submit_checkbox = (Button) ui.findViewById(R.id.esm_submit);
            submit_checkbox.setText(getSubmitButton());
            submit_checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);

                        ContentValues rowData = new ContentValues();
                        rowData.put(ESM_Provider.ESM_Data.ANSWER_TIMESTAMP, System.currentTimeMillis());
                        if (selected_options.size() > 0) {
                            rowData.put(ESM_Provider.ESM_Data.ANSWER,
                                    selected_options.toString()
                                            .replace("[", "").replace("]", "")
                            );
                        }
                        rowData.put(ESM_Provider.ESM_Data.STATUS, ESM.STATUS_ANSWERED);

                        getContext().getContentResolver().update(ESM_Provider.ESM_Data.CONTENT_URI, rowData, ESM_Provider.ESM_Data._ID + "=" + getID(), null);
                        selected_options.clear();

                        Intent answer = new Intent(ESM.ACTION_AWARE_ESM_ANSWERED);
                        answer.putExtra(ESM.EXTRA_ANSWER, rowData.getAsString(ESM_Provider.ESM_Data.ANSWER));
                        getActivity().sendBroadcast(answer);

                        if (Aware.DEBUG) Log.d(Aware.TAG, "Answer: " + rowData.toString());
                        esm_dialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return esm_dialog;
    }
    public class EmptyDragShadowBuilder extends View.DragShadowBuilder {

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            outShadowSize.set(1,1);
            outShadowTouchPoint.set(0,0);
        }
    }


}