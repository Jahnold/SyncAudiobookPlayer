package com.jahnold.syncaudiobookplayer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jahnold.syncaudiobookplayer.Activities.MainActivity;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


/**
 *  Log In Fragment
 */
public class LogInFragment extends android.support.v4.app.Fragment {

    TextView mTxtFailed;

    // empty constructor
    public LogInFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate layout
        View v = inflater.inflate(R.layout.fragment_login,container,false);

        // get refs
        final EditText email = (EditText) v.findViewById(R.id.et_email);
        final EditText password = (EditText) v.findViewById(R.id.et_password);
        final Button login = (Button) v.findViewById(R.id.btn_login);
        mTxtFailed = (TextView) v.findViewById(R.id.txt_failed);
        Button register = (Button) v.findViewById(R.id.btn_register);

        // set button listeners
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login(
                        email.getText().toString(),
                        password.getText().toString()
                );

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // switch to register fragment
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new RegisterFragment(), "RegisterFragment")
                        .commit();

            }
        });

        return v;

    }

    private void login(String email, String password) {

        ParseUser.logInInBackground(
                email,
                password,
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {

                        if (user != null) {

                            // log in success, switch main activity
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);

                        }
                        else {

                            // login failed, make the failed text visible
                            mTxtFailed.setVisibility(View.VISIBLE);

                        }
                    }
                }
        );

    }
}
