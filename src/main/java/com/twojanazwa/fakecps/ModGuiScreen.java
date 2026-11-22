package com.twojanazwa.fakecps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ModGuiScreen extends GuiScreen {

    private GuiButton toggleModButton;
    private GuiButton toggleHudButton;
    private GuiButton minusCpsButton;
    private GuiButton plusCpsButton;
    private GuiButton bindKeyButton;

    private boolean isBindingKey = false;

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Przycisk Włącz/Wyłącz moda
        toggleModButton = new GuiButton(1, centerX - 100, centerY - 55, 200, 20, getModStatusText());
        this.buttonList.add(toggleModButton);

        // Przyciski zmiany Fake CPS (- oraz +)
        minusCpsButton = new GuiButton(2, centerX - 100, centerY - 25, 40, 20, "-");
        plusCpsButton = new GuiButton(3, centerX + 60, centerY - 25, 40, 20, "+");
        this.buttonList.add(minusCpsButton);
        this.buttonList.add(plusCpsButton);

        // Przycisk bindowania klawisza
        bindKeyButton = new GuiButton(4, centerX - 100, centerY + 5, 200, 20, getBindButtonText());
        this.buttonList.add(bindKeyButton);

        // Przycisk Pokaż/Ukryj licznik na ekranie
        toggleHudButton = new GuiButton(5, centerX - 100, centerY + 35, 200, 20, getHudStatusText());
        this.buttonList.add(toggleHudButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (isBindingKey) return;

        switch (button.id) {
            case 1:
                FakeCpsMod.enabled = !FakeCpsMod.enabled;
                toggleModButton.displayString = getModStatusText();
                NotificationRenderer.triggerNotification(FakeCpsMod.enabled);
                break;
            case 2:
                if (FakeCpsMod.targetCps > 1) {
                    FakeCpsMod.targetCps--;
                }
                break;
            case 3:
                if (FakeCpsMod.targetCps < 100) {
                    FakeCpsMod.targetCps++;
                }
                break;
            case 4:
                isBindingKey = true;
                bindKeyButton.displayString = "\u00A7e> Nacisnij dowolny klawisz (ESC = brak) <";
                break;
            case 5:
                FakeCpsMod.showHud = !FakeCpsMod.showHud;
                toggleHudButton.displayString = getHudStatusText();
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isBindingKey) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                FakeCpsMod.toggleKeyCode = Keyboard.KEY_NONE;
            } else {
                FakeCpsMod.toggleKeyCode = keyCode;
            }
            isBindingKey = false;
            bindKeyButton.displayString = getBindButtonText();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.drawCenteredString(this.fontRendererObj, "\u00A7lUstawienia FastPlace & Fake CPS", centerX, centerY - 82, 0x55FF55);
        this.drawCenteredString(this.fontRendererObj, "Fake CPS: " + FakeCpsMod.targetCps, centerX, centerY - 19, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private String getModStatusText() {
        return "Status FastPlace: " + (FakeCpsMod.enabled ? "\u00A7aWLACZONY" : "\u00A7cWYLACZONY");
    }

    private String getBindButtonText() {
        String keyName = (FakeCpsMod.toggleKeyCode == Keyboard.KEY_NONE)
                ? "BRAK"
                : Keyboard.getKeyName(FakeCpsMod.toggleKeyCode);
        return "Bind klawisza: \u00A7d[" + keyName + "]";
    }

    private String getHudStatusText() {
        return "Licznik CPS na ekranie: " + (FakeCpsMod.showHud ? "\u00A7aWIDOCZNY" : "\u00A7cUKRYTY");
    }
}
