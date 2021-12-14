package com.mortenjust.notificationmaker.models

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

interface NotificationData {
  // Ints are saved as Strings
  fun getPrefInt(key: String?): Int
  fun getPrefString(key: String?): String
  fun getPrefBool(key: String?): Boolean
  fun getPrefStringSet(actions: String): Set<String?>?
}

class NotificationDataPreferences(context: Context) : NotificationData {
  private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  // Ints are saved as Strings
  override fun getPrefInt(key: String?): Int {
    val s: String? = prefs.getString(key, "0")
    return s!!.toInt()
  }

  override fun getPrefString(key: String?): String {
    return prefs.getString(key, "")!!
  }

  override fun getPrefBool(key: String?): Boolean {
    return prefs.getBoolean(key, false)
  }

  override fun getPrefStringSet(actions: String): Set<String?>? {
    return prefs.getStringSet(actions, null)
  }
}