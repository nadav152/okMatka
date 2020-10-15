package com.example.okmatka;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.okmatka.Interfaces.RateDialogCallBack;

public class RateDialog extends AppCompatDialogFragment {

    private EditText rateDialog_EDT_rate;
    private RateDialogCallBack rateDialogCallBack;

    public RateDialog(){

    }

    public RateDialog(RateDialogCallBack rateDialogCallBack) {
        this.rateDialogCallBack = rateDialogCallBack;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        assert getActivity() !=null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view  = inflater.inflate(R.layout.rate_dialog,null);

        builder.setView(view)
                .setTitle("Rate Dialog")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newRate = String.valueOf(rateDialog_EDT_rate.getText());
                        rateDialogCallBack.getRate(newRate);
                    }
                });

        rateDialog_EDT_rate = view.findViewById(R.id.rateDialog_EDT_rate);
        return builder.create();
    }
}
