package com.aware.ui.esms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.ESM;
import com.aware.R;
import com.aware.providers.ESM_Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cdolin on 22/05/19.
 */
public class ESM_Grid extends ESM_Question {

    public static final String esm_columns = "esm_columns";
    public static final String esm_rows = "esm_rows";

    public ESM_Grid() throws JSONException {
        this.setType(ESM.TYPE_ESM_GRID);
    }

    public JSONArray getColumns() throws JSONException {
        if (!this.esm.has(esm_columns)) {
            this.esm.put(esm_columns, new JSONArray());
        }
        return this.esm.getJSONArray(esm_columns);
    }

    public JSONArray getRows() throws JSONException {
        if (!this.esm.has(esm_rows)) {
            this.esm.put(esm_rows, new JSONArray());
        }
        return this.esm.getJSONArray(esm_rows);
    }

    public ESM_Grid setColumns(JSONArray columns) throws JSONException {
        this.esm.put(esm_columns, columns);
        return this;
    }

    public ESM_Grid setRows(JSONArray rows) throws JSONException {
        this.esm.put(esm_rows, rows);
        return this;
    }

    public ESM_Grid addColumn(String option) throws JSONException {
        JSONArray columns = getColumns();
        columns.put(option);
        this.setColumns(columns);
        return this;
    }

    public ESM_Grid addRow(String option) throws JSONException {
        JSONArray rows = getRows();
        rows.put(option);
        this.setRows(rows);
        return this;
    }

    public List<String> arrayToString(JSONArray array) {
        ArrayList<String> list = new ArrayList<>();
        try {
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View ui = inflater.inflate(R.layout.esm_grid, null);
        builder.setView(ui);

        esm_dialog = builder.create();
        esm_dialog.setCanceledOnTouchOutside(false);

        try {
            TextView esm_title = (TextView) ui.findViewById(R.id.esm_title);
            esm_title.setText(getTitle());

            LinearLayout gridView = (LinearLayout) ui.findViewById(R.id.esm_question_grid);
            RadioGridArrayAdapter adapter =
                    new RadioGridArrayAdapter(getContext(), R.layout.esm_grid_row,
                            arrayToString(getRows()), getColumns());

            LinearLayout labelView = (LinearLayout) ui.findViewById(R.id.esm_labels);
            for (int i = 0; i < getColumns().length(); i++) {
                TextView textView = new TextView(getContext());
                textView.setText(getColumns().getString(i));
                LinearLayout.LayoutParams params =
                        (LinearLayout.LayoutParams) textView.getLayoutParams();
                if (params == null) {
                    params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f);
                }
                params.weight = 1f;
                params.leftMargin = 8;
                params.rightMargin = 8;
                textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER);
                labelView.addView(textView);
            }

            for (int i = 0; i < getRows().length(); i++) {
                View view = adapter.getView(i, null, gridView);
                gridView.addView(view);
            }

            final LinearLayout grid = (LinearLayout) ui.findViewById(R.id.esm_question_grid);
            grid.setOnClickListener(new View.OnClickListener() {
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

            Button cancel_radio = (Button) ui.findViewById(R.id.esm_cancel);
            cancel_radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    esm_dialog.cancel();
                }
            });
            Button submit_radio = (Button) ui.findViewById(R.id.esm_submit);
            submit_radio.setText(getSubmitButton());
            submit_radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (getExpirationThreshold() > 0 && expire_monitor != null)
                            expire_monitor.cancel(true);

                        ContentValues rowData = new ContentValues();
                        rowData.put(ESM_Provider.ESM_Data.ANSWER_TIMESTAMP, System.currentTimeMillis());

                        LinearLayout gridView = (LinearLayout) ui.findViewById(R.id.esm_question_grid);
                        JSONObject responses = new JSONObject();
                        JSONArray rows = getRows();
                        JSONArray columns = getColumns();
                        for (int i = 0; i < gridView.getChildCount(); i++) {
                            LinearLayout gridRow = (LinearLayout) gridView.getChildAt(i);
                            RadioGroup radioOptions = gridRow.findViewById(R.id.esm_radio_row);
                            if (radioOptions.getCheckedRadioButtonId() != -1) {
                                responses.put(rows.getString(i),
                                        columns.getString(radioOptions.getCheckedRadioButtonId()));
                            }
                        }

                        rowData.put(ESM_Provider.ESM_Data.ANSWER, String.valueOf(responses));
                        rowData.put(ESM_Provider.ESM_Data.STATUS, ESM.STATUS_ANSWERED);

                        getContext().getContentResolver().update(ESM_Provider.ESM_Data.CONTENT_URI, rowData, ESM_Provider.ESM_Data._ID + "=" + getID(), null);

                        Intent answer = new Intent(ESM.ACTION_AWARE_ESM_ANSWERED);
                        answer.putExtra(ESM.EXTRA_ANSWER, rowData.getAsString(ESM_Provider.ESM_Data.ANSWER));
                        getActivity().sendBroadcast(answer);

                        if (Aware.DEBUG) Log.d(Aware.TAG, "Answer:" + rowData.toString());

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

    public class RadioGridArrayAdapter extends ArrayAdapter<String> {
        private int mResource;
        private JSONArray mColumns;

        public RadioGridArrayAdapter(Context context, int resource, List<String> rows, JSONArray columns) {
            super(context, resource, rows);
            this.mResource = resource;
            this.mColumns = columns;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater sInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = convertView;
            if (rowView == null) {
                rowView = sInflater.inflate(mResource, parent, false);
            }
            String time = getItem(position);
            if (time != null) {
                TextView row_label = rowView.findViewById(R.id.row_label);
                row_label.setText(time);
                RadioGroup radioOptions = (RadioGroup) rowView.findViewById(R.id.esm_radio_row);

                for (int i = 0; i < mColumns.length(); i++) {
                    RadioButton radioOption = new RadioButton(getActivity());
                    radioOption.setId(i);
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) radioOption.getLayoutParams();
                    if (params == null) {
                        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1.0f);
                    }
                    params.weight = 1f;
                    params.gravity = Gravity.CENTER;
                    radioOption.setLayoutParams(params);
                    radioOptions.addView(radioOption);
                }

            }

            return rowView;
        }
    }

}
