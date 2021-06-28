TroubleShooting (SLS = simple last.fm scrobbler)
------------------------------------------------


Solutions To Common problems.
-----------------------------

1. Clear credentials and log in again.
2. Check if you have other scrobbling apps open. (this can sometimes be the problem)
3. Check your phone's time is automatic.
4. Try enable "OnGoing" (and phone notifications) in Options.
5. Check battery/power settings on phone, add SLS to protected/whitelist (power save disabled for SLS).
6. Check SLS options. (power settings)
7. Check SLS options. (enabled apps.)
8. Check your music app's settings. (e.g. in Spotify, whether broadcasting to other apps is on)
9. Disable some of SLS' enabled apps if there are clashes. Some music players broadcast while closed (PowerAmp & DoubleTwist & DoubleTwist CloudPlayer). Some music players have mixed broadcasts. ("Anroid Music Player" and it's own broadcast)
10. Try a different music player that scrobbles if possible.
11. If you don't want to uninstall or clear the cache, create an issue on the GitHub. https://github.com/tgwizard/sls/issues

Note: You may lose your scrobbles if you do any of the following.

1. If you can, reinstall the app.
2. Clear the data also if reinstall does not work.
3. Create and issue on the GitHub. https://github.com/tgwizard/sls/issues

Can't Connect?
--------------

1. Clear the credentials and relogin.
2. Check if you can login to last.fm from your phone.
3. Check if Last.fm is up http://status.last.fm/
4. Check if Libre.fm is up https://twitter.com/librefm
5. Check if ListenBrainz is up https://listenbrainz.org/current-status


Losing Scrobbles?
-----------------

1. Metadata is wrong.
2. Time is wrong.
3. or something is wrong with the app.
4. or something is wrong with Last.fm / Libre.fm

##Possible ignored message codes:

1. None (the request passed all filters).
2. Filtered artist.
3. Filtered track.
4. Timestamp too far in the past.
5. Timestamp too far in the future.
6. Max daily scrobbles exceeded.

Enable Notification Access without Settings App
-----------------

1. Get ADB
2. enable Developer Settings (System Information and tap the Build Number a few times)
3. enable USB-Debugging in the Developer Settings
4. run `adb devices` to start the adb daemon and send an auth-request to your device
5. accept on your device
6. run `adb shell settings put secure enabled_notification_listeners %nlisteners:com.adam.aslfms/com.adam.aslfms.service.ControllerReceiverService`
   to enable notification access (Tested on FiiO M9)

[Compatible Applications List](https://github.com/tgwizard/sls/blob/master/Compatability.md)
--------------------------------------------------------------------------------------------


[Know Issues & Core Tasks](https://github.com/a93-39a/sls#core-tasks)
---------------------------------------
