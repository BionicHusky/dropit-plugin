package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.time.Instant;

@Slf4j
@PluginDescriptor(
		name = "Drop It",
		description = "A Twitch chat integration plugin that enforces a drop your weapon penalty game.",
		tags = {"twitch", "integration", "penalty", "streamer"}
)
public class DropItPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread; // <--- Here is the magical thread manager!

	@Inject
	private DropItConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DropItOverlay overlay;

	@Inject
	private OkHttpClient okHttpClient;

	private WebSocket webSocket;

	// State Variables for the Overlay
	@Getter
	private boolean warningActive = false;
	@Getter
	private int warningTimer = 0;

	@Getter
	private boolean penaltyActive = false;
	@Getter
	private int penaltyTimer = 0;

	private Instant lastSecondUpdate = null;

	@Provides
	DropItConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DropItConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		connectToTwitch();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		disconnectFromTwitch();
		resetTimers();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Reconnect if the streamer changes the Twitch Channel in settings
		if (event.getGroup().equals("dropit") && event.getKey().equals("twitchChannel"))
		{
			connectToTwitch();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// Manage the real-time seconds for the timers
		if (lastSecondUpdate != null && Instant.now().isAfter(lastSecondUpdate.plusSeconds(1)))
		{
			lastSecondUpdate = Instant.now();

			if (warningActive)
			{
				warningTimer--;
				if (warningTimer <= 0)
				{
					warningActive = false;
					penaltyActive = true;
					penaltyTimer = 60; // Start the 60 second penalty
				}
			}
			else if (penaltyActive)
			{
				penaltyTimer--;
				if (penaltyTimer <= 0)
				{
					resetTimers(); // Penalty is over!
				}
			}
		}
	}

	private void connectToTwitch()
	{
		disconnectFromTwitch();

		String channel = config.twitchChannel();
		if (channel == null || channel.isEmpty())
		{
			return;
		}

		String finalChannel = channel.toLowerCase();
		Request request = new Request.Builder()
				.url("wss://irc-ws.chat.twitch.tv:443")
				.build();

		webSocket = okHttpClient.newWebSocket(request, new WebSocketListener()
		{
			@Override
			public void onOpen(WebSocket webSocket, Response response)
			{
				// Connect as an anonymous JustinFan user
				webSocket.send("PASS SCHMOOPIIE");
				int randomNum = (int) (Math.random() * 99999) + 10000;
				webSocket.send("NICK justinfan" + randomNum);
				webSocket.send("JOIN #" + finalChannel);
				log.info("Drop It connected to Twitch channel: " + finalChannel);
			}

			@Override
			public void onMessage(WebSocket webSocket, String text)
			{
				// Twitch requires an automated ping/pong to keep the connection alive
				if (text.startsWith("PING"))
				{
					webSocket.send("PONG :tmi.twitch.tv");
					return;
				}

				// If a chat message comes through
				if (text.contains("PRIVMSG #" + finalChannel + " :"))
				{
					int exclamationIndex = text.indexOf("!");
					if (exclamationIndex != -1 && text.startsWith(":"))
					{
						String sender = text.substring(1, exclamationIndex);
						String message = text.substring(text.indexOf(":", text.indexOf("PRIVMSG")) + 1).trim();

						// Check for the trigger command
						if (message.equalsIgnoreCase("!dropit"))
						{
							handleDropItCommand(sender);
						}
					}
				}
			}
		});
	}

	private void disconnectFromTwitch()
	{
		if (webSocket != null)
		{
			webSocket.close(1000, "Plugin stopped or config changed");
			webSocket = null;
		}
	}

	private void handleDropItCommand(String sender)
	{
		String allowedBot = config.allowedBotName();
		// Check whitelist if the streamer configured one
		if (allowedBot != null && !allowedBot.isEmpty())
		{
			if (!sender.equalsIgnoreCase(allowedBot.trim()))
			{
				return;
			}
		}

		// Trigger the Warning State
		warningActive = true;
		warningTimer = 5;
		penaltyActive = false;
		penaltyTimer = 0;
		lastSecondUpdate = Instant.now();

		// Play the Armadyl Crossbow sound using RuneLite's actual ClientThread
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.playSoundEffect(2277);
			}
		});
	}

	private void resetTimers()
	{
		warningActive = false;
		warningTimer = 0;
		penaltyActive = false;
		penaltyTimer = 0;
		lastSecondUpdate = null;
	}
}