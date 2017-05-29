# Simple Last.fm Scrobbler (and Libre.fm and ListenBrainz and Custom Personal Servers)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tgwizard/sls)
[![License](http://img.shields.io/:license-apache-blue.svg?style=round)](LICENSE.md)
[![Google+](http://ibin.co/2BnMUC5fKA2e)](https://plus.google.com/communities/104841597680405981934)

Simple Last.fm Scrobbler (SLS) is a simple app that scrobbles music listened to on an Android phone. Scrobbling means submitting listening information to Last.fm (and optionally/additionally Libre.fm and ListenBrainz) when you play a track, and you can then get music recommendations and view you listening history and statistics at Last.fm.

Before the release of a new version of SLS, it will be available here to test for one week in the [Beta Testing Versions link](README.md#download).

More info about scrobbling can be found on [Last.fm's FAQ](http://www.last.fm/help/faq?category=Scrobbling#201).

[Trouble Shooting](TroubleShooting.md)

Contact emails: Try the first email address first.

[sls-app@googlegroups.com](mailto:sls-app@googlegroups.com) <- Please use this email. 

[HumbleBeeBumbleBeeDebugs@gmail.com](mailto:HumbleBeeBumbleBeeDebugs@gmail.com)  <- Support & Maintenance

[simple.lfm.scrobbler@gmail.com](mailto:simple.lfm.scrobbler@gmail.com)  <- Lead Developer

(if uncertain please email all of them)
 
## Download

[![Google Play Store](https://ibin.co/2hN6gwDRsRut.png)](https://play.google.com/store/apps/details?id=com.adam.aslfms)
[![F-Droid](https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Get_it_on_F-Droid.svg/320px-Get_it_on_F-Droid.svg.png)](https://f-droid.org/repository/browse/?fdid=com.adam.aslfms)

[(Beta) Release Downloads](https://github.com/tgwizard/sls/releases)

[Downloads deprecated](https://github.com/blog/1302-goodbye-uploads)

[Old Versions](https://github.com/tgwizard/sls/downloads)

## Features

 * Scrobbling
 * Now playing notifications
 * Power-saving settings
 * Caching scrobbles (while offline or through settings)
 * Editing scrobble cache
 * More

### Supported languages

 * English
 * Spanish
 * French
 * German
 * Brazilian Portuguese
 * Polish
 * Russian
 * Czech

Head to [Transifex](https://www.transifex.com/sls/sls) if you want to be a translator

### Supported scrobble services

 * [Last.fm](http://last.fm)
 * [Libre.fm](http:///libre.fm)
 * [ListenBrainz](https://listenbrainz.org)
 * [Custom GNU-fm/Libre.fm Server](https://git.gnu.io/gnu/gnu-fm/blob/master/gnufm_install.txt)
 * [Custom ListenBrainz Server](https://github.com/metabrainz/listenbrainz-server/blob/master/README.md)

### Apps

WARNING! Some android devices have special battery settings that will disable SLS scrobbling entirely or just when SLS is closed. [Example](http://developer.sonymobile.com/2013/04/03/how-sonys-battery-stamina-mode-works/)

Please note that some of the following applications require changing their default settings to enable scrobbling.

 * Default Android Music Player
 * HTC Hero Music Player
 * [Meridian Player](http://sites.google.com/site/eternalsandbox/Home/meridian-video-player) **(a)**
 * [MixZing](http://mixzing.com/android.html ) ([Google Play](https://play.google.com/store/apps/details?id=com.mixzing.basic)) **(a)**
 * [Archos Music Player](http://www.archos.com/products/imt/archos_5it/index.html) **(b)**
 * [New Vanilla Music Player](https://github.com/vanilla-music/vanilla) ([Google Play](https://play.google.com/store/apps/details?id=ch.blinkenlights.android.vanilla), [F-Droid](https://f-droid.org/repository/browse/?fdid=ch.blinkenlights.android.vanilla))
 * [SmartVanilla Music](https://github.com/gordon01/vanilla) ([Google Play](https://play.google.com/store/apps/details?id=su.thinkdifferent.vanilla))
 * [bTunes](http://www.btunesmusicplayer.com/default.html) ([Google Play](https://play.google.com/store/apps/details?id=com.bmayers.bTunesRelease), and [more](http://www.facebook.com/pages/bTunes/362875048125?v=wall))
 * [WIMP](http://wimp.no/)
 * [Just Playlists](http://jp.folsoms.info)
 * [PowerAMP](http://powerampapp.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.maxmpz.audioplayer))
 * [myTouch 4G](http://mytouch.t-mobile.com/)
 * [Google Music](http://music.google.com/music/) ([Google Play](https://play.google.com/store/apps/details?id=com.google.android.music))
 * [PlayerPro](http://www.aplayerpro.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.tbig.playerpro))
 * [jukefox](http://www.jukefox.org/) ([Google Play](https://play.google.com/store/apps/details?id=ch.ethz.dcg.pancho2)) **(a)**
 * [DAAP Media Player](http://code.google.com/p/daap-client/ ) ([Google Play](https://play.google.com/store/apps/details?id=org.mult.daap))
 * [Folder Player](http://folderplayer.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.folderplayer))
 * [GoneMAD Music Player](http://gonemadmusicplayer.blogspot.com/) ([Google Play](https://play.google.com/store/apps/details?id=gonemad.gmmp))
 * MIUI Music Player
 * [ServeStream](http://sourceforge.net/projects/servestream/) ([Google Play](https://play.google.com/store/apps/details?id=net.sourceforge.servestream))
 * Default Sony Ericsson/Xperia Music Player
 * [Neutron Music Player](http://neutronmp.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.neutroncode.mp))
 * [Rdio](http://www.rdio.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.rdio.android.ui))
 * [DeaDBeeF](http://deadbeef.sourceforge.net/) ([Google Play](https://play.google.com/store/apps/details?id=org.deadbeef.android)) **(a)**
 * [Music Pump XBMC Remote (Beta)](http://forum.xbmc.org/showthread.php?tid=131303) ([Google Play](https://play.google.com/store/apps/details?id=ch.berard.xbmcremotebeta))
 * [JRTStudio Android Music Player](http://www.jrtstudio.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.jrtstudio.music))
 * [Music Folder Player](https://sites.google.com/site/zorillasoft/) ([Google Play](https://play.google.com/store/apps/details?id=de.zorillasoft.musicfolderplayer))
 * [Apollo](http://forum.cyanogenmod.org/topic/56681-apollo-music-player/) **(b)**
 * LG Music Player
 * [Shuttle Music Player](https://plus.google.com/104842767174352132769/posts) ([Google Play](https://play.google.com/store/apps/details?id=another.music.player))
 * [My Cloud Player](http://mycloudplayers.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.mycloudplayers.mycloudplayer))
 * SoundSeeder ([Google Play](https://play.google.com/store/apps/details?id=com.kattwinkel.android.soundseeder.player))
 * [Amazon Music](http://www.amazon.com/gp/feature.html?ie=UTF8&docId=1000454841) ([Google Play](https://play.google.com/store/apps/details?id=com.amazon.mp3), [Amazon Appstore](http://www.amazon.com/gp/product/B004FRX0MY))
 * [Radio Noise FM](http://noisefm.ru) ([Google Play](https://play.google.com/store/apps/details?id=ru.modi.dubsteponline), [Amazon Appstore](http://www.amazon.com/gp/product/B00GAXBGM2))
 * [8tracks](https://8tracks.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.e8tracks), [Amazon Appstore](http://www.amazon.com/8tracks/dp/B007JWJIJG/)) **(b)**
 * [VLC](https://www.videolan.org/vlc/download-android.html) ([VLC](https://www.videolan.org/vlc/download-android.html), [Google Play](https://play.google.com/store/apps/details?id=org.videolan.vlc), [F-Droid](https://f-droid.org/repository/browse/?fdid=org.videolan.vlc)) **(b)**
 * [Spotify](https://spotify.com) ([Google Play](https://play.google.com/store/apps/details?id=com.spotify.music))
 * [n7player](http://n7player.com/) ([Google Play](https://play.google.com/store/apps/details?id=com.n7mobile.nplayer))
 * [MortPlayer](http://www.sto-helit.de/) ([Google Play](https://play.google.com/store/apps/details?id=de.stohelit.folderplayer))
 * [Rocket Player](http://www.jrtstudio.com/Music-Player-For-Android) ([Google Play](https://play.google.com/store/apps/details?id=com.jrtstudio.AnotherMusicPlayer))
 * BlackPlayer ([Google Play](https://play.google.com/store/apps/details?id=com.kodarkooperativet.blackplayerfree))
 * [Jetaudio](https://www.facebook.com/jetappfactory) ([Google Play](https://play.google.com/store/apps/details?id=com.jetappfactory.jetaudio)) 
 * [AIMP](http://aimp.ru/) ([Google Play](https://play.google.com/store/apps/details?id=com.aimp.player))
 * [Pulsar](http://rhmsoft.com/?p=318) ([Google Play](https://play.google.com/store/apps/details?id=com.rhmsoft.pulsar))
 * More to come...

**(a)** Uses the [Scrobble Droid API](http://code.google.com/p/scrobbledroid/wiki/DeveloperAPI) - identifies under "Enabled apps" as "Scrobble Droid Apps".

**(b)** It presents itself to SLS as the Android Music Player. This means that it won't show up under "Enabled apps" under its real name, but under Android Music Player.


### Known compatible devices

**Note.** SLS should work on any device that can install it, and that can run one of the music apps above.

#### Reported working:

 * Acer Liquid A1
 * Amazon Kindle Fire HDX
 * Archos 5 Internet Tablet
 * Google Galaxy Nexus
 * Google Nexus 7
 * Google Nexus 5
 * Google Nexus 4
 * Google Nexus One
 * HTC Desire
 * HTC Desire HD
 * HTC Desire S
 * HTC Desire Z
 * HTC Dream
 * HTC Droid Eris
 * HTC Droid Incredible (should work, email me if it doesn't)
 * HTC Evo 3D
 * HTC Flyer
 * HTC Hero
 * HTC Incredible S
 * HTC Legend
 * HTC Magic
 * HTC One (M8)
 * HTC Tattoo
 * HTC Wildfire
 * LG Optimus G
 * LG Optimus One P500
 * LG Optimus V
 * Motorola Defy (MB525)
 * Motorola Droid
 * Motorola Droid 3
 * Motorola G2 (2014)
 * Motorola Milestone
 * One Plus One
 * Samsung Galaxy Ace (probably doesn't work with builtin player)
 * Samsung Galaxy Core
 * Samsung Galaxy S2
 * Samsung Galaxy S2 LTE
 * Samsung Galaxy S3
 * Samsung Galaxy S4
 * Samsung Galaxy S5
 * Samsung Galaxy S7
 * Samsung Galaxy Spica (doesn't work with the built-in 2.1 music app)
 * Sony Ericsson Xperia X10 Mini Pro
 * Sony Ericsson Xperia X8
 * Sony Ericsson Xperia Pro
 * Sony Xperia SP
 * Sony Xperia U

#### Issues:

 * HTC EVO 4G LTE doesn't seem to work
 * Motorola CLIQ/DEXT doesn't seem to work
 
### Changes

For a complete list of changes, see [the changelog](app/src/main/assets/changelog.txt).

## Bugs, Contributions, Thoughts...

First, read the [FAQ](FAQ.md) to see if you can find any help with your issue.

If you can't find it there, you can always open an issue or pull request here on GitHub, or you can email me: [simple.lfm.scrobbler@gmail.com](mailto:simple.lfm.scrobbler@gmail.com)

## Developers

If you want to enable your app to scrobble music through SLS, see [Developer's API](Developer%27s%20API.md).

If you want to help out and start immediately, please see HumbleBeeBumbleBee's [Issue Summary](IssueSummary.md)

## Credits

All of the code is open source, released under [Apache License 2.0](LICENSE.md).

 * Almost all of the code is written by me, so: Copyright 2009-2015 Adam Renberg.
 * The Last.fm logo is copyright of Last.fm, taken from their [media kit](http://www.last.fm/resources).
 * The Libre.fm logo is probably copyright of Libre.fm, used in good faith. (Because of their name and stated mission, I assume it is okay).

I use copyright here only in the sense of proper attribution. Do whatever you want with the code (as long as the licenses are followed).

### Contributors
 * Adam Renberg, [github.com/tgwizard](https://github.com/tgwizard), main author
 * Argoday, [github.com/argoday](https://github.com/argoday), code fixes
 * inverse [github.com/inverse](https://github.com/inverse), core contributor
 * HumbleBeeBumbleBee, [github.com/HumbleBeeBumbleBee](https://github.com/HumbleBeeBumbleBee), core contributor
 * Sean O'Neil, [github.com/SeanPONeil](https://github.com/SeanPONeil), android 4.0
 * Andrew Thomson, support for MIUI Music Player
 * Mark Gillespie, support for Sony/Sony Ericsson/Xperia phones
 * Dmitry Kostjuchenko, support for Neutron Music Player
 * stermi, support for Rdio
 * Joseph Cooper, [github.com/grodin](https://github.com/grodin), support for JRTStudio Android Music Player
 * Shahar, [github.com/kshahar](https://github.com/kshahar), support for LG Music Player
 * theli-ua, [github.com/theli-ua](https://github.com/theli-ua), support for Amazon Music
 * metanota, [github.com/metanota](https://github.com/metanota), support for PlayerPro, bug fixes
 * Joel Teichroeb, [github.com/klusark](https://github.com/klusark), bug fixes
 * Tha PHLASH, [thaphlash.com](http://www.thaphlash.com/), old icon
 * pierreduchemin [github.com/pierreduchemin](https://github.com/pierreduchemin), French translation
 * moshpirit [github.com/moshpirit](https://github.com/moshpirit), Spanish translation 
 * bryansills [github.com/bryansills](https://github.com/bryansills), Eclipse to Android Studio, new icon, Material Design
 * herrherrmann [github.com/herrherrmann](https://github.com/herrherrmann), German translation
 * Alia5 [github.com/Alia5](https://github.com/Alia5), better Enabled apps handle
 * MendelGusmao [github.com/MendelGusmao](https://github.com/MendelGusmao), Brazil Portuguese Translation
 * Grzes58 [github.com/Grzes58](https://github.com/Grzes58), Polish
 * bajituka [github.com/bajituka](https://github.com/bajituka), Russian 

A complete list of [contributors](https://github.com/tgwizard/sls/graphs/contributors)
 
### Test device contributors

 * Dmitry Paskal, [github.com/paskal](https://github.com/paskal)
 * Iļja Gubins, [https://github.com/the-lay](https://github.com/the-lay)

Several people have also contributed with comments, suggestions and [issues](https://github.com/tgwizard/sls/issues/).
