package org.ometa.lovemonster.ui.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.ui.activities.LoveListActivity;

import java.util.List;

/**
 * Extended {@link DialogFragment} which allows a user to send love to another user.
 */
public class MakeLoveDialogFragment extends DialogFragment {

    /**
     * Callback used by the cancel action to dismiss the dialog.
     */
    private static final MaterialDialog.SingleButtonCallback CANCEL_CALLBACK = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull final MaterialDialog materialDialog, @NonNull final DialogAction dialogAction) {
            materialDialog.dismiss();
        }
    };

    /**
     * The arugment name for the lovee username.
     */
    public static final String LOVEE_USERNAME_ARGUMENT_NAME = "loveeUsername";

    /**
     * Creates a new instance of this dialog fragment, setup with the specified user as the sender.
     *
     * @param user
     *      the user sending the love
     * @return
     *      the dialog fragment
     */
    public static MakeLoveDialogFragment newInstance(final User user) {
        final MakeLoveDialogFragment dialog = new MakeLoveDialogFragment();
        final Bundle arguments = new Bundle();

        arguments.putString(LOVEE_USERNAME_ARGUMENT_NAME, user.username);
        dialog.setArguments(arguments);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Log.d("foo", "create: " + getArguments().getString(LOVEE_USERNAME_ARGUMENT_NAME));
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .iconRes(R.drawable.ic_heart)
                .limitIconToDefaultSize()
                .autoDismiss(false)
                .title(R.string.fragment_make_love_dialog_title)
                .customView(R.layout.fragment_make_love_dialog, true)
                .positiveText(R.string.fragment_make_love_dialog_send)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog materialDialog, @NonNull final DialogAction dialogAction) {
                        final EditText username = (EditText) materialDialog.findViewById(R.id.fragment_make_love_dialog_lovee_username);
                        final EditText reason = (EditText) materialDialog.findViewById(R.id.fragment_make_love_dialog_reason);
                        final EditText message = (EditText) materialDialog.findViewById(R.id.fragment_make_love_dialog_message);
                        final CheckBox isPrivate = (CheckBox) materialDialog.findViewById(R.id.fragment_make_love_dialog_private);
                        final ProgressBar progressBar = (ProgressBar) materialDialog.findViewById(R.id.fragment_make_love_progress_bar);
                        final MDButton sendButton = materialDialog.getActionButton(DialogAction.POSITIVE);

                        final User lovee = new User("", username.getText().toString());
                        final Love love = new Love(reason.getText().toString(), LoveMonsterClient.getInstance().getAuthenticatedUser(), lovee);
                        love.message = message.getText().toString();
                        love.isPrivate = isPrivate.isChecked();

                        username.setEnabled(false);
                        reason.setEnabled(false);
                        message.setEnabled(false);
                        isPrivate.setEnabled(false);
                        sendButton.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);

                        LoveMonsterClient.getInstance().makeLove(love, new LoveMonsterClient.LoveResponseHandler() {
                            @Override
                            public void onSuccess(@NonNull Love love) {
                                final Toast toast = Toast.makeText(
                                        getActivity(),
                                        R.string.fragment_make_love_dialog_success,
                                        Toast.LENGTH_LONG
                                );
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                materialDialog.dismiss();
                            }

                            @Override
                            public void onFail(@NonNull List<String> errorMessages) {
                                final Toast toast = Toast.makeText(
                                        getActivity(),
                                        R.string.fragment_make_love_dialog_error_unable_to_send,
                                        Toast.LENGTH_LONG
                                );
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                progressBar.setVisibility(View.GONE);
                                username.setEnabled(true);
                                reason.setEnabled(true);
                                message.setEnabled(true);
                                isPrivate.setEnabled(true);
                                sendButton.setEnabled(true);
                            }

                            @Override
                            public void onAuthenticationFailure() {
                                final Intent intent = new Intent(getActivity(), LoveListActivity.class);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            }
                        });
                    }
                })
                .onNegative(CANCEL_CALLBACK)
                .build();

        final EditText username = (EditText) dialog.getView().findViewById(R.id.fragment_make_love_dialog_lovee_username);
        final EditText reason = (EditText) dialog.getView().findViewById(R.id.fragment_make_love_dialog_reason);

        final MDButton sendButton = dialog.getActionButton(DialogAction.POSITIVE);
        sendButton.setEnabled(false);

        final LoveValidator loveValidator = new LoveValidator(reason, username, sendButton);
        username.addTextChangedListener(loveValidator);
        reason.addTextChangedListener(loveValidator);

        username.setText(getArguments().getString(LOVEE_USERNAME_ARGUMENT_NAME));

        if (username.getText().length() > 0) {
            reason.requestFocus();
        } else {
            username.requestFocus();
        }

        return dialog;
    }

    private static class LoveValidator implements TextWatcher {

        private final EditText reasonField;
        private final EditText loveeField;
        private final MDButton sendButton;

        LoveValidator(final EditText reasonField, final EditText loveeField, final MDButton sendButton) {
            this.reasonField = reasonField;
            this.loveeField = loveeField;
            this.sendButton = sendButton;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final boolean enteredReasonAndLoveeUsername =
                    reasonField.getText().length() > 0
                            && loveeField.getText().length() > 0;
            sendButton.setEnabled(enteredReasonAndLoveeUsername);
        }
    }
}
