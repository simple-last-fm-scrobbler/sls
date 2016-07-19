TroubleShooting (SLS = simple last.fm scrobbler)
------------------------------------------------


Solutions To Common problems.
-----------------------------
1. Clear credentials and log in again.
2. Check if you have other scrobbling apps open. (this can sometimes be the problem)
3. Check your phone's time is automatic.
4. Check battery/power settings on phone, add SLS to protected/whitelist (power save disabled for SLS).
5. Check SLS options. (power settings)
6. Check SLS options. (enabled apps.)
7. Check your music app's settings.
8. Disable some of SLS' enabled apps if there are clashes. Some music players broadcast while closed (PowerAmp & DoubleTwist & DoubleTwist CloudPlayer). Some music players have mixed broadcasts. ("Anroid Music Player" and it's own broadcast)
9. Try a different music player that scrobbles if possible.
10. If you don't want to uninstall or clear the cache, create an issue on the GitHub. https://github.com/tgwizard/sls/issues

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


Know Issue: Compatibility vs. Structure
---------------------------------------

##Two major conflicting issues.

SLS tries to provide support for a person to have many apps downloaded and scrobble from without broadcast clashes.

### Conflict

SLS provides support for a variety of applications.

OR

SLS provides support for single track repeat and more structured scrobbling.


##Reasons for conflict issues

Some of these applications don't share track duration (well).

AND

Some of these applications don't tell SLS when the track is paused or completed, so SLS cannot find out when the track officially started or finished.

AND

Some of these applications have mixed broadcasts for SLS, Android Music Player, and Scrobble Droid.


Developer's Job
---------------

I am manually going through all the applications to see which ones don't work with a more compatible and structured scrobbling service.

The current Queue method works for now.
