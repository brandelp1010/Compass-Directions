package com.compass;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("compass")
public interface CompassConfig extends Config
{
	// --------------------
	// Sections
	// --------------------

	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 0
	)
	String generalSection = "general";

	@ConfigSection(
			name = "Offsets",
			description = "Position and radius adjustments",
			position = 1
	)
	String offsetSection = "offsets";

	@ConfigSection(
			name = "Rotation",
			description = "Per-direction rotation adjustments",
			position = 2
	)
	String rotationSection = "rotation";

	// --------------------
	// General
	// --------------------

	@ConfigItem(
			keyName = "showNorth",
			name = "Show North",
			description = "Draw N (disable if you want to keep the default compass N)",
			section = generalSection,
			position = 0
	)
	default boolean showNorth()
	{
		return false;
	}

	@ConfigItem(
			keyName = "textColor",
			name = "Text Color",
			description = "Color of NSEW letters",
			section = generalSection,
			position = 1
	)
	default Color textColor()
	{
		return Color.BLACK;
	}

	@Range(min = 8, max = 24)
	@ConfigItem(
			keyName = "fontSize",
			name = "Font Size",
			description = "Font size of NSEW letters",
			section = generalSection,
			position = 2
	)
	default int fontSize()
	{
		return 15;
	}

	// --------------------
	// Offsets (neutral slider mapping)
	// --------------------

	@Range(min = 0, max = 40)
	@ConfigItem(
			keyName = "offsetXShift",
			name = "Center X Shift",
			description = "0..40 maps to -20..+20 pixels (20 = neutral)",
			section = offsetSection,
			position = 20
	)
	default int offsetXShift()
	{
		return 20;
	}

	@Range(min = 0, max = 40)
	@ConfigItem(
			keyName = "offsetYShift",
			name = "Center Y Shift",
			description = "0..40 maps to -20..+20 pixels (20 = neutral)",
			section = offsetSection,
			position = 20
	)
	default int offsetYShift()
	{
		return 20;
	}

	@Range(min = 0, max = 30)
	@ConfigItem(
			keyName = "radiusShift",
			name = "Radius Shift",
			description = "0..30 maps to -15..+15 pixels (15 = neutral)",
			section = offsetSection,
			position = 15
	)
	default int radiusShift()
	{
		return 15;
	}

	// --------------------
	// Rotation (choose ONE style)
	// --------------------
	// Option A: allow negatives directly
	@Range(min = -30, max = 30)
	@ConfigItem(
			keyName = "northOffset",
			name = "North Offset (deg)",
			description = "Fine adjustment for North label rotation",
			section = rotationSection,
			position = 0
	)
	default int northOffset()
	{
		return 0;
	}

	@Range(min = -30, max = 30)
	@ConfigItem(
			keyName = "eastOffset",
			name = "East Offset (deg)",
			description = "Fine adjustment for East label rotation",
			section = rotationSection,
			position = 1
	)
	default int eastOffset()
	{
		return 3;
	}

	@Range(min = -30, max = 30)
	@ConfigItem(
			keyName = "southOffset",
			name = "South Offset (deg)",
			description = "Fine adjustment for South label rotation",
			section = rotationSection,
			position = 2
	)
	default int southOffset()
	{
		return 0;
	}

	@Range(min = -30, max = 30)
	@ConfigItem(
			keyName = "westOffset",
			name = "West Offset (deg)",
			description = "Fine adjustment for West label rotation",
			section = rotationSection,
			position = 3
	)
	default int westOffset()
	{
		return 0;
	}
}
