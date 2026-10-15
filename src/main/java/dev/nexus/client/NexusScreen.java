package dev.nexus.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class NexusScreen extends Screen {
    private enum Page { HOME, FRIENDS, WORLDS, VOICE, SKINS, EMOTES }
    private Page page = Page.HOME;
    private String notice = "Your social space starts here.";
    private int panelX, panelY, panelW, panelH;

    public NexusScreen() { super(Text.literal("NEXUS")); }

    @Override
    protected void init() {
        clearChildren();
        panelW = Math.min(520, width - 32);
        panelH = Math.min(340, height - 32);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        int navX = panelX + 18, navY = panelY + 66, navW = 112, navH = 25;
        addDrawableChild(nav("Overview", Page.HOME, navX, navY, navW, navH));
        addDrawableChild(nav("Friends", Page.FRIENDS, navX, navY + 31, navW, navH));
        addDrawableChild(nav("Worlds", Page.WORLDS, navX, navY + 62, navW, navH));
        addDrawableChild(nav("Voice", Page.VOICE, navX, navY + 93, navW, navH));
        addDrawableChild(nav("Skins", Page.SKINS, navX, navY + 124, navW, navH));
        addDrawableChild(nav("Emotes", Page.EMOTES, navX, navY + 155, navW, navH));
        int contentX = panelX + 150, contentW = panelW - 170, actionY = panelY + 245;
        if (page == Page.SKINS) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Import PNG"), b -> { notice = SkinLibrary.importSkin(); clearAndInit(); })
                    .dimensions(contentX, actionY, Math.min(140, contentW), 24).build());
        } else if (page == Page.WORLDS) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Hosting setup"), b -> notice = "Internet hosting needs a reachable relay/tunnel service. This build does not create a public tunnel yet.")
                    .dimensions(contentX, actionY, Math.min(160, contentW), 24).build());
        } else if (page == Page.VOICE) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Voice settings"), b -> notice = "Voice transport is not connected yet; no microphone audio is sent in this build.")
                    .dimensions(contentX, actionY, Math.min(160, contentW), 24).build());
        } else if (page == Page.FRIENDS) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Add friend"), b -> notice = "Friend requests require the NEXUS account/relay service, which is not connected yet.")
                    .dimensions(contentX, actionY, Math.min(140, contentW), 24).build());
        } else if (page == Page.EMOTES) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Wave"), b -> notice = "Emote preview selected. Network-synced player animations are not implemented yet.")
                    .dimensions(contentX, actionY, Math.min(100, contentW), 24).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(panelX + panelW - 82, panelY + panelH - 34, 64, 22).build());
    }

    private ButtonWidget nav(String label, Page target, int x, int y, int w, int h) {
        return ButtonWidget.builder(Text.literal((page == target ? "• " : "") + label), b -> { page = target; notice = "NEXUS • " + label; clearAndInit(); })
                .dimensions(x, y, w, h).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0x9907070D);
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF101018);
        context.fill(panelX, panelY, panelX + panelW, panelY + 3, 0xFFE34264);
        context.fill(panelX + 12, panelY + 12, panelX + 136, panelY + panelH - 12, 0xFF171720);
        context.fill(panelX + 145, panelY + 54, panelX + panelW - 14, panelY + panelH - 48, 0xFF15151E);
        context.drawTextWithShadow(textRenderer, Text.literal("NEXUS"), panelX + 18, panelY + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("PLAY TOGETHER. ANYWHERE."), panelX + 18, panelY + 36, 0xFFE76A84);
        context.drawTextWithShadow(textRenderer, Text.literal(pageTitle()), panelX + 160, panelY + 20, 0xFFFFFFFF);
        int x = panelX + 160, y = panelY + 68;
        switch (page) {
            case HOME -> { line(context, "YOUR SPACE, YOUR PEOPLE", x, y, 0xFFE76A84); y += 22; line(context, "Friends and shared worlds in one place.", x, y, 0xFFE6E6EE); y += 26; line(context, "FRIENDS", x, y, 0xFFFFFFFF); y += 14; line(context, "Your friend list will appear here.", x, y, 0xFFAAAAB8); y += 28; line(context, "QUICK ACCESS", x, y, 0xFFFFFFFF); y += 14; line(context, "Worlds  /  Voice  /  Skins  /  Emotes", x, y, 0xFFAAAAB8); }
            case FRIENDS -> { line(context, "YOUR FRIENDS", x, y, 0xFFE76A84); y += 22; line(context, "Friend service: not connected", x, y, 0xFFFFC66D); y += 18; line(context, "The UI is ready for account and presence sync.", x, y, 0xFFAAAAB8); }
            case WORLDS -> { line(context, "PLAY IN YOUR WORLD", x, y, 0xFFE76A84); y += 22; line(context, "Public hosting is not enabled yet.", x, y, 0xFFFFC66D); y += 18; line(context, "A tunnel relay and invite service are required.", x, y, 0xFFAAAAB8); y += 18; line(context, "Do not share your IP with strangers.", x, y, 0xFFAAAAB8); }
            case VOICE -> { line(context, "PROXIMITY VOICE", x, y, 0xFFE76A84); y += 22; line(context, "Voice transport: not connected", x, y, 0xFFFFC66D); y += 18; line(context, "Mic controls will appear after voice integration.", x, y, 0xFFAAAAB8); }
            case SKINS -> { line(context, "YOUR SKIN COLLECTION", x, y, 0xFFE76A84); y += 22; List<String> skins = SkinLibrary.listSkins(); line(context, skins.size() + " / " + SkinLibrary.MAX_SKINS + " saved skins", x, y, 0xFFFFFFFF); y += 18; line(context, "Active: " + (SkinLibrary.activeSkin().isBlank() ? "none" : SkinLibrary.activeSkin()), x, y, 0xFFAAAAB8); y += 22; if (skins.isEmpty()) line(context, "Import a 64x64 or legacy 64x32 PNG.", x, y, 0xFFAAAAB8); else { for (String skin : skins.stream().limit(7).toList()) { line(context, (skin.equals(SkinLibrary.activeSkin()) ? "✓ " : "  ") + skin, x, y, 0xFFE6E6EE); y += 15; } line(context, "Saved locally in .minecraft/nexus/skins", x, panelY + panelH - 61, 0xFFAAAAB8); } }
            case EMOTES -> { line(context, "EXPRESS YOURSELF", x, y, 0xFFE76A84); y += 22; line(context, "Wave  •  Dance  •  Cheer  •  Sad", x, y, 0xFFE6E6EE); y += 20; line(context, "Player-synced animations are planned.", x, y, 0xFFAAAAB8); }
        }
        context.drawTextWithShadow(textRenderer, Text.literal(notice), panelX + 18, panelY + panelH - 28, 0xFFB7B7C6);
        super.render(context, mouseX, mouseY, delta);
    }

    private void line(DrawContext context, String value, int x, int y, int color) { context.drawTextWithShadow(textRenderer, Text.literal(value), x, y, color); }
    private String pageTitle() { return switch (page) { case HOME -> "Overview"; case FRIENDS -> "Friends"; case WORLDS -> "World hosting"; case VOICE -> "Voice chat"; case SKINS -> "Skin library"; case EMOTES -> "Emotes & cosmetics"; }; }
}
