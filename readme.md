# AudioPlayer To CustomDiscs v1.0.0 for Paper and Folia 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10
A Paper plugin to (kind of) convert an AudioPlayer instrument into a working CustomDiscs compatible format.

This plugin will be usefull if you need to play a custom music disc, goat horn or player head initially created with AudioPlayer, then ported to a CustomDiscs compliant server.

This initial release doesn't do an automatic conversion, it'll need the player input to request the conversion to take place.
Also, no permission system is in place for now.

---

## How to use
Use of this plugin is as simple as :

- Take the item you want to convert in your main hand
- In chat, use ```/ap2cd convert```

Special note : This plugin will not check if the corresponding audio file exist, ensure the following :
- Original AudioPlayer music files located inside ```audio_player_data/``` should be placed inside ```plugins/CustomDiscs/musicdata/```

By default, as no check are taking place, the file extension will be set to .mp3.

If the original file is a .wav, after running the ```/ap2cd convert``` command, you can execute ```/ap2cd setwav```

The other way is also available with ```/ap2cd setmp3```

---

## Versions support matrix :

| Minecraft version                          | Server type     | Compatible versions | Latest compatible<br>version                                   |
|--------------------------------------------|-----------------|---------------------|----------------------------------------------------------------|
| **1.21.7-9, 1.21.8**<br>**1.21.8**         | Paper<br>Folia  | 1.0.0<br>1.0.0      | [1.0.0](https://github.com/Athar42/ap2cd/releases/tag/v1.0.0)  |
| **1.21.9, 1.21.10**<br>**1.21.9, 1.21.10** | Paper<br>Folia  | 1.0.0<br>1.0.0      | [1.0.0](https://github.com/Athar42/ap2cd/releases/tag/v1.0.0)  |

---

## Final notes

This plugin may **(or may not)** work with earlier versions of PaperMC/Folia.