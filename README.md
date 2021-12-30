## Table of contents

* [LabyMod Emote NPC](#labymod-emote-npc)
  * [config.yml](#configyml)
  * [Commands](#commands)
  * [Permissions](#permissions)
  * [Example Pictures](#example-pictures)

------------

# LabyMod Emote NPC

This plugin is there to create a NPC which can play all LabyMod Emotes. The played Emote can be choosen in a GUI with all Emotes (to open the GUI you have to right click the NPC, but after a reload you have to rejoin). The skin of the NPC is always the skin you have yourself.

------------
###### config.yml
```yaml
prefix: "&6EmoteNPC &8|"

inventory:
  title: "&8Emotes"
  pages: "&aPage %page% of %max%"
  next-page: "&6next page"
  last-page: "&6previous page"
  stop-emote: "&cstop emote"

settings:
  look-close: true
  sneak: true

npc:
  name:
    prefix: ""
    name: "&bEmotes"
    suffix: ""
  world: "world"
  x: 0.5
  y: 0.0
  z: 0.5
  yaw: 0.0
  pitch: 0.0

messages:
  play-emote: "%prefix% &7The &e%emote% Emote &7will now be played!"
  reload: "%prefix% &7The config was &asuccessfully &7reloaded!"
  set:
    location: "%prefix% &7The location of the NPC was set &asuccessfully&7!"
  toggle:
    look-close:
      enabled: "%prefix% &7The NPC now always looks at the player!"
      disabled: "%prefix% &7The NPC now always looks the direction specified in the &econfig.yml&7!"
    sneak:
      enabled: "%prefix% &7The NPC now sneaks whenever the player sneaks!"
      disabled: "%prefix% &7The NPC no longer sneaks when the player sneaks!"
  error:
    labymod: "%prefix% &cYou have to use LabyMod! &ohttps://labymod.net/download"
    only-player: "%prefix% &cYou have to be a player to execute this command!"
    permission: "%prefix% &cInsufficient permissions!"
  help:
    set:
      location: "%prefix% &e/emote set location &8> &7sets the location of the NPC to your current location"
    toggle:
      look-close: "%prefix% &e/emote toggle lookclose &8> &7switches whether the NPC looks at the player all the time"
      sneak: "%prefix% &e/emote toggle sneaking &8> &7switches whether the NPC is sneaking when the player is sneaking"
    reload: "%prefix% &e/emote reload &8> &7reload the config"

debug: false
```
------------
###### Commands
    /emote reload - to reload the config.yml
    /emote set location - sets the location of the NPC to your current location
    /emote toggle lookclose - toggles whether the NPC looks at the player or in the direction of the config.yml
    /emote toggle sneaking - toggles whether the NPC sneaks when the players sneaks

------------
###### Permissions
    /emote - 'emote.command'
    /emote reload - 'emote.command.reload'
    /emote set location - 'emote.command.set'
    /emote toggle lookclose - 'emote.command.toggle'
    /emote toggle sneaking- 'emote.command.toggle'

------------
###### Example Pictures
![the GUI to choose the Emote](https://i.ibb.co/5jsN3Nn/2021-11-23-17-44-40.png "the GUI to choose the Emote")
*the GUI to choose the Emote*

![the Emote NPC](https://i.ibb.co/dLrSJqZ/2021-11-23-17-44-23.png "the Emote NPC")
*the Emote NPC*
