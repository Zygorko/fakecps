package com.twojanazwa.fakecps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = FakeCpsMod.MODID, version = FakeCpsMod.VERSION, clientSideOnly = true)
public class FakeCpsMod {
    public static final String MODID = "fakecpsmod";
    public static final String VERSION = "1.1";

    // Ustawienia moda
    public static boolean enabled = true;
    public static boolean showHud = true;
    public static int targetCps = 18;
    public static int toggleKeyCode = Keyboard.KEY_X; // Domyślny bind na FastPlace (jak na screenie: X)

    private KeyBinding openGuiKey;
    private boolean wasToggleKeyDown = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Klawisz otwierania GUI pod 'U'
        openGuiKey = new KeyBinding("Otworz menu FakeCPS", Keyboard.KEY_U, "FakeCPS Mod");
        ClientRegistry.registerKeyBinding(openGuiKey);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ClickHandler());
        MinecraftForge.EVENT_BUS.register(new NotificationRenderer());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        // Otwieranie GUI pod klawiszem U
        if (openGuiKey.isPressed()) {
            mc.displayGuiScreen(new ModGuiScreen());
            return;
        }

        // Obsługa zbindowanego klawisza do włączania/wyłączania FastPlace
        if (toggleKeyCode != Keyboard.KEY_NONE) {
            boolean isDown = Keyboard.isKeyDown(toggleKeyCode);
            if (isDown && !wasToggleKeyDown) {
                enabled = !enabled;
                NotificationRenderer.triggerNotification(enabled);
            }
            wasToggleKeyDown = isDown;
        }
    }
}
