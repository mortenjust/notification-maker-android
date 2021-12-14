package com.mortenjust.notificationmaker;


import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import com.google.android.material.snackbar.Snackbar;
import com.mortenjust.notificationmaker.models.NotificationDataPreferences;
import com.mortenjust.notificationmaker.models.NotificationRepo;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String FRAGMENT_ARGUMENT_LOAD_NOTIFICATION = "FRAGMENT_ARGUMENT_LOAD_NOTIFICATION";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final String TAG = "mj.SettingsActivity";
    public static final String PREFS_HINT_SEEN = "PREFS_HINT_SEEN";

    private NotificationRepo repo;

//    @Override
//    public void onHeaderClick(Header header, int position) {
//        Log.d(TAG, "on the headerz clickz");
//        super.onHeaderClick(header, position);
//
//        if (header.id == R.id.send_notification) {
//            sendNotification();
//        }
//    }
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String TAG = "onpreflistener";
            Log.d(TAG, "mj.bind to value listener");
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private final OnSharedPreferenceChangeListener
        onNotificationSavedListener = (a, b) -> invalidateHeaders();;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repo = new NotificationRepo(this);
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().addOnBackStackChangedListener(this::invalidateHeaders);
        repo.addNotificationSavedListener(onNotificationSavedListener);

        showHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.removeNotificationSavedListener(onNotificationSavedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateHeaders();
    }

    private void showHint() {
        SharedPreferences prefs = getSharedPreferences(PREFS_HINT_SEEN, MODE_PRIVATE);
        if (!prefs.getBoolean(PREFS_HINT_SEEN, false)) {
            getListView().postDelayed(() -> {
                Snackbar snackbar = Snackbar
                    .make(getListView(), R.string.wearos_warning_intro_snackbar,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(
                    R.string.show_me,
                    v -> {
                        prefs.edit().putBoolean(PREFS_HINT_SEEN, true).apply();
                        startActivity(new Intent(this, HelpActivity.class));
                    });

                snackbar.show();
            }, 1000);
        }
    }

    /**
     * Set up the {@link ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);

        String[] savedNotifications = repo.loadNotificationNames();
        for (String notifName : savedNotifications) {
            Header header = new Header();
            header.title = notifName;
            header.fragment = target.get(0).fragment;
            Bundle fragmentArguments = new Bundle();
            fragmentArguments.putString(FRAGMENT_ARGUMENT_LOAD_NOTIFICATION, notifName);
            header.fragmentArguments = fragmentArguments;
            target.add(header);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AppearancePreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppearancePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_appearance);
            setHasOptionsMenu(true);

            Log.d(getTag(), "mj.we are in appearance fragment ");

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {

        private NotificationRepo repo;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            repo = new NotificationRepo(getContext());

            Log.d(getTag(), "mj.we are in the content fragment ");

            Bundle arguments = getArguments();
            if (arguments != null) {
                String savedPreferenceName = arguments.getString(FRAGMENT_ARGUMENT_LOAD_NOTIFICATION);
                repo.loadNotification(savedPreferenceName);
            }

            addPreferencesFromResource(R.xml.pref_content);
            setHasOptionsMenu(true);

            refreshSummaries();


            Preference buttonPref = findPreference("sendNotificationButton");
            buttonPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d("mj.", "we just got the click!");

                    NotificationAssembler assembler = new NotificationAssembler(getContext(), new NotificationDataPreferences(getContext()));
                    assembler.postNotification();

                    return false;
                }
            });

            Preference dismissAllButton = findPreference("dismiss_all");
            dismissAllButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Clear all notification
                    NotificationManager nMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    nMgr.cancelAll();
                return false;
                }
            });




            // Bind th  e summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        private void refreshSummaries() {
            bindPreferenceSummaryToValue(findPreference("content_title"));
            bindPreferenceSummaryToValue(findPreference("content_text"));
            bindPreferenceSummaryToValue(findPreference("category"));
            bindPreferenceSummaryToValue(findPreference("group"));
            bindPreferenceSummaryToValue(findPreference("content_info"));
            bindPreferenceSummaryToValue(findPreference("use_style"));
            bindPreferenceSummaryToValue(findPreference("large_icon"));
            bindPreferenceSummaryToValue(findPreference("priority"));
//            bindHashSetPreferenceSummaryToValue(findPreference("actions")); // meh, crashes
            bindPreferenceSummaryToValue(findPreference("visibility"));
            bindPreferenceSummaryToValue(findPreference("notification_id_name"));
            bindPreferenceSummaryToValue(findPreference("person"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            } else if (id == R.id.save) {
                String title = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString("content_title", "Saved notification");
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), "Can't save: Please add a title first", Toast.LENGTH_LONG).show();
                } else {
                    repo.saveNotification(title);
                    Toast.makeText(getContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                }
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.notif_menu, menu);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

//            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
//            // to their values. When their values change, their summaries are
//            // updated to reflect the new value, per the Android Design
//            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
            Log.d(getTag(), "mj.we are here, so we can do stuff");
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
