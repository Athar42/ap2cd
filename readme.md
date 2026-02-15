# AudioPlayer To CustomDiscs v1.1.4
### For Paper and Folia
### 1.21.7 to 1.21.11

[![GitHub Total Downloads](https://img.shields.io/github/downloads/Athar42/ap2cd/total?style=plastic&label=GitHub%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/audioplayer-to-customdiscs) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Modrinth Downloads](https://img.shields.io/modrinth/dt/audioplayer-to-customdiscs?style=plastic&label=Modrinth%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/audioplayer-to-customdiscs) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![AudioPlayer-to-CustomDiscs](https://img.shields.io/hangar/dt/AudioPlayer-to-CustomDiscs?link=https%3A%2F%2Fhangar.papermc.io%2FAtharDev%2FAudioPlayer-to-CustomDiscs&style=plastic&label=Hangar%20Downloads&color=success)](https://hangar.papermc.io/AtharDev/AudioPlayer-to-CustomDiscs)

---

A Paper plugin to (kind of) convert an AudioPlayer instrument into a working CustomDiscs compatible format.

This plugin will be useful if you need to play a custom music disc, goat horn or player head initially created with AudioPlayer, then ported to a [CustomDiscs](https://modrinth.com/plugin/customdiscs-plugin) compliant server.

Release 1.1.0 introduce the automatic conversion ! (Manual conversion mode is still available and the one active by default)

Also, no permission system is in place for now (unless requested).

Join the discord for support: https://discord.gg/rJtBRmRFCr.

---

## How to use
This plugin work in two modes :
- Automatic conversion mode
- Manual conversion mode

In automatic mode (when you set "automaticConvert" to true in the ```config.yml``` file) :
- Goat horns will be converted when attempting to first play it. A second attempt will play it.
- Custom discs will be converted when being played (inserted by players or, in theory, any automated form of insertion).
- Player heads will also be converted instantly on head placement. When already placed on top of a note block, one or two triggers will be required to be actually played.  

In manual mode (which will still work even if the "Automatic mode" is enabled) :

- Take the item you want to convert in your **main hand** ONLY
- In chat, use ```/ap2cd convert```

Special note : This plugin will now check if a corresponding audio file exist (by its ID value), so ensure the following :
- Original AudioPlayer music files located inside ```audio_player_data/``` should be placed inside ```plugins/CustomDiscs/musicdata/```
  - Failure to provide those music files will result in the conversion to fail (without modifying the original item).


In case the detected file exist in multiple extensions type, the wrong one could be used.
In such rare scenario, you can use one of those commands while holding the item in your main hand :
- Set to WAV type : ```/ap2cd setwav```
- Set to MP3 type : ```/ap2cd setmp3```
- Set to FLAC type : ```/ap2cd setflac```

---

## Versions support matrix :

| Minecraft version                          | Server type     | Compatible versions        | Latest compatible<br>version                                                  |
|--------------------------------------------|-----------------|----------------------------|-------------------------------------------------------------------------------|
| **1.21.7-9, 1.21.8**<br>**1.21.8**         | Paper<br>Folia  | 1.0.0-1.1.4<br>1.0.0-1.1.4 | [1.1.4](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.1.4) |
| **1.21.9, 1.21.10**<br>**1.21.9, 1.21.10** | Paper<br>Folia  | 1.0.0-1.1.4<br>1.0.0-1.1.4 | [1.1.4](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.1.4) |
| **1.21.11**<br>**1.21.11**                 | Paper<br>Folia  | 1.1.1<br>1.1.4             | [1.1.4](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.1.4) |

---

## Final notes

This plugin may **(or may not)** work with earlier versions of PaperMC/Folia.

Version 1.1.0+ now require ProtocolLib to be installed too.

**This plugin will now be made available on both Modrinth and Hangar (PaperMC).**