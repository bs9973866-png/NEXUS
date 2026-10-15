package dev.nexus.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class NexusClient implements ClientModInitializer {
    private static KeyBinding openMenuKey;
    private static final Identifier UI_FONT = Identifier.of("minecraft", "uniform");

    @Override
    public void onInitializeClient() {
        NexusData.initialize();
        SkinLibrary.initialize();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexus.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
                int buttonWidth = 86;
                int buttonHeight = 20;
                int x = Math.max(4, scaledWidth - buttonWidth - 8);
                int y = 8;
                Screens.getButtons(screen).add(new NexusButton(x, y, buttonWidth, buttonHeight, "NEXUS", () ->
                        client.setScreen(new NexusScreen(screen))));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new NexusScreen());
            }
        });
    }

    public static Text uiText(String value) {
        return Text.literal(value).styled(style -> style.withFont(new StyleSpriteSource.Font(UI_FONT)));
    }
}
