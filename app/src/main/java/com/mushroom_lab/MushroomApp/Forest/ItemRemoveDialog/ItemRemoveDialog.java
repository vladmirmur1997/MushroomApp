package com.mushroom_lab.MushroomApp.Forest.ItemRemoveDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.mushroom_lab.MushroomApp.Forest.ItemRemoveDialog.ItemRemoveInterface.ItemRemoveInterface;

public class ItemRemoveDialog extends DialogFragment {
    private ItemRemoveInterface removable;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        removable = (ItemRemoveInterface) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int num = getArguments().getInt("orient");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle("Удаленние ориентира")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Вы хотите удалить метку?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removable.removeItem(num);
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
    }
}
