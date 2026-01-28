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
    // Known packed widget IDs for the compass across layouts
    private static final int[] COMPASS_WIDGET_IDS =
            {
                    10747935, // Resizable Modern
                    10551327, // Resizable Classic
                    35913752  // Fixed Classic
            };

    // 2048 units == 360 degrees
    private static final double FULL_ROTATION_UNITS = 2048.0;
    private static final double UNITS_TO_RADIANS = (2.0 * Math.PI) / FULL_ROTATION_UNITS;

    // Slider neutral midpoints (non-negative UI → signed offset)
    private static final int SHIFT_NEUTRAL_0_TO_40 = 20;
    private static final int RADIUS_NEUTRAL_0_TO_30 = 15;

    private final Client client;
    private final CompassConfig config;

    // Cache: last working compass widget id (0 = unknown)
    private int cachedCompassWidgetId = 0;

    // Cache: last font size applied, so we only deriveFont when it changes
    private int lastFontSize = -1;
    private Font cachedFont = null;

    // Cache: throttle rescans so we don't loop IDs every frame
    private long lastRescanMillis = 0L;
    private static final long RESCAN_INTERVAL_MS = 2000L;

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
        final Widget compass = getCompassWidget();
        if (compass == null || compass.isHidden())
        {
            return null;
        }

        final Rectangle bounds = compass.getBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
        {
            return null;
        }

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // Font size (avoid deriveFont every frame)
        final int fontSize = clamp(config.fontSize(), 8, 24);
        if (fontSize != lastFontSize || cachedFont == null)
        {
            cachedFont = graphics.getFont().deriveFont(Font.PLAIN, (float) fontSize);
            lastFontSize = fontSize;
        }
        graphics.setFont(cachedFont);

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

        // Base map rotation (in radians)
        final double baseRot = client.getMapAngle() * UNITS_TO_RADIANS;

        final Color textColor = config.textColor();

        // Per-direction fine rotation correction
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

    /**
     * Find the compass widget in a layout-safe way.
     * Uses a cached widget id when possible, and rescans periodically.
     */
    private Widget getCompassWidget()
    {
        // 1) Try cached id first
        if (cachedCompassWidgetId != 0)
        {
            Widget w = client.getWidget(cachedCompassWidgetId);
            if (isUsableCompassWidget(w))
            {
                return w;
            }
        }

        // 2) Throttle rescans (don’t loop every frame if widgets are null during login/loading)
        final long now = System.currentTimeMillis();
        if (now - lastRescanMillis < RESCAN_INTERVAL_MS)
        {
            return null;
        }
        lastRescanMillis = now;

        // 3) Scan known IDs
        for (int id : COMPASS_WIDGET_IDS)
        {
            Widget w = client.getWidget(id);
            if (isUsableCompassWidget(w))
            {
                cachedCompassWidgetId = id;
                return w;
            }
        }

        // still not found
        cachedCompassWidgetId = 0;
        return null;
    }

    private static boolean isUsableCompassWidget(Widget w)
    {
        if (w == null || w.isHidden())
        {
            return false;
        }

        Rectangle b = w.getBounds();
        return b != null && b.width > 0 && b.height > 0;
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
