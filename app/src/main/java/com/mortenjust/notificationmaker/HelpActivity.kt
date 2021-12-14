package com.mortenjust.notificationmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class HelpActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.help_activity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setHomeButtonEnabled(true)
      actionBar.setDisplayHomeAsUpEnabled(true)
    }
  }
}