Simple Last.fm Scrobbler
========================

Simple Last.fm Scrobbler (SLS) is a simple app that scrobbles music listened
to on an Android phone. Scrobbling means submitting listening information to
Last.fm (and optionally/additionally Libre.fm) when you play a track, and you
can then get music recommendations and view you listening history and statistics
at Last.fm.

More info about scrobbling can be found on [Last.fm's FAQ](http://www.last.fm/help/faq?category=Scrobbling#201).

**If anyone wants to take part in developing this app further, I'm looking for contributors. Please email me.**

If you need inspiration on stuff to do, you might want to have a look at the list of issues.

Contact email:
[simple.lfm.scrobbler@gmail.com](mailto:simple.lfm.scrobbler@gmail.com)

Download
--------
You can find the latest version of Simple Last.fm Scrobbler in the
[Google Play Store](https://play.google.com/store/apps/details?id=com.adam.aslfms).

Features
--------

 * Scrobbling
 * Now playing notifications
 * Power-saving settings
 * Caching scrobbles (while offline or through settings)
 * Editing scrobble cache
 * More

Supported websites (to scrobble to)
-----------------------------------

 * [Last.fm](http://last.fm)
 * [Libre.fm](http:///libre.fm)

Apps
----

 * Default Android Music Player
 * HTC Hero Music Player
 * [Meridian Player](http://sites.google.com/site/eternalsandbox/Home/meridian-video-player) **(a)**
 * [RockOn](http://abrantix.org/rockon.php) ([market](https://market.android.com/details?id=org.abrantes.filex)) **(a)**
 * [MixZing](http://mixzing.com/android.html ) ([market](https://market.android.com/details?id=com.mixzing.basic)) **(a)**
 * [Archos Music Player](http://www.archos.com/products/imt/archos_5it/index.html) **(b)**
 * [Rhapsody Android Beta](http://www.rhapsody.com/android/download) **(b)**
 * [Vanilla Music Player](http://github.com/kreed/vanilla) ([market](https://market.android.com/details?id=org.kreed.vanilla)) **(a)**
 * [bTunes](http://www.btunesmusicplayer.com/default.html) ([market](https://market.android.com/details?id=com.bmayers.bTunesRelease), and [more](http://www.facebook.com/pages/bTunes/362875048125?v=wall))
 * [³ (cubed)](http://abrantix.org/3.php) - Appears as !RockOn !NextGen in the Enabled Apps screen ([market](https://market.android.com/details?id=org.abrantix.rockon.rockonnggl))
 * [WIMP](http://wimp.no/)
 * [Zimly](http://zim.ly/)
 * [Just Playlists](http://jp.folsoms.info)
 * [PowerAMP](http://powerampapp.com/) ([market](https://market.android.com/details?id=com.maxmpz.audioplayer))
 * [Winamp](http://blog.winamp.com/2010/11/30/winamp-for-android/)
 * [myTouch 4G](http://mytouch.t-mobile.com/)
 * [Google Music](http://music.google.com/music/)
 * [Player Pro](http://www.aplayerpro.com/) ([market](https://market.android.com/details?id=com.tbig.playerpro))
 * [jukefox(http://www.jukefox.org/)] ([market](https://market.android.com/details?id=ch.ethz.dcg.pancho2)) **(a)**
 * [DAAP Media Player](http://code.google.com/p/daap-client/ ) ([market](https://market.android.com/details?id=org.mult.daap))
 * [Folder Player](http://folderplayer.com/) ([market](https://market.android.com/details?id=com.folderplayer))
 * [GoneMAD Music Player](http://gonemadmusicplayer.blogspot.com/) ([market](https://market.android.com/details?id=gonemad.gmmp))
 * MIUI Music Player
 * [ServeStream](http://sourceforge.net/projects/servestream/) ([market](https://market.android.com/details?id=net.sourceforge.servestream))
 * Default Sony Ericsson/Xperia Music Player
 * More to come...

**(a)** Uses the [Scrobble Droid API](http://code.google.com/p/scrobbledroid/wiki/DeveloperAPI) - identifies under "Enabled apps" as "Scrobble Droid Apps".

**(b)** It presents itself to SLS as the Android Music Player. This means that it won't show up under "Enabled apps" under its real name, but under Android Music Player.


Known compatible devices: **(c)**
-----------------------------

Tested by me:

 * Google Nexus One
 * HTC Magic
 * HTC Desire

Reported working:

 * Acer Liquid A1
 * Archos 5 Internet Tablet
 * HTC Desire HD
 * HTC Desire Z
 * HTC Dream
 * HTC Droid Eris
 * HTC Droid Incredible (should work, email me if it doesn't)
 * HTC Flyer
 * HTC Hero
 * HTC Legend
 * HTC Tattoo
 * HTC Wildfire
 * LG Optimus One P500
 * LG Optimus V
 * Motorola Droid
 * Motorola Milestone
 * Samsung Galaxy Ace (probably doesn't work with builtin player)
 * Samsung Galaxy Spica (doesn't work with the built-in 2.1 music app)
 * Sony Ericsson Xperia X10 Mini Pro

Issues:

 * Motorola CLIQ/DEXT ([http://code.google.com/p/a-simple-lastfm-scrobbler/issues/detail?id=25 see this])

**(c)** SLS should work on any device that can install it, and that can run one of the music apps above.

Changes
-------

For a complete list of changes, see [the changelog](https://github.com/tgwizard/sls/blob/master/assets/changelog.txt)

Questions, Bugs, Suggestions, Contributions, Thoughts...
--------------------------------------------------------

First, read the [FAQ (old)](http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/FAQ)
to see if you can find any help with your issue.

If you can't find it there, you can always email me,
[simple.lfm.scrobbler@gmail.com](mailto:simple.lfm.scrobbler@gmail.com),
or open an issue here on github.


Developers
----------
If you want to scrobble music through SLS, see [the wiki (old)](http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/Developers ).

Credits
-------

All of the code is open source, and as of 2010-04-27 lincesed under the Apache License 2.0 (it was previously GPLv3). **I've not yet removed the refernces to GPL from the source code**.

 * Almost all of the code is written by me, so: Copyright 2009 Adam Renberg.

 * A small MD5 utilities class seems to be written by many people, but it is released under GPLv2, and I got it from [ostermiller.org](http://ostermiller.org/utils/MD5.html ).

 * The icon was made by [Tha PHLASH](http://www.thaphlash.com/).

 * The Last.fm logo is copyright of Last.fm, taken from their [media kit](http://www.last.fm/resources).

 * The Libre.fm logo is probably copyright of Libre.fm, used in good faith. (Because of their name and stated mission, I assume it is okay).

I use copyright here only in the sense of proper attribution. Do whatever you want with the code (as long as the licenses are followed). I switched to the Apache License 2.0 for a less viral license.
