package com.aware.ui.esms;
import java.time.LocalTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.aware.ESM;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by sjwelber on 2/20/20
 */

public class ESM_Range extends ESM_Checkbox {
    private LocalTime start = LocalTime.of(0,0);
    private LocalTime end = LocalTime.of(24,0);
    private Duration step  = Duration.of(1, ChronoUnit.HOURS);

    public ESM_Range() throws JSONException {
        this.setType(ESM.TYPE_ESM_RANGE);
    }

    public JSONArray getIntervals() throws JSONException {
        if (!this.esm.has(esm_checkboxes)) {
            JSONArray intervals = new JSONArray();
            LocalTime temp = this.getStart();
            while (temp.isBefore(this.getEnd())) {
                String interval_string = temp.toString() + " - " + temp.plus(step).toString();
                intervals.put(interval_string);
                temp = temp.plus(step);
            }
            this.esm.put(esm_checkboxes, intervals);
        }
        return this.esm.getJSONArray(esm_checkboxes);
    }

    public ESM_Range setStart(LocalTime start) throws JSONException {
        this.start = start;
        return this;
    }

    public LocalTime getStart() throws JSONException {
        return this.start;
    }

    public ESM_Range setEnd(LocalTime end) throws JSONException {
        this.end = end;
        return this;
    }

    public LocalTime getEnd() throws JSONException {
        return this.end;
    }

    public ESM_Range setStep(Duration step) throws JSONException {
        this.step = step;
        return this;
    }

    public Duration getStep() throws JSONException {
        return this.step;
    }

}
