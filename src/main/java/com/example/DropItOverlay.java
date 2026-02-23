package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

public class DropItOverlay extends Overlay {
    private final Client client;
    private final DropItPlugin plugin;

    @Inject
    private DropItOverlay(Client client, DropItPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        long currentTime = System.currentTimeMillis();

        if (plugin.isPanicMode) {
            long timeLeft = plugin.panicEndTime - currentTime;

            if (timeLeft <= 0) {
                plugin.isPanicMode = false;
            } else {
                int seconds = (int) Math.ceil(timeLeft / 1000.0);

                int alpha = (int) (75 + (75 * Math.sin(currentTime / 100.0)));
                graphics.setColor(new Color(255, 0, 0, alpha));
                graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());

                String text = "PANIC MODE: " + seconds + "s";
                graphics.setFont(new Font("Arial", Font.BOLD, 48));
                graphics.setColor(Color.RED);

                int x = client.getCanvasWidth() / 2 - 200;
                int y = client.getCanvasHeight() / 2 - 100;
                graphics.drawString(text, x, y);
            }
        }

        if (!plugin.isPanicMode && currentTime < plugin.penaltyEndTime) {

            ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
            boolean hasWeapon = false;

            if (equipment != null) {
                Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
                if (weapon != null && weapon.getId() > 0) {
                    hasWeapon = true;
                }
            }

            if (hasWeapon) {
                Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

                if (inventory != null && !inventory.isHidden()) {
                    Rectangle bounds = inventory.getBounds();

                    graphics.setColor(new Color(0, 0, 0, 230));
                    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

                    graphics.setColor(Color.WHITE);
                    graphics.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));

                    int emojiX = bounds.x + (bounds.width / 2) - 65;
                    int emojiY = bounds.y + (bounds.height / 2) + 40;

                    graphics.drawString("🤡", emojiX, emojiY);
                }
            }
        }
        return null;
    }
}