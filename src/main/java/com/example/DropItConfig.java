package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("dropit")
public interface DropItConfig extends Config
{
	@ConfigItem(
			keyName = "twitchChannel",
			name = "Twitch Channel",
			description = "The Twitch channel to listen to (e.g., jape94)"
	)
	default String twitchChannel()
	{
		return "";
	}

	@ConfigItem(
			keyName = "botUsername",
			name = "Allowed Bot Name",
			description = "The username of the bot allowed to trigger the command (e.g., streamelements)"
	)
	default String botUsername()
	{
		return "";
	}
}