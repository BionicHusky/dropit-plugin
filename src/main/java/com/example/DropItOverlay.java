package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class DropItOverlay extends Overlay
{
    private final Client client;
    private final DropItPlugin plugin;

    @Inject
    private DropItOverlay(Client client, DropItPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.isWarningActive())
        {
            renderWarning(graphics);
        }
        else if (plugin.isPenaltyActive())
        {
            renderPenalty(graphics);
        }
        return null;
    }

    private void renderWarning(Graphics2D graphics)
    {
        // 1. Draw the pulsing red screen
        long time = System.currentTimeMillis();
        int alpha = (int) (100 + 100 * Math.sin(time / 150.0)); // Math for the pulse effect
        graphics.setColor(new Color(255, 0, 0, Math.max(0, Math.min(255, alpha))));
        graphics.fill(new Rectangle(0, 0, client.getCanvasWidth(), client.getCanvasHeight()));

        // 2. Draw the giant countdown timer in the middle of the screen
        String text = String.valueOf(plugin.getWarningTimer());
        graphics.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics metrics = graphics.getFontMetrics();
        int x = (client.getCanvasWidth() - metrics.stringWidth(text)) / 2;
        int y = (client.getCanvasHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

        // Add a black drop-shadow to the text so it's readable anywhere
        graphics.setColor(Color.BLACK);
        graphics.drawString(text, x + 4, y + 4);

        // Draw the white text
        graphics.setColor(Color.WHITE);
        graphics.drawString(text, x, y);
    }

    private void renderPenalty(Graphics2D graphics)
    {
        // Check if the player currently has a weapon equipped
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return;
        }

        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());

        // If they have a weapon equipped during the penalty phase...
        if (weapon != null && weapon.getId() > 0)
        {
            Widget invWidget = client.getWidget(WidgetInfo.INVENTORY);
            if (invWidget != null && !invWidget.isHidden())
            {
                // 1. Draw the blackout box over the inventory
                Rectangle bounds = invWidget.getBounds();
                graphics.setColor(Color.BLACK);
                graphics.fill(bounds);

                // 2. Draw the giant clown emoji
                String text = "🤡";
                graphics.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                FontMetrics metrics = graphics.getFontMetrics();
                int x = bounds.x + (bounds.width - metrics.stringWidth(text)) / 2;
                int y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();

                graphics.setColor(Color.WHITE);
                graphics.drawString(text, x, y);
            }
        }
    }
}