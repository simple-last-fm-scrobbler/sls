## Introduction

Any app can easily scrobble their music through Simple Last.fm Scrobbler (SLS). Just to send certain broadcasts with the appropriate track information, and SLS will do the all the account management, network requests and error-handling etc.

Implement the behavior below, and it should just work. Or, if you prefer, email me some specifications on the notifications your app sends and I'll implement directly into SLS.

If you use this API you could send me an email, [simple.lfm.scrobbler@gmail.com](mailto:simple.lfm.scrobbler@gmail.com), and tell me about your app. That way I can add it to the list of supported apps.

If you have any questions about this API, or suggestions on how to improve it, I'd be happy help or discuss it with you.

## Details

The broadcast action is:

`com.adam.aslfms.notify.playstatechanged`

The intent your app broadcast must contain one of these states:

    | State      | Integer value | What it means                             |
    +------------+---------------+-------------------------------------------+
    | START      |             0 | A track has started playing               |
    | RESUME     |             1 | Playback has been resumed for a/the track |
    | PAUSE      |             2 | Playback has been paused for a/the track  |
    | COMPLETE   |             3 | A/The track has finished playing          |

The possible fields for the intent (to be put in its extras bundle) are:

    | Name         | Type   | Required | Content                                                             |
    +--------------+--------+----------+---------------------------------------------------------------------+
    | app-name     | String | Yes      | The name of the music app                                           |
    | app-package  | String | Yes      | The package of the music app                                        |
    | state        | int    | Yes      | One of the states above                                             |
    | album        | String | No       | The album name _Strongly recommended_                               |
    | artist       | String | Yes      | The artist name                                                     |
    | track        | String | Yes      | The track name                                                      |
    | duration     | int    | Yes      | The duration of the track (in seconds)                              |
    | track-number | int    | No       | Track number on album                                               |
    | mbid         | String | No       | A Track-ID from <http://musicbrainz.org/doc/TrackID>                |
    | source       | String | No       | How the user listens to the music, see table below (default is 'P') |

Table of source values:

    | Value | How the track was "chosen"                                                                            |
    +-------+-------------------------------------------------------------------------------------------------------+
    | 'P'   | Chosen by the user (the most common value; unless you have a reason for choosing otherwise, use this) |
    | 'R'   | Non-personalised broadcast (e.g. Shoutcast, BBC Radio 1, etc.)                                        |
    | 'E'   | Personalised recommendation except Last.fm (e.g. Pandora, Launchcast, etc.)                           |
    | 'U'   | Source unknown                                                                                        |

These values are taken from the Last.fm [Submissions Protocol Specifiaction, section 3.2](http://www.last.fm/api/submissions#3.2).

## When to send

You should broadcast an intent with the information above whenever the user starts playing a new track (including when it is the next track in a playlist), resumes a paused track or pauses or stops a already playing track. Sending intents at other times (such as when the user is seeking within a track) is unnecessary, but does no harm.

It is not necessary to broadcast with state set to COMPLETE for a track to scrobble*; if a new track starts playing and START is broadcast, previously broadcasted tracks will scrobble automatically.

*If you play the same track again directly after it has finished playing (e.g. looping a playlist with one track), you have to send a COMPLETE for proper scrobbling.

## Examples

This sends a broadcast that Chris Cornell's song "You Know My Name" has started playing, and will scrobble any previously playing tracks:

~~~ java
Intent bCast = new Intent("com.adam.aslfms.notify.playstatechanged");
bCast.putExtra("state", START);
bCast.putExtra("app-name", "Example App");
bCast.putExtra("app-package", "com.example.exampleapp");
bCast.putExtra("artist", "Chris Cornell");
bCast.putExtra("album", "Casino Royale");
bCast.putExtra("track", "You Know My Name");
bCast.putExtra("duration", 244);
sendBroadcast(bCast);
~~~

And this says that James Marster's song "Looking At You" has finished playing and should be scrobbled: 

~~~ java
Intent bCast = new Intent("com.adam.aslfms.notify.playstatechanged");
bCast.putExtra("state", COMPLETE);
bCast.putExtra("app-name", "Example App");
bCast.putExtra("app-package", "com.example.exampleapp");
bCast.putExtra("artist", "James Marsters");
bCast.putExtra("album", "Like A Waterfall");
bCast.putExtra("track", "Looking At You");
bCast.putExtra("duration", 175);
sendBroadcast(bCast);
~~~

This might be if you have a radio app, which just started playing a new track:

~~~ java
Intent bCast = new Intent("com.adam.aslfms.notify.playstatechanged");
bCast.putExtra("state", START);
bCast.putExtra("app-name", "Example App");
bCast.putExtra("app-package", "com.example.exampleapp");
bCast.putExtra("artist", "Yohanna");
bCast.putExtra("album", "Eurovision Song Contest: Moscow 2009 (disc 2)");
bCast.putExtra("track", "Is It True? (Iceland)");
bCast.putExtra("duration", 181);
bCast.putExtra("track-number", 3);
bCast.putExtra("mbid", "ceb9d062-145c-4831-839b-3be53e9d5549");
bCast.putExtra("source", "R");
sendBroadcast(bCast);
~~~

The app ³ (cubed) [implements](http://github.com/fabrantes/rockonnggl/blob/master/src/org/abrantix/rockon/rockonnggl/RockOnNextGenService.java) this API.

## Internals

The behavioiur to handle these API broadcasts is implemented in the `BroadcastReceiver` [SLSAPIReceiver](https://github.com/tgwizard/sls/blob/master/src/com/adam/aslfms/receiver/SLSAPIReceiver.java). It might be helpful to read through that class to get a feel about the API.

## A user's perspective

When a user starts playing music in a music app using this api, she will automatically have her music scrobbled. The first broadcast to be sent to SLS will create an entry in the Supported Apps section, where the user can disable/enable scrobbling from that application (it is enabled by default).

To make this work properly, the two fields `app-name` and `app-package` are crucial. `app-name` is the string that gets displayed to the user, and `app-package` is what identifies the source of the music played. This should be the "root package" (or whatever it's called) of the music app, and it should never change. 

## Troubleshooting

### General

This API was first implemented in version 1.2.3 of SLS, available on Google Play, so this shouldn't be an issue.

### Broadcast doesn't get through

If your broadcast doesn't seem to get through, first check if SLS actually receives it. Everytime SLS gets an intent meant for scrobbling, it outputs to `LogCat` (debug level):

`"SLSPlayStatusReceiver" : "Action received was: [received intent]"`

If that seems fine, make sure you've logged in to either last.fm or libre.fm (or both). This is one of the first checks that are made when SLS receives an intent, and it will abort if it hasn't authenticated correctly. If this is the cause, you'll get a `LogCat` message (information level) saying something like:

`"SLSPlayStatusReceiver" : "The user has not authenticated, won't propagate the submission request"`.

Otherwise, check `LogCat` forr error-messages of the form:

`"SLSPlayStatusReceiver" : "Got a bad track from: [some name], ignoring it ([reason])"`.

### Crashes

SLS aspires to never crash, so if it does, please tell me. Whatever data you send to SLS, it **should** be fine.

### Other

Open an issue here on github and we'll look into an problems you might have. Saved `LogCats` can be useful. You can also send me an [email](mailto:simple.lfm.scrobbler@gmail.com ), and I'll answer as quickly as I can

## Custom Broadcasts

If you already broadcast your own intents, tell me about them and I'll see if I can work them into SLS. 

## Thanks

A tip of the hat to jjc1138 of Scrobble Droid, his/her documentation is superb.