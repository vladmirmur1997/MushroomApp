package com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog.RemoveInterface.RemoveInterface;

public class WalkRemoveDialog extends DialogFragment {
    private RemoveInterface removable;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        removable = (RemoveInterface) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //String forest = getArguments().getString("Forest");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle("Удаленние леса")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Вы хотите удалить последний поход?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removable.remove();
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
    }
}
