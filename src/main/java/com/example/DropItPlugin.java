package com.example;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Drop It"
)
public class DropItPlugin extends Plugin
{
	@Inject
	private DropItConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DropItOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private TwitchClient twitchClient;

	public volatile boolean isPanicMode = false;
	public volatile long panicEndTime = 0;
	public volatile long penaltyEndTime = 0;

	@Override
	protected void startUp() throws Exception
	{
		System.out.println("Drop It started! Waiting for Twitch channel...");
		overlayManager.add(overlay);
		connectToTwitch();
	}

	@Override
	protected void shutDown() throws Exception
	{
		System.out.println("Drop It stopped! Disconnecting from Twitch...");
		overlayManager.remove(overlay);
		isPanicMode = false;

		if (twitchClient != null) {
			twitchClient.close();
			twitchClient = null;
		}
	}

	private void connectToTwitch() {
		if (config.twitchChannel().isEmpty()) {
			return;
		}

		twitchClient = TwitchClientBuilder.builder()
				.withEnableChat(true)
				.build();

		twitchClient.getChat().joinChannel(config.twitchChannel());

		twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {

			if (event.getMessage().equalsIgnoreCase("!dropit")) {

				String senderName = event.getUser().getName();
				String allowedBot = config.botUsername();
				String broadcaster = config.twitchChannel();

				if (senderName.equalsIgnoreCase(allowedBot) || senderName.equalsIgnoreCase(broadcaster)) {
					isPanicMode = true;
					long currentTime = System.currentTimeMillis();

					panicEndTime = currentTime + 5000;
					penaltyEndTime = panicEndTime + 60000;

					clientThread.invokeLater(() -> client.playSoundEffect(2277));

					System.out.println("🚨 PANIC TRIGGERED! 60-Second Clown Rule Active! 🚨");
				}
			}
		});
	}

	@Provides
	DropItConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DropItConfig.class);
	}
}