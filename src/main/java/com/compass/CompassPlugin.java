package com.compass;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
		name = "Compass Directions"
)
public class CompassPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CompassOverlay compassOverlay;

	@Override
	protected void startUp()
	{
		overlayManager.add(compassOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(compassOverlay);
	}

	@Provides
	CompassConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompassConfig.class);
	}
}
