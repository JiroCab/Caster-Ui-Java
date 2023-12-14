# Caster UI 

- A Mindustry Java mod that aims to make the client a bit better for casting!
- Java Rewite of [JiroCab/Caster-Ui](https://github.com/JiroCab/Caster-Ui) That was based on ['Ferlern/extended-UI'](https://github.com/ferlern/extended-ui-v7) **use said mods at the same with caution as it may break in a inglorious fashion**

# Features
Everything can be Toggles on or off From "Setting > Caster Ui Setting"

### - Unit counter
- Displays all units per team sorted base on the Core database order, Size of the table can be changed (On by default)
- Separating per team can be toggled for better readability or compactness (On by default)

### - Player tracker
- Displays all players on the server, Payers without units will be hidden (On by default)
- Tap on a player's name or sprite to spectate them
- Hold `H` to Track their cursor instead, can be changed to a toggle
- Use `;` & `'` cycle between players

![player and unit table](https://user-images.githubusercontent.com/57391931/188985548-624af3a4-959a-4416-9a60-c1e5f2154f47.PNG)

### - Core Lost notifications
1. Draw Circle on a core's death (On by default, speed & style can be changed)
2. Get a Toast on a core's death (On by default)
3. Send a message in chat on a Core death (Off by default)
4. You Press `G` to snap your camera to the last core death

![Core-Lost-Notification](https://user-images.githubusercontent.com/57391931/180737689-a11c7c35-9cae-4c49-8681-2ee338827b68.gif)

### - Tracks other players' cursor
- 7 Cursor types, use the slider in the settings to change it
- Client's Mouse can also be show for cases where system cursor does not show and use this as a backup
  ![Mouse Cursors](https://user-images.githubusercontent.com/57391931/183559978-f4bf81ae-b57d-44d0-b911-6b5ec15e2811.png)
- Use `;` & `'` to cycle between players (can be rebinded)

### - Shows additional information about enemy blocks
- Note: this has been moved to the left Center instead of the bottom left corner
- show health of hovered blocks - for no reason other than be a less cool version of [deltanedas/waisa](https://github.com/deltanedas/waisa) 
  ![enemy-resources](https://cdn.discordapp.com/attachments/606977691757051920/953751760273543238/unknown.png)
  ![enemy-power](https://cdn.discordapp.com/attachments/606977691757051920/953751888044625991/unknown.png)
  Note screenshots are updated

### - Logic & Factory Rally point  Tracker
- Displays logic block's controlled units
- Hover on a Factory to see its rally point (affected by `Show Block info`)
- Transparency can be changed
![logic-line](https://cdn.discordapp.com/attachments/606977691757051920/954039066305888326/unknown.png)

### - ~~Factory progress bar~~ **(Not implemented)**
  ![progress-bar](https://cdn.discordapp.com/attachments/606977691757051920/951186180895023165/unknown.png)

### - ~~Unit health & shield bars (takes a lot of fps)~~  **(Not implemented)**
  ![health-shield-bar](https://cdn.discordapp.com/attachments/606977691757051920/951889454824579092/unknown.png)

# Building The Mod
It's the same with any other mod.

All you need to know is '`gradlew jar`, `gradlew deploy`
And `runMindustryClient `, `runMindustryServer ` to test it with [Toxopid](https://github.com/Xpdustry/Toxopid)

# Special thanks
- [Capitaine Echo](https://www.twitch.tv/capitaine_echo) - For Play testing the mod
- [Wizer/Ferlern](https://github.com/Ferlern) - for the orginal [Extended-ui mod](https://github.com/Ferlern/extended-UI-v7) which what the original Caster User-Interface is based on
- [xzxADIxzx](https://github.com/xzxADIxzx) - for [Scheme-Size](https://github.com/xzxADIxzx/Scheme-Size) as some features & code are base on / from it