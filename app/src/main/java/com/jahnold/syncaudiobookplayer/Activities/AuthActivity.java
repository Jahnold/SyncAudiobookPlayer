package com.jahnold.syncaudiobookplayer.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.jahnold.syncaudiobookplayer.Fragments.LogInFragment;
import com.jahnold.syncaudiobookplayer.R;
import com.parse.ParseUser;

public class AuthActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // check for a user
        if (ParseUser.getCurrentUser() == null) {

            // no user, show the login screen
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LogInFragment(), "LogInFragment")
                    .commit();
        }
        else {

            // already a user move on to main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
