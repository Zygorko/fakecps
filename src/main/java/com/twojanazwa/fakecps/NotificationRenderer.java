package com.twojanazwa.fakecps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class NotificationRenderer {
    private static long notifyStartTime = 0;
    private static boolean notifyState = false;
    private static final long DURATION_MS = 2000; // Czas wyświetlania powiadomienia (2 sekundy)

    public static void triggerNotification(boolean state) {
        notifyState = state;
        notifyStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRenderNotification(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (notifyStartTime == 0) return;

        long elapsed = System.currentTimeMillis() - notifyStartTime;
        if (elapsed > DURATION_MS) {
            notifyStartTime = 0;
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        int boxWidth = 172;
        int boxHeight = 42;

        // Płynna animacja wysuwania się z dołu
        float animProgress = 1.0f;
        if (elapsed < 200) {
            animProgress = elapsed / 200.0f;
        } else if (elapsed > DURATION_MS - 250) {
            animProgress = (DURATION_MS - elapsed) / 250.0f;
        }
        // Ease-out
        animProgress = 1.0f - (float) Math.pow(1.0f - Math.max(0.0f, Math.min(1.0f, animProgress)), 3);

        int x = (sr.getScaledWidth() - boxWidth) / 2;
        int targetY = sr.getScaledHeight() - 72;
        int y = (int) (sr.getScaledHeight() - (sr.getScaledHeight() - targetY) * animProgress);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // 1. Zewnętrzna obwódka (ciemnoszara) + główne zaokrąglone tło (#1E1E24)
        drawRoundedRect(x - 1, y - 1, boxWidth + 2, boxHeight + 2, 10, 0xFF33333D);
        drawRoundedRect(x, y, boxWidth, boxHeight, 9, 0xF21C1C22);

        // 2. Koło statusu po lewej (Zielone #2ECC71 lub Czerwone #FF3B3B)
        int circleCenterX = x + 22;
        int circleCenterY = y + 20;
        int circleColor = notifyState ? 0xFF2ECC71 : 0xFFFF3B3B;
        drawCircle(circleCenterX, circleCenterY, 11.5f, circleColor);

        // 3. Kafelek z klawiszem po prawej stronie (#32323D)
        String keyName = (FakeCpsMod.toggleKeyCode == Keyboard.KEY_NONE) ? "-" : Keyboard.getKeyName(FakeCpsMod.toggleKeyCode);
        int badgeWidth = Math.max(24, fr.getStringWidth(keyName) + 14);
        int badgeHeight = 22;
        int badgeX = x + boxWidth - badgeWidth - 11;
        int badgeY = y + (boxHeight - badgeHeight) / 2;
        drawRoundedRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 0xFF32323D);

        // 4. Dolny pasek postępu (odliczanie czasu znikania)
        float timeRatio = 1.0f - ((float) elapsed / (float) DURATION_MS);
        int barMaxWidth = boxWidth - 24;
        int currentBarWidth = (int) (barMaxWidth * timeRatio);
        if (currentBarWidth > 2) {
            drawRoundedRect(x + 12, y + boxHeight - 5, currentBarWidth, 2, 1, circleColor);
        }

        GlStateManager.enableTexture2D();

        // 5. Biała ikona ptaszka (✓) lub krzyżyka (✕) wewnątrz koła
        String symbol = notifyState ? "\u2714" : "\u2716";
        fr.drawString(symbol, circleCenterX - fr.getStringWidth(symbol) / 2, circleCenterY - 4, 0xFFFFFFFF);

        // 6. Tekst "FastPlace" (pogrubiony biały) i status ("Włączony" / "Wyłączony")
        int textX = x + 42;
        fr.drawStringWithShadow("\u00A7lFastPlace", textX, y + 9, 0xFFFFFFFF);
        String statusText = notifyState ? "W\u0142\u0105czony" : "Wy\u0142\u0105czony";
        fr.drawString(statusText, textX, y + 21, circleColor);

        // 7. Fioletowy tekst klawisza wewnątrz prawego kafelka (#9B6BFF)
        fr.drawString("\u00A7l" + keyName, badgeX + (badgeWidth - fr.getStringWidth("\u00A7l" + keyName)) / 2, badgeY + 7, 0xFF9B6BFF);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    // Rysowanie zaokrąglonego prostokąta w OpenGL
    private void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_POLYGON);
        int segments = 16;
        // Prawy dolny
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 0.5 * i / segments;
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius, y + height - radius + Math.sin(angle) * radius);
        }
        // Lewy dolny
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 0.5 + Math.PI * 0.5 * i / segments;
            GL11.glVertex2d(x + radius + Math.cos(angle) * radius, y + height - radius + Math.sin(angle) * radius);
        }
        // Lewy górny
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI + Math.PI * 0.5 * i / segments;
            GL11.glVertex2d(x + radius + Math.cos(angle) * radius, y + radius + Math.sin(angle) * radius);
        }
        // Prawy górny
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 1.5 + Math.PI * 0.5 * i / segments;
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius, y + radius + Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    // Rysowanie gładkiego koła w OpenGL
    private void drawCircle(float cx, float cy, float r, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        GL11.glColor4f(red, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            GL11.glVertex2d(cx + Math.cos(angle) * r, cy + Math.sin(angle) * r);
        }
        GL11.glEnd();
    }
}
