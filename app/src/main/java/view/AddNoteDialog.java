package view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.scancreateprofessor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddNoteDialog extends DialogFragment {
    public interface AddNoteDialogListener {
        void onAddNoteDialogPositiveClick(String fileTitle);
    }

    private TextInputLayout textInputLayout;
    private TextInputEditText editText;
    private Button positiveButton;
    private AddNoteDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddNoteDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement AddNoteDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_note_dialog, null);
        editText = view.findViewById(R.id.title_text_field_add_new_note_dialog);
        textInputLayout = view.findViewById(R.id.title_text_field_layout_add_new_note_dialog);

        String dialogTitle=getResources().getString(R.string.add_note_dialog_title);
        String diaPositiveButtonText=getResources().getString(R.string.add_note_dialog_positive_button);
        String dialogNegativeButtonText=getResources().getString(R.string.add_note_dialog_negative_button);
        builder.setView(view)
                .setTitle(dialogTitle)
                .setPositiveButton(diaPositiveButtonText, (dialog, which) -> {
                    String fileTitle = (editText.getText() != null ? editText.getText().toString() : "");
                    listener.onAddNoteDialogPositiveClick(fileTitle);
                })
                .setNegativeButton(dialogNegativeButtonText, (dialog, which) -> dialog.dismiss());


        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return dialog;
    }

    private void validateInput(String input) {
        String diaInputErrorEmpty=getResources().getString(R.string.add_note_input_error_empty);
        String diaInputErrorCharacters=getResources().getString(R.string.add_note_input_error_characters);
        if (input.isEmpty()) {
            textInputLayout.setError(diaInputErrorEmpty);
            positiveButton.setEnabled(false);
        } else if (input.matches(".*[^a-zA-Z0-9_].*")) {
            textInputLayout.setError(diaInputErrorCharacters);
            positiveButton.setEnabled(false);
        } else {
            textInputLayout.setError(null);
            positiveButton.setEnabled(true);
        }
    }

}
