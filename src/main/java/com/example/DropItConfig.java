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
			description = "The exact name of the Twitch channel to monitor (e.g., bionichusky)",
			position = 1
	)
	default String twitchChannel()
	{
		return "";
	}

	@ConfigItem(
			keyName = "allowedBotName",
			name = "Allowed Bot Name (Optional)",
			description = "If filled in, ONLY messages from this exact user/bot will trigger the penalty (e.g., nightbot). Leave blank to allow anyone.",
			position = 2
	)
	default String allowedBotName()
	{
		return "";
	}
}