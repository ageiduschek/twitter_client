package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.helpers.Util;
import com.codepath.apps.twitterclient.models.User;
import com.squareup.picasso.Picasso;

/**
 *  Fragment for the header in the user profile
 */
public class ProfileHeaderFragment extends Fragment {
    private static final String USER_ID_KEY = "user_id_key";

    private long mUserId;

    public static ProfileHeaderFragment newInstance(long userId) {

        Bundle args = new Bundle();
        args.putLong(USER_ID_KEY, userId);

        ProfileHeaderFragment fragment = new ProfileHeaderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mUserId = getArguments().getLong(USER_ID_KEY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile_header, container, false);
        popupateUserProfileHeader(v);
        return v;
    }

    private void popupateUserProfileHeader(View v) {
        User user = User.lookupWithId(mUserId);
        ImageView ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);
        ImageView ivBackgroundImage = (ImageView) v.findViewById(R.id.ivBackgroundImage);
        TextView tvName = (TextView) v.findViewById(R.id.tvName);
        TextView tvScreenName = (TextView) v.findViewById(R.id.tvScreenName);
        TextView tvTagline = (TextView) v.findViewById(R.id.tvTagline);
        TextView tvNumFollowers = (TextView) v.findViewById(R.id.tvNumFollowers);
        TextView tvNumFollowing = (TextView) v.findViewById(R.id.tvNumFollowing);

        tvName.setText(user.getName());
        tvScreenName.setText("@" + user.getScreenName());
        tvTagline.setText(user.getTagLine());
        tvNumFollowers.setText("" + Util.formatNumber(user.getNumFollowers()));
        tvNumFollowing.setText("" + Util.formatNumber(user.getNumFollowing()));

        Picasso.with(getActivity()).load(user.getProfileImageUrl()).into(ivProfileImage);

        String backgroundImageUrl = user.getProfileBackgroundImageUrl();
        if (backgroundImageUrl != null) {
            Picasso.with(getActivity()).load(user.getProfileBackgroundImageUrl()).fit().centerCrop().into(ivBackgroundImage);
        }
    }
}
