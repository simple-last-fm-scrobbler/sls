Last Updated August 2nd 2019

Should you notice a data mistake, please create a pull request or notify the developers.



* Default Android Music Player
* HTC Hero Music Player
* LG Music Player
* MIUI Music Player

**(sd)** Uses the [Scrobble Droid API](http://code.google.com/p/scrobbledroid/wiki/DeveloperAPI) - identifies under "Enabled apps" as "Scrobble Droid Apps".

**(amp)** It presents itself to SLS as the Android Music Player. This means that it won't show up under "Enabled apps" under its real name, but under Android Music Player.

| Code | Type of Broadcast            | Note                                             |
|------|------------------------------|--------------------------------------------------|
| sd   | ScrobbleDroid                | if you disable it in SLS, all apps using it fail |
| amp  | Android Music Player         | if you disable it in SLS, all apps using it fail |
| sls  | Simple Last.fm Scrobbler API |      Ideal Scrobble broadcast for SLS                                            |
| abs  | Abstract                     |  identifiable because of package information                                                |
| brk  | broken                       | may not work or is no longer in use              |
| mix  | Mixed (more than 1)          | has an array of possible methods {sd,amp..}      |
| sel  | Selectable                   | has an array of possible methods {sd,amp..}      |
| unt  | untested                     | untested apps                                    |


| Code  | Type of Code         | Result                                       |
|-------|----------------------|----------------------------------------------|
| dis   | can be disabled in sls       | yes, no, unk (unknown) or cond (conditional) |
| dia   | can be disabled in music app | yes, no, unk (unknown) or cond (conditional) |
| notf  | notification support | yes, no, unk (unknown) or cond (conditional) |
| prb   | problems             | brief summary                                |
| samp  | sample broadcast     | sample data from track broadcast             |
| gplay | google play link     | simple html link                             |

| Gplay                                                                                                                              | Bcast | DIS  | DIA | Notf | Prb                        | Samp | Site                                                                           |
|------------------------------------------------------------------------------------------------------------------------------------|-------|------|-----|------|----------------------------|------|--------------------------------------------------------------------------------|
| [8tracks](https://play.google.com/store/apps/details?id=com.e8tracks)                                                              | abs   | yes  | unk | no   |                            |      | [Site](https://8tracks.com/)                                                   |
| [AIMP](https://play.google.com/store/apps/details?id=com.aimp.player)                                                              | abs   | yes  | unk | no   |                            |      | [Site](http://aimp.ru/)                                                        |
| [Amazon Music](https://play.google.com/store/apps/details?id=com.amazon.mp3)                                                       | abs   | yes  | unk | no   |                            |      | [Site](http://www.amazon.com/gp/feature.html?ie=UTF8&docId=1000454841)         |
| [Apollo](https://play.google.com/store/apps/details?id=com.andrew.apollo)                                                          | abs   | yes  | unk | no   |                            |      | [Site](https://forum.xda-developers.com/showthread.php?t=2625426)              |
| [Archos](https://play.google.com/store/apps/details?id=https://play.google.com/store/apps/details?id=com.archos.mediacenter.music) | abs   | yes  | unk | no   |                            |      | [Site](http://www.archos.com/products/imt/archos_5it/index.html)               |
| [BlackPlayer](https://play.google.com/store/apps/details?id=com.kodarkooperativet.blackplayerfree)                                 | mix   | cond | unk | no   |                            |      | [Site](https://www.reddit.com/r/blackplayer/)                                  |
| [BuMP](https://play.google.com/store/apps/details?id=com.n7mobile.micromusic)                                                      | amp   | cond | unk | no   | Sometimes Recognized       |      | [Site](https://n7mobile.com/en)                                                |
| [Clean Music](https://play.google.com/store/apps/details?id=com.myskyspark.music)                                                  | sd    | cond | unk | no   |                            |      | [Site](http://www.notriddle.com/)                                              |
| [DAAP](https://play.google.com/store/apps/details?id=org.mult.daap)                                                                | unt   | unk  | unk | no   |                            |      | [Site](http://code.google.com/p/daap-client/ )                                 |
| [DeaDBeeF](https://play.google.com/store/apps/details?id=org.deadbeef.android)                                                     | sd    | cond | unk | no   |                            |      | [Site](http://deadbeef.sourceforge.net/)                                       |
| [Deezer](https://play.google.com/store/apps/details?id=deezer.android.app)                                                         | abs   | yes  | unk | no   |                            |      | [Site](https://www.deezer.com/us/)                                             |
| [doubleTwist](https://play.google.com/store/apps/details?id=com.doubleTwist.androidPlayer)                                         | amp   | cond | unk | no   | Broadcasts when closed !   |      | [Site](https://www.doubletwist.com)                                            |
| [Dsub](https://play.google.com/store/apps/details?id=github.daneren2005.dsub)                                                      | abs   | yes  | unk | no   |                            |      | [Site](http://forum.subsonic.org/forum/viewforum.php?f=16)                     |
| [Folder Player](https://play.google.com/store/apps/details?id=com.folderplayer)                                                    | sd    | cond | unk | no   |                            |      | [Site](http://folderplayer.com/)                                               |
| [foobar2000](https://play.google.com/store/apps/details?id=com.foobar2000.foobar2000)                                              | amp   | cond | unk | no   |                            |      | [Site](https://www.foobar2000.org)                                             |
| [GoneMAD Music Player](https://play.google.com/store/apps/details?id=gonemad.gmmp)                                                 | mix   | yes  | unk | no   |                            |      | [Site](http://gonemadmusicplayer.blogspot.com/)                                |
| [Huawei Music](https://play.google.com/store/apps/details?id=com.android.mediacenter)                                              | abs   | yes  | unk | no   |                            |      | [Site](http://music.huaweimobilecloudservice.com/en/)                          |
| [InfiniTracks](https://play.google.com/store/apps/details?id=com.smithyproductions.infinitracks)                                   | amp   | cond | unk | no   |                            |      | [Site](http://smithyproductions.co.uk)                                         |
| [jetAudio](https://play.google.com/store/apps/details?id=com.jetappfactory.jetaudio)                                               | sel   | yes  | unk | no   |                            |      | [Site](https://www.facebook.com/jetappfactory)                                 |
| [JoeApollo](https://play.google.com/store/apps/details?id=the.joeapollo)                                                           | abs   | yes  | unk | no   |                            |      | [Site](http://www.josephpcohen.com/)                                           |
| [Kodi Remote](https://play.google.com/store/apps/details?id=ch.berard.xbmcremotebeta)                                              | sls   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [MediaMonkey](https://play.google.com/store/apps/details?id=com.ventismedia.android.mediamonkey)                                   | mix   | cond | unk | no   |                            |      | [Site](https://www.mediamonkey.com)                                            |
| [Meridian Player](https://play.google.com/store/apps/details?id=org.iii.romulus.meridian)                                          | sd    | cond | unk | no   |                            |      | [Site](http://sites.google.com/site/eternalsandbox/Home/meridian-video-player) |
| [MixZing](https://play.google.com/store/apps/details?id=com.mixzing.basic)                                                         | abs   | yes  | unk | no   |                            |      | [Site](http://mixzing.com/android.html )                                       |
| [MortPlayer Music](https://play.google.com/store/apps/details?id=de.stohelit.folderplayer)                                         | sd    | cond | unk | no   |                            |      | [Site](http://www.sto-helit.de/)                                               |
| [Mosaic player](https://play.google.com/store/apps/details?id=com.TFiveR.mosaicplayer)                                             | sel   | yes  | unk | no   |                            |      | [Site](https://sites.google.com/site/zorillasoft/)                             |
| [Mp3 Music Player](https://play.google.com/store/apps/details?id=us.music.marine)                                                  | mix   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Music Canoe](https://play.google.com/store/apps/details?id=com.lasthopesoftware.bluewater)                                        | unt   | unk  | unk | no   |                            |      | [Site](#)                                                                      |
| [Music Folder Player Free](https://play.google.com/store/apps/details?id=de.zorillasoft.musicfolderplayer)                         | amp   | cond | unk | no   |                            |      | [Site](http://mycloudplayers.com/)                                             |
| [Music Player by JRTStudio](https://play.google.com/store/apps/details?id=com.jrtstudio.music)                                     | sel   | yes  | unk | no   |                            |      | [Site](http://www.jrtstudio.com/)                                              |
| [Musixmatch](https://play.google.com/store/apps/details?id=com.musixmatch.android.lyrify)                                          | brk   | unk  | unk | no   |                            |      | [Site](#)                                                                      |
| [My Cloud Player](https://play.google.com/store/apps/details?id=com.mycloudplayers.mycloudplayer)                                  | sls   | yes  | unk | no   |                            |      | [Site](http://n7player.com/)                                                   |
| [n7player](https://play.google.com/store/apps/details?id=com.n7mobile.nplayer)                                                     | mix   | cond | unk | no   |                            |      | [Site](#)                                                                      |
| [Napster](https://play.google.com/store/apps/details?id=com.rhapsody)                                                              | abs   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Neutron (Eval)](https://play.google.com/store/apps/details?id=com.neutroncode.mpeval)                                             | abs   | yes  | unk | no   |                            |      | [Site](http://neutronmp.com/)                                                  |
| [Noise FM](https://play.google.com/store/apps/details?id=ru.modi.dubsteponline)                                                    | sel   | yes  | unk | no   |                            |      | [Site](http://noisefm.ru)                                                      |
| [Phonograph](https://play.google.com/store/apps/details?id=com.kabouzeid.gramophone)                                               | abs   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Play Music](https://play.google.com/store/apps/details?id=com.google.android.music)                                               | amp   | cond | unk | no   |                            |      | [Site](http://music.google.com/music/)                                         |
| [PlayerPro](https://play.google.com/store/apps/details?id=com.tbig.playerprotrial)                                                 | mix   | cond | unk | no   |                            |      | [Site](http://www.aplayerpro.com/)                                             |
| [Poweramp](https://play.google.com/store/apps/details?id=com.maxmpz.audioplayer)                                                   | mix   | yes  | unk | no   | Broadcasts when closed !   |      | [Site](http://powerampapp.com/)                                                |
| [Pulsar](https://play.google.com/store/apps/details?id=com.rhmsoft.pulsar)                                                         | unt   | unk  | unk | no   |                            |      | [Site](http://rhmsoft.com/?p=318)                                              |
| [Rdio](https://play.google.com/store/apps/details?id=com.rdio.android.ui)                                                          | abs   | yes  | unk | no   |                            |      | [Site](http://www.rdio.com/)                                                   |
| [Rocket Music Player](https://play.google.com/store/apps/details?id=com.jrtstudio.AnotherMusicPlayer)                              | sel   | yes  | unk | no   |                            |      | [Site](http://www.jrtstudio.com/)                                              |
| [Samsung Music](https://play.google.com/store/apps/details?id=com.sec.android.app.music)                                           | amp   | cond | unk | no   |                            |      | [Site](https://www.samsung.com/us/support/owners/app/samsung-music)            |
| [ServeStream](https://play.google.com/store/apps/details?id=net.sourceforge.servestream)                                           | unt   | unk  | unk | no   |                            |      | [Site](http://sourceforge.net/projects/servestream/)                           |
| [Shuttle](https://play.google.com/store/apps/details?id=another.music.player)                                                      | mix   | cond | unk | no   |                            |      | [Site](https://www.shuttlemusicplayer.com)                                     |
| [SmartVanilla Music](https://play.google.com/store/apps/details?id=su.thinkdifferent.vanilla)                                      | sd    | cond | unk | no   |                            |      | [Site](https://github.com/gordon01/vanilla)                                    |
| [Sony Music](https://play.google.com/store/apps/details?id=com.sonyericsson.music)                                                 | abs   | yes  | unk | no   |                            |      | [Site](https://www.sony.ca/en/electronics/sony-music-center-app)               |
| [SoundCloud](https://play.google.com/store/apps/details?id=com.soundcloud.android)                                                 | amp   | cond | unk | no   | Track length 40 char limit |      | [Site](#)                                                                      |
| [SoundSeeder](https://play.google.com/store/apps/details?id=com.kattwinkel.android.soundseeder.player)                             | sls   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Spotify](https://play.google.com/store/apps/details?id=com.spotify.music)                                                         | abs   | yes  | unk | no   |                            |      | [Site](https://spotify.com)                                                    |
| [Squeezer](https://play.google.com/store/apps/details?id=uk.org.ngo.squeezer)                                                      | sls   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Subsonic](https://play.google.com/store/apps/details?id=net.sourceforge.subsonic.androidapp)                                      | abs   | yes  | unk | no   |                            |      | [Site](https://github.com/daneren2005/Subsonic)                                |
| [TIDAL](https://play.google.com/store/apps/details?id=com.aspiro.tidal)                                                            | unt   | unk  | unk | no   |                            |      | [Site](#)                                                                      |
| [Timber](https://play.google.com/store/apps/details?id=naman14.timber)                                                             | abs   | yes  | unk | no   |                            |      | [Site](#)                                                                      |
| [Vanilla Music](https://play.google.com/store/apps/details?id=ch.blinkenlights.android.vanilla)                                    | sd    | cond | unk | no   |                            |      | [Site](https://github.com/vanilla-music/vanilla)                               |
| [VLC](https://play.google.com/store/apps/details?id=org.videolan.vlc.debug)                                                        | abs   | yes  | unk | no   | Patch may not be added.    |      | [Site](https://www.videolan.org/vlc/download-android.html)                     |
| [WiMP](https://play.google.com/store/apps/details?id=com.aspiro.wamp)                                                              | unt   | unk  | unk | no   |                            |      | [Site](http://wimp.no/)                                                        |
| [Winamp](https://play.google.com/store/apps/details?id=com.nullsoft.winamp)                                                        | brk   | unk  | unk | no   |                            |      | [Site](#)                                                                      |
| [XenoAmp](https://play.google.com/store/apps/details?id=pl.qus.xenoamp)                                                            | sls   | yes  | unk | no   |                            |      | [Site](#)                                                                      |

* More to come...


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
 * Samsung Galaxy S8
 * Samsung Galaxy Spica (doesn't work with the built-in 2.1 music app)
 * Sony Ericsson Xperia X10 Mini Pro
 * Sony Ericsson Xperia X8
 * Sony Ericsson Xperia Pro
 * Sony Xperia SP
 * Sony Xperia U
 * Sony Xperia XZ3

#### Issues:

 * HTC EVO 4G LTE doesn't seem to work
 * Motorola CLIQ/DEXT doesn't seem to work
