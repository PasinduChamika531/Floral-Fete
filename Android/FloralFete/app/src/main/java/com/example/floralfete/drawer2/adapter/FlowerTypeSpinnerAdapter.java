package com.example.floralfete.drawer2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.floralfete.R;

import java.util.List;

public class FlowerTypeSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> flowerTypes;

    public FlowerTypeSpinnerAdapter(@NonNull Context context, List<String> flowerTypes) {
        super(context, 0, flowerTypes);
        this.context = context;
        this.flowerTypes = flowerTypes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinnerText);
        textView.setText(flowerTypes.get(position));

        return convertView;
    }
}
