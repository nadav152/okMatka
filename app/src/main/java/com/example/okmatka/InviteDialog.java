package com.example.okmatka;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.okmatka.Interfaces.InviteDialogCallback;

public class InviteDialog extends AppCompatDialogFragment {

    private InviteDialogCallback inviteDialogCallback;

    public InviteDialog(){

    }

    public InviteDialog(InviteDialogCallback inviteDialogCallback) {
        this.inviteDialogCallback = inviteDialogCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        assert getActivity() !=null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view  = inflater.inflate(R.layout.map_dialog,null);

        builder.setView(view)
                .setTitle("Map Invitation")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inviteDialogCallback.getAnswer(false);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inviteDialogCallback.getAnswer(true);
                    }
                });
        return builder.create();
    }
}


