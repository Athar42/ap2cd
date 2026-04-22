# AudioPlayer To CustomDiscs v1.2.0
### For Paper, Folia and Forks (Purpur, Leaf, ...)
### 1.21.7 to 26.1.2

[![GitHub Total Downloads](https://img.shields.io/github/downloads/Athar42/ap2cd/total?style=plastic&label=GitHub%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/audioplayer-to-customdiscs) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Modrinth Downloads](https://img.shields.io/modrinth/dt/audioplayer-to-customdiscs?style=plastic&label=Modrinth%20Downloads&color=success "Click here to download the plugin")](https://modrinth.com/plugin/audioplayer-to-customdiscs) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![AudioPlayer-to-CustomDiscs](https://img.shields.io/hangar/dt/AudioPlayer-to-CustomDiscs?link=https%3A%2F%2Fhangar.papermc.io%2FAtharDev%2FAudioPlayer-to-CustomDiscs&style=plastic&label=Hangar%20Downloads&color=success)](https://hangar.papermc.io/AtharDev/AudioPlayer-to-CustomDiscs)

---

## ⚠️ Dependency notice — PacketEvents required

**Since version 1.1.5, [PacketEvents](https://modrinth.com/plugin/packetevents) (v2.11.2+) is required.**\
ProtocolLib is no longer used — it can be safely removed if no other plugin needs it.

## ⚠️ Java 25 required

**Starting with version 1.2.0, this plugin requires at least Java 25 to run on your server.**

---

A Paper based plugin to convert [AudioPlayer](https://modrinth.com/plugin/audioplayer) instruments into a [CustomDiscs](https://modrinth.com/plugin/customdiscs-plugin) compatible format.

> Any Paper's forks should be supported. In case of issues, reach us on our [Discord server](https://discord.gg/rJtBRmRFCr).

Useful if you need to play a custom music disc, goat horn or player head initially created with AudioPlayer, then ported to a CustomDiscs-compliant server.

> No permission system is in place for now (unless requested).

Join our Discord for support: https://discord.gg/rJtBRmRFCr

---

## Dependencies

| Plugin | Required | Notes |
|--------|----------|-------|
| [PacketEvents](https://modrinth.com/plugin/packetevents) | ✅ Required | Since v1.1.5 — tested with v2.11.2 |
| [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) | ⛔ Up to v1.1.4 only | No longer required as of v1.1.5 |

---

## How to Use

This plugin works in two modes: **automatic** and **manual**.

### Prerequisite — Audio Files

This plugin checks whether a corresponding audio file exists (by its ID) before converting. Ensure the following:

- Original AudioPlayer music files from `audio_player_data/` must be placed in `plugins/CustomDiscs/musicdata/`
- Failure to provide those files will cause the conversion to fail, **without modifying the original item**

### Automatic Mode

Enable it by setting `automaticConvert: true` in `config.yml`:

- **Goat horns** — converted on first use attempt. A second attempt will play it.
- **Custom discs** — converted when inserted by a player or any automated mechanism.
- **Player heads** — converted instantly on placement. When already placed on a note block, one or two triggers may be needed before playback.

### Manual Mode

Works regardless of whether automatic mode is enabled.\
Hold the item in your **main hand** only, then run:

```
/ap2cd convert
```

### Audio File Type Override

If multiple file extensions exist for the same audio ID, the wrong one may be selected.\
While holding the item in your **main hand**, use one of the following:

| Command | Effect                   |
|---------|--------------------------|
| `/ap2cd setwav` | Force set to WAV format  |
| `/ap2cd setmp3` | Force set to MP3 format  |
| `/ap2cd setflac` | Force set to FLAC format |

---

## Configuration

**config.yml**

```yaml
# [AP2CD Config]

# Disable or enable the automatic conversion process while items are being used.
automaticConvert: false

# Debug Mode - To display some more logging and Stack Trace information.
# You SHOULD NOT turn this on, unless you want to be spammed by a lot of messages (console and player side).
debugMode: false
```

---

## Version Support Matrix

| Minecraft version          | Paper & Forks (Purpur, Leaf, ...)                                                     | Folia & Forks                                                                         |
|----------------------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| **1.21.7-9, 1.21.8**       | 1.0.0 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) | 1.0.0 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) |
| **1.21.9, 1.21.10**        | 1.0.0 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) | 1.0.0 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) |
| **1.21.11**                | 1.1.1 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) | 1.1.1 – [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0) |
| **26.1, 26.1.1, 26.1.2**   | [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0)         | [1.2.0](https://modrinth.com/plugin/audioplayer-to-customdiscs/version/1.2.0)         |

---

## Notes

- This plugin may **(or may not)** work with earlier versions of PaperMC/Folia.
  - While this plugin is marked as compatible with Paper's forks, no extensive tests has been made on them. Report any issues on our Discord !
- Versions 1.1.0 to 1.1.4 required [ProtocolLib](https://github.com/dmulloy2/ProtocolLib). As of 1.1.5, it is no longer needed and is replaced by [PacketEvents](https://modrinth.com/plugin/packetevents).
- This plugin is available on both [Modrinth](https://modrinth.com/plugin/audioplayer-to-customdiscs) and [Hangar](https://hangar.papermc.io/AtharDev/AudioPlayer-to-CustomDiscs).
