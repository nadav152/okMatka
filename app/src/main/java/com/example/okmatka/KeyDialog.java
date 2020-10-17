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

import com.example.okmatka.Interfaces.KeyDialogCallBack;

public class KeyDialog extends AppCompatDialogFragment {

    private EditText keyDialog_EDT_key;
    private KeyDialogCallBack keyDialogCallBack;

    public KeyDialog(){

    }

    public KeyDialog(KeyDialogCallBack keyDialogCallBack) {
        this.keyDialogCallBack = keyDialogCallBack;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        assert getActivity() !=null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view  = inflater.inflate(R.layout.key_dialog,null);

        builder.setView(view)
                .setTitle("Key Dialog")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = String.valueOf(keyDialog_EDT_key.getText());
                        keyDialogCallBack.getKey(key);
                    }
                });

        keyDialog_EDT_key = view.findViewById(R.id.keyDialog_EDT_key);
        return builder.create();
    }
}
