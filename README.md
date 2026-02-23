# Drop It! 🤡

A chaotic Twitch-integrated RuneLite plugin that forces players to unequip their weapon when chat commands it, or suffer the consequences.

## How It Works
When a viewer triggers the `!dropit` command in Twitch chat:
1. A deafening Armadyl Crossbow sound effect plays.
2. A massive, flashing red "PANIC MODE" timer takes over the screen for 5 seconds.
3. If the player fails to unequip their weapon before the 5 seconds are up, a giant 🤡 emoji completely blocks their inventory.
4. The clown remains active for **60 seconds**. If the player re-equips a weapon at any point during that minute, the clown instantly returns!

## Streamer Setup (The Bot Bridge)
To prevent chat trolls from spamming the alarm, this plugin uses a secure "Bot Bridge" whitelist.

**How to link this to Channel Points:**
1. Create a custom Channel Point reward on Twitch (e.g., "Drop It!").
2. Open your stream bot (Nightbot, StreamElements, MixItUp, etc.).
3. Create a command/rule in your bot: Whenever the "Drop It!" reward is redeemed, have the bot type `!dropit` in your Twitch chat.
4. In the RuneLite plugin settings, enter the name of your bot in the **Allowed Bot Name** field.

Now, only your bot (triggered by channel points) or you (the broadcaster) can trigger the panic mode!

## Plugin Settings
* **Twitch Channel:** The exact username of the Twitch channel to monitor (e.g., `jape94`).
* **Allowed Bot Name:** The exact username of the bot authorized to trigger the command (e.g., `streamelements`). Leave blank to disable the security whitelist (not recommended).
