package com.mushroom_lab.MushroomApp.Forest.ForestRemoveDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import com.mushroom_lab.MushroomApp.Forest.ForestRemoveDialog.RemoveInterface.RemoveInterface;
public class ForestRemoveDialog extends DialogFragment {
    private RemoveInterface removable;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        removable = (RemoveInterface) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String forest = getArguments().getString("Forest");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle("Удаленние леса")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Вы хотите удалить " + forest + "?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removable.remove(forest);
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
    }
}
