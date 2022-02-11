package com.mortenjust.notificationmaker;

//import android.app.Notification;
//import android.app.NotificationManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.mortenjust.notificationmaker.models.NotificationData;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by mortenjust on 3/9/16.
 */
public class NotificationAssembler {

  private final NotificationData model;
  Notification.Builder builder;
  Notification.WearableExtender wearableExtender;

  Notification notification;
  Context context;

  public NotificationAssembler(Context context, NotificationData model) {
    this.context = context;
    this.model = model;
  }

  // Ints are saved as Strings
  Integer getPrefInt(String key) {
    return model.getPrefInt(key);
  }

  String getPrefString(String key) {
    return model.getPrefString(key);
  }

  Boolean getPrefBool(String key) {
    return model.getPrefBool(key);
  }

  private Set<String> getPrefStringSet(String actions) {
    return model.getPrefStringSet(actions);
  }

  void postNotification() {
    String channelId = createChannel();

    Person person = null;

    String personName = getPrefString("person");
    if (!TextUtils.isEmpty(personName)) {
      person = new Person.Builder()
          .setName(personName)
          .setKey(personName)
          .build();
    }

    //noinspection ResourceType
    builder = new Notification.Builder(context, channelId)
        .setContentTitle(getPrefString("content_title"))
        .setContentText(getPrefString("content_text"))
        .setContentInfo(getPrefString("content_info"))
        .setCategory(getPrefString("category"))
        .setGroup(getPrefString("group"))
        .setGroupSummary(getPrefBool("group_summary"))
        //.setPriority(getPrefInt("priority"))
        .setVisibility(getPrefInt("visibility"))
        .setOnlyAlertOnce(getPrefBool("only_alert_once"))
        .setOngoing(getPrefBool("ongoing"))
        .setUsesChronometer(getPrefBool("uses_chronometer"))
        .addPerson(person)
        .setAutoCancel(getPrefBool("auto_cancel"))
        .setChannelId(channelId)
    ;

    if (getPrefBool("messaging") && person != null) {
      Person sender = new Person.Builder()
          .setName("Darth")
          .setKey("Darth")
          .build();
      builder
          .setStyle(new Notification.MessagingStyle(person)
              .addMessage("No! No! No!",
                  System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30), person)
              .addMessage("Search your feelings. You know it to be true.",
                  System.currentTimeMillis(), sender));
    }

    if (getPrefBool("progress_determinate") || getPrefBool("progress_indeterminate")) {
      builder.setProgress(100, 60, getPrefBool("progress_indeterminate"));
    }

    Log.d("mj.", "use_style is " + getPrefString("use_style"));
    if (getPrefString("use_style") != "use_media_style") {
      Log.d("mj.", "Adding regular");
      addActionsFromPref();
      setRemoteInput();
    }
    setVibrationFromPref();
    setLargeIconFromPref();
    setSmallIconAndColorFromPref();

    // wearable extender
    if (getPrefBool("enable_wearable_extender")) {
      wearableExtender = new Notification.WearableExtender();
      addWearableActionsFromPref();
      setLongScreenTimeout();
      setScrolledToBottom();
      setWearableBackground();
      addWearablePages();
      builder.extend(wearableExtender);
    }

    setStyleFromPref(); // must be last because they want to override stuff to not look weird
    Integer notificationId = getNotificationId();

    NotificationManager manager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(notificationId, builder.build());
  }

  private String createChannel() {
    NotificationManager manager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);

    int importance = getPrefInt("priority");
    String group = null; //"wrong use of group": getPrefString("group");
    boolean groupSummary = getPrefBool("group_summary");
    boolean vibrate = getPrefBool("vibrate");

    String id = "test_" + importance + "_" + "_" + vibrate;
    // The user-visible name of the channel.
    CharSequence name = "Test channel " + id;
    // The user-visible description of the channel.
    String description = "Test channel description";
    NotificationChannel mChannel = new NotificationChannel(id, name, importance);
    // Configure the notification channel.
    mChannel.setDescription(description);
    mChannel.enableLights(true);
    // Sets the notification light color for notifications posted to this
    // channel, if the device supports this feature.
    mChannel.setLightColor(Color.RED);

    mChannel.enableVibration(vibrate);
    if (vibrate) {
      mChannel.setVibrationPattern(new long[]{100, 500});
    }
    manager.createNotificationChannel(mChannel);

    if (!TextUtils.isEmpty(group)) {
      mChannel.setGroup(group);
      manager.createNotificationChannelGroup(new NotificationChannelGroup(group, group));
    }

    return id;
  }

  void setSmallIconAndColorFromPref() {

    Icon i;
    String name = getPrefString("notification_id_name");

    Integer r;
    int c;
    switch (name) {
      case "default":
        r = R.drawable.ic_small_icon;
        c = context.getResources().getColor(R.color.colorPrimary, null);
        break;
      case "twitter":
        r = R.drawable.ic_twitter;
        c = Color.rgb(29, 161, 242);
        break;
      case "fit":
        r = R.drawable.ic_fit;
        c = Color.rgb(221, 77, 66);
        break;
      case "weather":
        r = R.drawable.ic_weather;
        c = Color.rgb(255, 203, 47);
        break;
      case "maps":
        r = R.drawable.map_white_48dp;
        c = Color.rgb(31, 163, 99);
        break;
      case "calendar":
        r = R.drawable.event_available_white_48dp;
        c = Color.rgb(70, 135, 244);
        break;
      case "email":
        r = R.drawable.gmail_white_48dp;
        c = Color.rgb(220, 74, 62);
        break;
      case "chat":
        r = R.drawable.ic_chat;
        c = Color.rgb(220, 74, 62);
        break;
      case "nytimes":
        r = R.drawable.ic_nytimes;
        c = Color.rgb(0, 0, 0);
        break;
      default:
        r = R.drawable.ic_small_icon;
        c = context.getResources().getColor(R.color.colorPrimary, null);
    }

    i = Icon.createWithResource(context, r);
    builder.setSmallIcon(i);
    builder.setColor(c);
  }

  @NonNull
  private Integer getNotificationId() {
    Integer notificationId;
    Boolean updateNotification = getPrefBool("update_notification");
    if (updateNotification) {
      notificationId = 1337;
    } else {
      Random r = new Random();
      notificationId = r.nextInt(63000 - 100) + 63000;
    }
    return notificationId;
  }

  private void setLongScreenTimeout() {
    if (getPrefBool("long_screen_timeout")) {
      wearableExtender.setHintScreenTimeout(Notification.WearableExtender.SCREEN_TIMEOUT_LONG);
    }
  }

  private void setScrolledToBottom() {
    if (getPrefBool("start_scrolled_to_bottom")) {
      wearableExtender.setStartScrollBottom(true);
    }
  }

  private void setRemoteInput() {
    if (getPrefBool("use_remote_input")) {
      CharSequence[] options = {"Yes", "No", "Call me maybe"};

      if (getPrefBool("use_remote_input")) {
        RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
            .setLabel("Reply now")
            .setChoices(options)
            .build();

        Icon icon = Icon.createWithResource(context, R.drawable.ic_reply_white_36dp);

        Notification.Action a =
            new Notification.Action.Builder(icon,
                "Reply here", getPendingIntent())
                .addRemoteInput(remoteInput)
                .build();
        builder.addAction(a);
      }
    }
  }

  PendingIntent getPendingIntent() {
    Intent resultIntent = new Intent(context, SettingsActivity.class);
    PendingIntent p =
        PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    return p;
  }

  private void setWearableBackground() {
    if (getPrefBool("background_image")) {
      wearableExtender.setBackground(getBitmapFromResource(R.drawable.album_cover));
    }
  }

  private void addWearablePages() {
    if (getPrefBool("add_wearable_pages")) {

      Notification.BigTextStyle textStyle = new Notification.BigTextStyle();
      textStyle.bigText(
          "\"It was the BigTextStyle on Page 1 of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.");

      Notification page1;
      page1 = new Notification.Builder(context)
          .setStyle(textStyle)
          .build();

      Notification page2 = new Notification.Builder(context)
          .setStyle(new Notification.BigTextStyle().bigText(
              "\"It was the BigTextStyle on Page 2 of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.\";"))
          .build();

      wearableExtender.addPage(page1);
      wearableExtender.addPage(page2);
    }
  }


  private void setStyleFromPref() {
    switch (getPrefString("use_style")) {
      case "use_big_picture_style":
        Bitmap bigPicture = getBitmapFromResource(R.drawable.big_picture_dog);
        builder.setStyle(new Notification.BigPictureStyle().bigPicture(bigPicture));
        break;
      case "use_big_text_style":
        String bigTextSubject = "About the times";
        String bigTextBody = "It was the BigTextStyle of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.";
        Spanned bigText = Html.fromHtml(bigTextSubject + "<br>" + bigTextBody);
        builder.setStyle(new Notification.BigTextStyle().bigText(bigText));
        //                builder.setContentTitle(Html.fromHtml("About the times"));
        builder.setContentText(Html.fromHtml("About the times"));
        break;
      case "use_inbox_style":

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
            .addLine(Html.fromHtml("<b>Luke</b> No! No! No!"))
            .addLine(Html.fromHtml("<b>DV</b> Search your feelings. You know it to be true."))
            .addLine(Html.fromHtml("<b>Luke</b> No. No. That's not true! That's impossible!"))
            .addLine(Html.fromHtml("<b>DV</b> No. I am your father."))
            .addLine(Html.fromHtml("<b>Luke</b>  He told me enough! He told me you killed him."))
            .addLine(Html.fromHtml(
                "<b>DV</b> If you only knew the power of the dark side. Obi-Wan never told\n" +
                    "you what happened to your father."))
            .setBigContentTitle("41 new messages in Parenthood")
            .setSummaryText("+35 more");
        builder.setStyle(inboxStyle);
        break;
      case "use_media_style":
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle();

        /// http://developer.android.com/reference/android/app/Notification.MediaStyle.html
        // should set smallicon to play, largeicon to album art, contenttitle as track title, content text as album/artist

        builder.addAction(createAction(R.drawable.ic_play_arrow_white_36dp, "Play"));
        builder.addAction(createAction(R.drawable.ic_fast_forward_white_36dp, "Fast Forward"));
        builder.addAction(createAction(R.drawable.ic_record_voice_over_white_36dp, "Sing!"));
        builder.setLargeIcon(getBitmapFromResource(R.drawable.album_cover));
        builder.setStyle(mediaStyle);
        break;
    }
  }

  private void setLargeIconFromPref() {
    String largeIcon = getPrefString("large_icon");
    switch (largeIcon) {
      case "none":
        break;
      case "photo_female_watches_as_eyes":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.photo_female_watches_as_eyes));
        break;
      case "profile_male_finger_pattern":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_male_finger_pattern));
        break;
      case "profile_man_asian":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_asian));
        break;
      case "profile_man_hat":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_hat));
        break;
      case "profile_woman_hat":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_woman_hat));
        break;
      case "profile_man_watch":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_watch));
        break;
      case "launcher_icon":
        builder.setLargeIcon(getBitmapFromResource(R.drawable.ic_launcher));
        break;
    }
  }

  private void setVibrationFromPref() {
    if (getPrefBool("vibrate")) {
      builder.setVibrate(new long[]{1000, 1000});
    }
  }

  private void addActionsFromPref() {
    Log.d("mj.actions", "ready to run through");
    Set<String> selectedActions = getPrefStringSet("actions");

    if (selectedActions == null) {
      return;
    }

    for (String s : selectedActions) {
      Log.d("mj.", "mj.action" + s);
      switch (s) {
        case "reply":
          builder.addAction(createAction(R.drawable.ic_reply_white_36dp, "Reply"));
          break;
        case "archive":
          builder.addAction(createAction(R.drawable.ic_archive_white_36dp, "Archive"));
          break;
        case "data":
          builder.addAction(createAction(R.drawable.ic_graph, "See stats"));
          break;
        case "comment":
          builder.addAction(createAction(R.drawable.ic_comment_white_36dp, "Comment"));
          break;
        case "like":
          builder.addAction(createAction(R.drawable.ic_thumb_up_white_36dp, "Like"));
          break;
        case "open":
          builder.addAction(createAction(R.drawable.ic_album_white_36dp, "Open"));
          break;
        case "done":
          builder.addAction(createAction(R.drawable.ic_checkmark, "Done"));
          break;
      }
    }
  }

  private void addWearableActionsFromPref() {
    Set<String> selectedActions = getPrefStringSet("wearable_actions");

    if (selectedActions == null) {
      return;
    }

    for (String s : selectedActions) {
      Log.d("mj.", "mj.action" + s);
      switch (s) {
        case "reply":
          wearableExtender.addAction(createAction(R.drawable.ic_reply_white_36dp, "Reply"));
          break;
        case "archive":
          wearableExtender.addAction(createAction(R.drawable.ic_archive_white_36dp, "Archive"));
          break;
        case "comment":
          wearableExtender.addAction(createAction(R.drawable.ic_comment_white_36dp, "Comment"));
          break;
        case "like":
          wearableExtender.addAction(createAction(R.drawable.ic_thumb_up_white_36dp, "Like"));
          break;
        case "open":
          wearableExtender.addAction(createAction(R.drawable.ic_album_white_36dp, "Open"));
          break;
        case "done":
          wearableExtender.addAction(createAction(R.drawable.ic_check_circle_black_24dp, "Done"));
          break;
      }
    }
  }

  Notification.Action createAction(int iconId, String label) {
    Notification.Action action;
    Icon i = Icon.createWithResource(context, iconId);
    Intent intent = new Intent(context, SettingsActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, label.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    action = new Notification.Action.Builder(i, label, pendingIntent).build();
    return action;
  }

  Bitmap getBitmapFromResource(int resourceId) {
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
    return bitmap;
  }


}
