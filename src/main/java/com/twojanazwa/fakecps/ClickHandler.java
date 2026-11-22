package com.twojanazwa.fakecps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.Random;

public class ClickHandler {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();
    private Field rightClickDelayTimerField;
    private int displayedCps = 0;
    private long lastCpsUpdate = 0;

    public ClickHandler() {
        try {
            rightClickDelayTimerField = ReflectionHelper.findField(Minecraft.class, "rightClickDelayTimer", "field_71467_ac");
            rightClickDelayTimerField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return;

        if (!FakeCpsMod.enabled || mc.currentScreen != null) {
            displayedCps = 0;
            return;
        }

        boolean isHoldingRightClick = Mouse.isButtonDown(1);
        boolean isHoldingBlock = mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;

        if (isHoldingRightClick && isHoldingBlock) {
            try {
                if (rightClickDelayTimerField != null) {
                    rightClickDelayTimerField.setInt(mc, 0);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - lastCpsUpdate > 100) {
                int jitter = random.nextInt(3) - 1;
                displayedCps = Math.max(1, FakeCpsMod.targetCps + jitter);
                lastCpsUpdate = System.currentTimeMillis();
            }
        } else {
            displayedCps = 0;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!FakeCpsMod.enabled || !FakeCpsMod.showHud || mc.gameSettings.showDebugInfo) return;

        FontRenderer fr = mc.fontRendererObj;
        String text = "CPS: " + displayedCps;
        fr.drawStringWithShadow(text, 10, 10, 0xFFFFFF);
    }
}
