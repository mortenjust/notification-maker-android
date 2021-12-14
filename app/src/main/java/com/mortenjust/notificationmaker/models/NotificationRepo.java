package com.mortenjust.notificationmaker.models;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.w3c.dom.Text;

public class NotificationRepo {
  private static final String TAG = "NotificationRepo";

  private static final String PREFS_NAME = "PREFS_NAME";
  private static final String PREFS_LIST_NAME = "PREFS_LIST_NAME";
  private static final String PREFS_NAMES_LIST = "PREFS_NAMES_LIST";
  private static final String NAMES_LIST_DELIMITER = "~";


  private final Context context;

  public NotificationRepo(Context context) {
    this.context = context;
  }

  /**
   * Saves the current notification preferences to disk so we can reload them later.
   *
   * @param name unique name of the preference to show in the load menu
   */
  public void saveNotification(String name) {
    SharedPreferences prefToSave = context.getSharedPreferences(PREFS_NAME + name, MODE_PRIVATE);
    Log.d(TAG, "saveNotification " + name + ": " + prefToSave);
    Editor editor = prefToSave.edit();
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    Set<? extends Entry<String, ?>> entries = prefs.getAll().entrySet();
    for (Entry<String, ?> entry : entries) {
      Object value = entry.getValue();
      String type = value.getClass().getSimpleName();
      String key = entry.getKey();
      Log.d(TAG, "Saving field " + key + " = (" + type + ") " + value);

      switch(type) {
        case "String":
          editor.putString(key, (String) value);
          break;
        case "Boolean":
          editor.putBoolean(key, (Boolean) value);
          break;
        case "HashSet":
          editor.putStringSet(key, (Set<String>) value);
          break;
        default:
          Log.e(TAG, "Unrecognized preference type " + type);
      }
    }
    editor.apply();

    List<String> allNames = new ArrayList<>(Arrays.asList(loadNotificationNames()));
    if (!allNames.contains(name)) {
      SharedPreferences listPrefs = context.getSharedPreferences(PREFS_LIST_NAME, MODE_PRIVATE);
      allNames.add(name);
      String csv = TextUtils.join(NAMES_LIST_DELIMITER, allNames);
      listPrefs.edit().putString(PREFS_NAMES_LIST, csv).apply();
    }
  }

  /**
   * Loads the given preferences from disk into the default shared preferences.
   *
   * @param name unique name of the preference to show in the load menu
   */
  public void loadNotification(String name) {
    SharedPreferences prefsToSave = PreferenceManager
        .getDefaultSharedPreferences(context);
    Editor editor = prefsToSave.edit().clear();
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + name, MODE_PRIVATE);
    Set<? extends Entry<String, ?>> entries = prefs.getAll().entrySet();
    for (Entry<String, ?> entry : entries) {
      Object value = entry.getValue();
      String type = value.getClass().getSimpleName();
      String key = entry.getKey();
      Log.d(TAG, "Saving field " + key + " = (" + type + ") " + value);

      switch(type) {
        case "String":
          editor.putString(key, (String) value);
          break;
        case "Boolean":
          editor.putBoolean(key, (Boolean) value);
          break;
        case "HashSet":
          editor.putStringSet(key, (Set<String>) value);
          break;
        default:
          Log.e(TAG, "Unrecognized preference type " + type);
      }
    }
    editor.commit();
  }

  public String[] loadNotificationNames() {
    SharedPreferences listPrefs = context.getSharedPreferences(PREFS_LIST_NAME, MODE_PRIVATE);
    String csv = listPrefs.getString(PREFS_NAMES_LIST, null);
    if (TextUtils.isEmpty(csv)) {
      return new String[0];
    }
    return csv.split(NAMES_LIST_DELIMITER);
  }

  public void addNotificationSavedListener(OnSharedPreferenceChangeListener listener) {
    SharedPreferences listPrefs = context.getSharedPreferences(PREFS_LIST_NAME, MODE_PRIVATE);
    listPrefs.registerOnSharedPreferenceChangeListener(listener);
  }

  public void removeNotificationSavedListener(OnSharedPreferenceChangeListener listener) {
    SharedPreferences listPrefs = context.getSharedPreferences(PREFS_LIST_NAME, MODE_PRIVATE);
    listPrefs.unregisterOnSharedPreferenceChangeListener(listener);
  }
}
