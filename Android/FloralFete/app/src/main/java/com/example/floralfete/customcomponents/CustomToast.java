package com.example.floralfete.customcomponents;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.floralfete.R;

public class CustomToast {

    public static void successCustomToast(String message, Context context) {
        Toast t = new Toast(context);
        t.setDuration(Toast.LENGTH_SHORT);
        t.setGravity(Gravity.BOTTOM, 0, 200);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view1 = inflater.inflate(R.layout.custom_toast1, null, false);
        TextView textView = view1.findViewById(R.id.customToastTextView1);
        textView.setText(message);
        textView.setTextSize(14);
        t.setView(view1);
        t.show();
    }

    public static void errorCustomToast(String message,Context context) {
        Toast t = new Toast(context);
        t.setDuration(Toast.LENGTH_SHORT);
        t.setGravity(Gravity.BOTTOM, 0, 200);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view1 = inflater.inflate(R.layout.custom_toast2, null, false);
        TextView textView = view1.findViewById(R.id.customToastTextView2);
        textView.setText(message);
        textView.setTextSize(14);
        t.setView(view1);
        t.show();
    }

}
