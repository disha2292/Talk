package com.texastech.talk;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.texastech.talk.intro.IntroActivity;
import com.texastech.talk.navigation.JournalFragment;
import com.texastech.talk.navigation.ResourcesFragment;
import com.texastech.talk.navigation.SettingsFragment;
import com.texastech.talk.navigation.StatisticsFragment;
import com.texastech.talk.notification.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements JournalFragment.OnFragmentInteractionListener,
        ResourcesFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {
    /**
     * This is the core, single activity that runs throughout the lifetime of
     * the application. The rest of the UI consists of fragments embedded
     * into the UI for this activity using the Navigation component or
     * AlertDialogs and other Android components for requesting for input.
     */

    class MoodDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            /**
             * Listens for the event in which the chooses a radio button for
             * their mood then hits 'Next' to select the level of their mood.
             */
            dialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this, R.style.DarkAlertDialog);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();

            builder.setTitle("How intense is this feeling?");
            builder.setView(inflater.inflate(R.layout.dialog_mood_level, null));
            builder.setPositiveButton("Save", new MoodLevelListener());
            builder.setCancelable(false);
            builder.show();
        }
    }

    class MoodLevelListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            /**
             * Gets the level the user enters. TODO: Improve documentation.
             */
            Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /**
         * Runs when the activity is first launched. For more information about
         * the Android activity lifecycle, please refer to https://bit.ly/2q7i3eK.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPrefs.getBoolean(IntroActivity.LAUNCHED_APP_BEFORE, false)) {
            // The user is launching the app for the first time
            Intent intent = new Intent(this, IntroActivity.class);
            finish();
            startActivity(intent);
        }

        registerNotificationChannel();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        /**
         * Called whenever the app is opened when it was not open before or it
         * was paused and is being restored. In either case, it could also be the
         * result of hitting the notification so we check to see if that's the
         * case and, if so, prompt the user with an AlertDialog.
         */
        super.onResume();

        boolean resumeFromNotification = getIntent().getBooleanExtra(
                AlarmReceiver.ASK_MOOD_INTENT_PARAM, false);
        if (resumeFromNotification) {
            promptForCurrentMood();
        } else {
            // The user is just opening the app so for demo purposes, show
            // them a notification that they can click.
            // TODO: Remove.
            showNotification();
        }
    }

    void showNotification() {
        /**
         * Sets the alarm to display a notification in the notification bar asking the user to hit
         * the notification so that they get prompted to enter their mood. The notification is
         * shown 3 seconds after requested for demo purposes.
         * TODO: Remove.
         */
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3 * 1000, pendingIntent
        );
    }

    void promptForCurrentMood() {
        /**
         * Shows the user a series of AlertDialog popups to ask them what their
         * current mood is then stores the gathered information into the SQLite database.
         */
        CharSequence items[] = {
                "Happy", "Sad", "Scared", "Disgusted", "Angry", "Depressed"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkAlertDialog);
        builder.setTitle("How are you feeling?");
        builder.setSingleChoiceItems(items, 0, null);
        builder.setPositiveButton("Next", new MoodDialogListener());
        builder.setCancelable(false);
        builder.show();
    }

    void registerNotificationChannel() {
        /**
         * Registers a notification channel which is required to post notifications
         * to the user. This is done repeatedly whenever the app is started but
         * there is not problem with calling .createNotificationChannel repeatedly.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    AlarmReceiver.CHANNEL_ID, "DailyNotification", importance);
            channel.setDescription("Talk.Notifications");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    void setupBottomNavigation() {
        /**
         * Links the BottomNavigationView element to the NavController that controls
         * the NavHost containing all the top-level UI fragments. The NavController
         * is then used to switch between UIs/fragments.
         */
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        /**
         * Danger: This is required to include the fragments used by the Android
         * Navigation component (e.g. JournalFragment). It may not have any code
         * but you can leave it alone so that the app doesn't crash.
         */
    }
}