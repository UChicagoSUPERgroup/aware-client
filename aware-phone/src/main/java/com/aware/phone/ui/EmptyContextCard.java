package com.aware.phone.ui;


import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.aware.phone.R;
import com.aware.utils.IContextCard;


public class EmptyContextCard implements IContextCard {

    private View card;

    //Constructor used to instantiate this card
    public EmptyContextCard() {

    }

    private TextView hello = null;

    @Override
    public View getContextCard(Context context) {
        //Load card layout
        LayoutInflater sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        card = sInflater.inflate(R.layout.layout_empty, null);

        //Return the card to AWARE/apps
        return card;
    }

}