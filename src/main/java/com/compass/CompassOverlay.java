package com.compass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class CompassOverlay extends Overlay
{
    // IF3 packed widget id from Widget Inspector
    private static final int COMPASS_WIDGET_ID = 10747935;

    // 2048 units == 360 degrees
    private static final double FULL_ROTATION_UNITS = 2048.0;

    // Slider neutral midpoints (non-negative UI → signed offset)
    private static final int SHIFT_NEUTRAL_0_TO_40 = 20;
    private static final int RADIUS_NEUTRAL_0_TO_30 = 15;

    private final Client client;
    private final CompassConfig config;

    @Inject
    private CompassOverlay(Client client, CompassConfig config)
    {
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        final Widget compass = client.getWidget(COMPASS_WIDGET_ID);
        if (compass == null || compass.isHidden())
        {
            return null;
        }

        final Rectangle bounds = compass.getBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
        {
            return null;
        }

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Font size
        final int fontSize = clamp(config.fontSize(), 8, 24);
        graphics.setFont(graphics.getFont().deriveFont(Font.PLAIN, (float) fontSize));

        // Convert non-negative sliders → signed offsets
        final int offsetX = config.offsetXShift() - SHIFT_NEUTRAL_0_TO_40; // [-20..+20]
        final int offsetY = config.offsetYShift() - SHIFT_NEUTRAL_0_TO_40; // [-20..+20]
        final int radiusPadding = config.radiusShift() - RADIUS_NEUTRAL_0_TO_30; // [-15..+15]

        // Center of compass + tuning offsets
        final int cx = bounds.x + (bounds.width / 2) + offsetX;
        final int cy = bounds.y + (bounds.height / 2) + offsetY;

        // Radius (tight + user padding)
        final int baseRadius = Math.min(bounds.width, bounds.height) / 2;
        final int radius = clamp(baseRadius + radiusPadding, 6, 100);

        // Base map rotation
        final double baseRot = client.getMapAngle() * (2.0 * Math.PI / FULL_ROTATION_UNITS);

        final Color textColor = config.textColor();

        // Apply per-direction rotation corrections
        if (config.showNorth())
        {
            drawDirection(graphics, "N", cx, cy, radius,
                    baseRot + Math.toRadians(config.northOffset()), textColor);
        }

        drawDirection(graphics, "E", cx, cy, radius,
                baseRot + (Math.PI / 2.0) + Math.toRadians(config.eastOffset()), textColor);

        drawDirection(graphics, "S", cx, cy, radius,
                baseRot + Math.PI + Math.toRadians(config.southOffset()), textColor);

        drawDirection(graphics, "W", cx, cy, radius,
                baseRot + (3.0 * Math.PI / 2.0) + Math.toRadians(config.westOffset()), textColor);

        return null;
    }

    private static void drawDirection(Graphics2D g, String text, int cx, int cy, int r, double theta, Color color)
    {
        final int x = (int) Math.round(cx + Math.sin(theta) * r);
        final int y = (int) Math.round(cy - Math.cos(theta) * r);

        final FontMetrics fm = g.getFontMetrics();
        final int tx = x - (fm.stringWidth(text) / 2);
        final int ty = y + (fm.getAscent() / 2);

        g.setColor(color);
        g.drawString(text, tx, ty);
    }

    private static int clamp(int v, int min, int max)
    {
        return Math.max(min, Math.min(max, v));
    }
}
