package com.codepath.apps.twitterclient.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.helpers.TwitterModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnComposeTweetActionListener} interface
 * to handle interaction events.
 * Use the {@link ComposeTweetDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComposeTweetDialog extends DialogFragment {
    private TwitterModel mTwitterModel;
    private OnComposeTweetActionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment ComposeTweetDialog.
     */
    public static ComposeTweetDialog newInstance() {
        return new ComposeTweetDialog();
    }

    public ComposeTweetDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_tweet_dialog, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(R.string.compose_title);
        final EditText textField = (EditText) view.findViewById(R.id.etComposeTweet);
        Button submitButton = (Button) view.findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTweetSubmit(textField.getText().toString());
            }
        });

        // Show soft keyboard automatically and request focus to field
        textField.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }




    public void onTweetSubmit(String tweet) {
        mTwitterModel.postTweet(tweet, new TwitterModel.OnPostFinishDelegate() {
            @Override
            public void onQueryComplete(boolean networkSuccess) {
                if (mListener != null) {
                    mListener.onTweetSave(networkSuccess);
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnComposeTweetActionListener) activity;
            mTwitterModel = new TwitterModel(activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mTwitterModel = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnComposeTweetActionListener {
        void onTweetSave(boolean success);
    }

}
