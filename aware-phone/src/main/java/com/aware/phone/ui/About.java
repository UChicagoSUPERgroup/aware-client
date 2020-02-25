package com.aware.phone.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.aware.phone.R;

public class About extends Aware_Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aware_about);

        TextView about_team = (TextView) findViewById(R.id.about_team);
        about_team.setMovementMethod(LinkMovementMethod.getInstance());

        TextView about_super = (TextView) findViewById(R.id.about_super);
        about_super.setMovementMethod(LinkMovementMethod.getInstance());

        /*
        WebView about_us = (WebView) findViewById(R.id.about_us);
        WebSettings settings = about_us.getSettings();
        settings.setJavaScriptEnabled(true);
        about_us.loadUrl("https://awareframework.com/team/");

        */
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
