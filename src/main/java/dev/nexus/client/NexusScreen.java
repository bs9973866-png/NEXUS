package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public final class NexusScreen extends Screen {
    private enum Page { HOME, FRIENDS, CHAT, PROFILE, SKINS, MORE }

    private static final Identifier UI_FONT = Identifier.of("minecraft", "uniform");
    private static final int BG = 0xFF0A0A10;
    private static final int PANEL = 0xFF12121B;
    private static final int PANEL_ALT = 0xFF191923;
    private static final int RED = 0xFFE84B68;
    private static final int PINK = 0xFFFF8195;
    private static final int MUTED = 0xFF9E9AA8;
    private final Screen parent;
    private Page page = Page.HOME;
    private String notice = "";
    private String selectedFriend = "";
    private int panelX, panelY, panelW, panelH, sidebarW, contentX, contentY, contentW;
    private TextFieldWidget friendInput;
    private TextFieldWidget recipientInput;
    private TextFieldWidget messageInput;

    public NexusScreen() { this(null); }
    public NexusScreen(Screen parent) {
        super(Text.literal("NEXUS"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        panelW = Math.max(280, Math.min(900, width - 18));
        panelH = Math.max(210, Math.min(560, height - 18));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        sidebarW = Math.min(148, Math.max(98, panelW / 3));
        contentX = panelX + sidebarW + 18;
        contentY = panelY + 66;
        contentW = Math.max(110, panelW - sidebarW - 34);

        int navX = panelX + 10;
        int navY = panelY + 60;
        int navW = sidebarW - 20;
        int navH = 19;
        addNav("01  " + NexusData.t("HOME", "ГЛАВНАЯ"), Page.HOME, navX, navY, navW, navH, 0);
        addNav("02  " + NexusData.t("FRIENDS", "ДРУЗЬЯ"), Page.FRIENDS, navX, navY + 22, navW, navH, 1);
        addNav("03  " + NexusData.t("MESSAGES", "СООБЩЕНИЯ"), Page.CHAT, navX, navY + 44, navW, navH, 2);
        addNav("04  " + NexusData.t("PROFILE", "ПРОФИЛЬ"), Page.PROFILE, navX, navY + 66, navW, navH, 3);
        addNav("05  " + NexusData.t("SKINS", "СКИНЫ"), Page.SKINS, navX, navY + 88, navW, navH, 4);
        addNav("06  " + NexusData.t("MORE", "ЕЩЁ"), Page.MORE, navX, navY + 110, navW, navH, 5);

        addDrawableChild(new NexusButton(panelX + panelW - 86, panelY + panelH - 29, 76, 19,
                NexusData.t("CLOSE", "ЗАКРЫТЬ"), this::close));

        switch (page) {
            case FRIENDS -> buildFriendsPage();
            case CHAT -> buildChatPage();
            case SKINS -> buildSkinsPage();
            case MORE -> buildMorePage();
            default -> { }
        }
    }

    private void addNav(String label, Page target, int x, int y, int w, int h, int index) {
        addDrawableChild(new NexusButton(x, y, w, h, label, () -> {
            page = target;
            notice = "";
            clearAndInit();
        }));
    }

    private TextFieldWidget input(int x, int y, int w, String placeholder, int limit) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, Math.max(40, w), 19, Text.literal(""));
        field.setMaxLength(limit);
        field.setDrawsBackground(false);
        field.setEditableColor(0xFFF0EAF2);
        field.setUneditableColor(0xFFAAA3B0);
        field.setTextShadow(true);
        field.setPlaceholder(Text.literal(placeholder).styled(style -> style.withFont(UI_FONT)));
        addDrawableChild(field);
        return field;
    }

    private void buildFriendsPage() {
        int y = contentY + 36;
        friendInput = input(contentX, y, Math.max(100, contentW - 108),
                NexusData.t("Minecraft username", "Ник Minecraft"), 16);
        addDrawableChild(new NexusButton(contentX + Math.max(100, contentW - 102), y, 102, 19,
                NexusData.t("SEND REQUEST", "ЗАЯВКА"), () -> {
            notice = NexusData.requestFriend(friendInput.getText());
            friendInput.setText("");
            clearAndInit();
        }));
        int listY = y + 34;
        List<String> friends = NexusData.friends();
        if (friends.isEmpty()) {
            drawNothingListHint = true;
        } else {
            drawNothingListHint = false;
        }
        int rowHeight = 22;
        int maxRows = Math.max(1, Math.min(5, (panelY + panelH - 54 - listY) / rowHeight));
        for (String name : friends.stream().limit(maxRows).toList()) {
            addDrawableChild(new NexusButton(contentX, listY, Math.max(100, contentW - 4), 19,
                    "@" + name + "   ›  " + NexusData.t("OPEN CHAT", "НАПИСАТЬ"), () -> {
                selectedFriend = name;
                page = Page.CHAT;
                notice = "";
                clearAndInit();
            }));
            listY += rowHeight;
        }
    }

    private boolean drawNothingListHint;

    private void buildChatPage() {
        int y = contentY + 34;
        recipientInput = input(contentX, y, contentW,
                NexusData.t("Recipient name", "Ник получателя"), 16);
        if (!selectedFriend.isBlank()) recipientInput.setText(selectedFriend);
        y += 27;
        int sendW = Math.min(78, Math.max(64, contentW / 3));
        messageInput = input(contentX, y, contentW - sendW - 5,
                NexusData.t("Write a message…", "Напиши сообщение…"), 220);
        addDrawableChild(new NexusButton(contentX + contentW - sendW, y, sendW, 19,
                NexusData.t("SEND", "ОТПРАВИТЬ"), () -> {
            String target = recipientInput.getText().trim();
            String message = messageInput.getText();
            notice = NexusData.sendMessage(target, message);
            if (NexusData.validUsername(target)) selectedFriend = target;
            if (notice.equals(NexusData.t("Message sent to the server. Replies appear in Minecraft chat.", "Сообщение отправлено серверу. Ответы появятся в обычном чате Minecraft."))) {
                messageInput.setText("");
            }
        }));
    }

    private void buildSkinsPage() {
        List<String> skins = SkinLibrary.listSkins();
        int listY = contentY + 42;
        int buttonW = Math.max(90, Math.min(contentW - 4, contentW - 62));
        int maxRows = Math.max(1, Math.min(5, (panelY + panelH - 62 - listY) / 22));
        for (String skin : skins.stream().limit(maxRows).toList()) {
            String label = (skin.equals(SkinLibrary.activeSkin()) ? "✓  " : "    ") + skin;
            addDrawableChild(new NexusButton(contentX, listY, buttonW, 19, label, () -> {
                SkinLibrary.select(skin);
                notice = NexusData.t("Preview selected: ", "Выбрано для предпросмотра: ") + skin;
                clearAndInit();
            }));
            listY += 22;
        }
        addDrawableChild(new NexusButton(contentX, panelY + panelH - 54, Math.min(150, contentW), 20,
                NexusData.t("IMPORT PNG", "ИМПОРТ PNG"), () -> {
            String result = SkinLibrary.importSkin();
            notice = switch (result) {
                case "SAVED" -> NexusData.t("Skin added to your local collection.", "Скин добавлен в локальную коллекцию.");
                case "LIMIT" -> NexusData.t("Limit reached: 10 skins.", "Лимит достигнут: 10 скинов.");
                case "INVALID_SIZE" -> NexusData.t("Use a 64×64 or legacy 64×32 PNG.", "Нужен PNG 64×64 или старый формат 64×32.");
                case "NO_PICKER" -> NexusData.t("File picker is not available.", "Выбор файла недоступен.");
                case "CANCELLED" -> NexusData.t("Import cancelled.", "Импорт отменён.");
                default -> NexusData.t("Could not import this file.", "Не удалось импортировать файл.");
            };
            clearAndInit();
        }));
    }

    private void buildMorePage() {
        int y = panelY + panelH - 53;
        addDrawableChild(new NexusButton(contentX, y, Math.min(170, contentW), 19,
                NexusData.t("LANGUAGE: " + NexusData.language().toUpperCase(), "ЯЗЫК: " + NexusData.language().toUpperCase()), () -> {
            NexusData.toggleLanguage();
            notice = NexusData.t("Language changed to English.", "Язык изменён на русский.");
            clearAndInit();
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fully custom dark shell; avoid vanilla screen blur to prevent duplicate-blur crashes.
        context.fill(0, 0, width, height, 0xD8070810);
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        context.fill(panelX, panelY, panelX + panelW, panelY + 3, RED);
        context.fill(panelX + 1, panelY + 3, panelX + panelW - 1, panelY + panelH - 1, 0xFF0D0D14);
        context.fill(panelX + 7, panelY + 8, panelX + sidebarW, panelY + panelH - 8, 0xFF14141E);
        context.fill(panelX + sidebarW + 6, panelY + 8, panelX + panelW - 7, panelY + panelH - 8, 0xFF101018);
        context.fill(panelX + sidebarW + 6, panelY + 8, panelX + sidebarW + 7, panelY + panelH - 8, 0xFF33202A);

        text(context, "NEXUS", panelX + 17, panelY + 17, 0xFFFFFFFF, true);
        text(context, "SOCIAL / GAME LAYER", panelX + 17, panelY + 33, PINK, false);
        text(context, NexusData.t("NO ACCOUNT. JUST PLAY.", "БЕЗ АККАУНТА. ПРОСТО ИГРАЙ."), panelX + 17, panelY + 44, MUTED, false);
        text(context, pageTitle(), contentX, panelY + 19, 0xFFFFFFFF, true);
        text(context, NexusData.t("LOCAL PROFILE", "ЛОКАЛЬНЫЙ ПРОФИЛЬ"), panelX + panelW - 112, panelY + 20, PINK, false);
        String username = MinecraftClient.getInstance().getSession().getUsername();
        text(context, username, panelX + panelW - 112, panelY + 32, MUTED, false);
        context.fill(contentX, panelY + 47, panelX + panelW - 16, panelY + 48, 0xFF30232D);

        switch (page) {
            case HOME -> renderHome(context);
            case FRIENDS -> renderFriends(context);
            case CHAT -> renderChat(context);
            case PROFILE -> renderProfile(context);
            case SKINS -> renderSkins(context);
            case MORE -> renderMore(context);
        }

        if (!notice.isBlank()) {
            int noticeY = panelY + panelH - 30;
            context.fill(panelX + 9, noticeY - 3, panelX + panelW - 93, noticeY + 11, 0xFF21171F);
            text(context, fit(notice, Math.max(24, panelW - 120)), panelX + 15, noticeY, PINK, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHome(DrawContext context) {
        int x = contentX, y = contentY + 3;
        context.fill(x, y, x + contentW, y + 63, 0xFF21151F);
        context.fill(x, y, x + 3, y + 63, RED);
        text(context, NexusData.t("YOUR PEOPLE. YOUR WORLD.", "ТВОИ ДРУЗЬЯ. ТВОЙ МИР."), x + 12, y + 12, PINK, true);
        text(context, NexusData.t("A clean social layer for Minecraft.", "Социальный интерфейс для Minecraft."), x + 12, y + 28, 0xFFE7E0E8, false);
        text(context, NexusData.t("No NEXUS account or registration.", "Без аккаунтов и регистрации NEXUS."), x + 12, y + 42, MUTED, false);

        int tileY = y + 75;
        int gap = 7;
        int tileW = Math.max(64, (contentW - gap) / 2);
        dashboardTile(context, x, tileY, tileW, 42, "01", NexusData.t("FRIENDS", "ДРУЗЬЯ"), NexusData.friends().size() + " " + NexusData.t("saved locally", "сохранено локально"));
        dashboardTile(context, x + tileW + gap, tileY, tileW, 42, "02", NexusData.t("MESSAGES", "СООБЩЕНИЯ"), NexusData.t("via server /msg", "через /msg сервера"));
        int secondY = tileY + 49;
        dashboardTile(context, x, secondY, tileW, 42, "03", NexusData.t("SKINS", "СКИНЫ"), SkinLibrary.listSkins().size() + " / " + SkinLibrary.MAX_SKINS);
        dashboardTile(context, x + tileW + gap, secondY, tileW, 42, "04", NexusData.t("PROFILE", "ПРОФИЛЬ"), NexusData.t("no login required", "вход не нужен"));
        text(context, NexusData.t("Select a section on the left to get started.", "Выбери раздел слева, чтобы начать."), x, secondY + 56, MUTED, false);
    }

    private void dashboardTile(DrawContext context, int x, int y, int w, int h, String number, String heading, String sub) {
        context.fill(x, y, x + w, y + h, PANEL_ALT);
        context.fill(x, y, x + w, y + 1, 0xFF39303B);
        text(context, number, x + 7, y + 7, RED, true);
        text(context, fit(heading, Math.max(5, w / 6)), x + 28, y + 7, 0xFFF2EDF5, true);
        text(context, fit(sub, Math.max(7, w / 5)), x + 8, y + 25, MUTED, false);
    }

    private void renderFriends(DrawContext context) {
        int x = contentX, y = contentY + 5;
        text(context, NexusData.t("CONTACT LIST", "СПИСОК КОНТАКТОВ"), x, y, PINK, true);
        text(context, NexusData.t("Requests are sent as a normal /msg on your current server.", "Заявка отправляется обычным /msg на текущем сервере."), x, y + 17, MUTED, false);
        if (drawNothingListHint) {
            text(context, NexusData.t("No contacts yet. Enter a nickname below.", "Пока нет контактов. Введи ник ниже."), x, y + 66, 0xFFE7E0E8, false);
        }
        context.fill(x - 1, panelY + panelH - 80, x + contentW, panelY + panelH - 79, 0xFF33232E);
        text(context, NexusData.t("ADD A FRIEND", "ДОБАВИТЬ ДРУГА"), x, panelY + panelH - 72, PINK, true);
        text(context, NexusData.t("Local nickname list, no registration.", "Никнеймы хранятся локально, без регистрации."), x, panelY + panelH - 60, MUTED, false);
    }

    private void renderChat(DrawContext context) {
        int x = contentX, y = contentY + 4;
        String target = recipientInput == null ? selectedFriend : recipientInput.getText().trim();
        text(context, NexusData.t("PRIVATE MESSAGE", "ЛИЧНОЕ СООБЩЕНИЕ"), x, y, PINK, true);
        text(context, target.isBlank() ? NexusData.t("Choose a contact or type a nickname.", "Выбери контакт или введи ник.") : "@" + target, x, y + 17, 0xFFEAE5EE, false);
        context.fill(x, y + 52, x + contentW, y + 53, 0xFF30232D);
        int historyY = y + 78;
        List<String> history = NexusData.recentOutgoing(target, 5);
        for (String line : history) {
            text(context, fit("→ " + line, Math.max(15, contentW - 4)), x, historyY, MUTED, false);
            historyY += 14;
        }
        if (history.isEmpty()) {
            text(context, NexusData.t("No local messages in this thread yet.", "В этой переписке пока нет сохранённых сообщений."), x, historyY, MUTED, false);
        }
        text(context, NexusData.t("Only works on servers with /msg. Replies arrive in normal chat.", "Работает на серверах с /msg. Ответы приходят в обычный чат."), x, panelY + panelH - 48, PINK, false);
    }

    private void renderProfile(DrawContext context) {
        int x = contentX, y = contentY + 10;
        text(context, NexusData.t("PLAYER CARD", "КАРТОЧКА ИГРОКА"), x, y, PINK, true);
        drawSkinHead(context, x, y + 27, 5);
        text(context, MinecraftClient.getInstance().getSession().getUsername(), x + 55, y + 37, 0xFFFFFFFF, true);
        text(context, NexusData.t("Minecraft profile", "Профиль Minecraft"), x + 55, y + 52, MUTED, false);
        text(context, NexusData.t("NEXUS does not need a separate account.", "NEXUS не требует отдельного аккаунта."), x, y + 91, 0xFFE8E1EB, false);
        text(context, NexusData.t("Skin shown here is a local preview only.", "Скин здесь отображается только в предпросмотре."), x, y + 108, MUTED, false);
        text(context, NexusData.t("Equipping/syncing a skin needs a skin service.", "Для применения/синхронизации нужен сервис скинов."), x, y + 123, MUTED, false);
    }

    private void renderSkins(DrawContext context) {
        int x = contentX, y = contentY + 5;
        text(context, NexusData.t("YOUR SKIN VAULT", "ТВОЁ ХРАНИЛИЩЕ СКИНОВ"), x, y, PINK, true);
        text(context, SkinLibrary.listSkins().size() + " / " + SkinLibrary.MAX_SKINS + "  PNG", x, y + 16, 0xFFE7E0E8, false);
        text(context, NexusData.t("Select a row to preview the face.", "Нажми на скин, чтобы увидеть лицо."), x, y + 29, MUTED, false);
        int previewX = panelX + panelW - 65;
        drawSkinHead(context, previewX, y + 42, 5);
        if (!SkinLibrary.activeSkin().isBlank()) {
            text(context, fit(SkinLibrary.activeSkin(), 12), previewX - 8, y + 85, PINK, false);
        }
        if (SkinLibrary.listSkins().isEmpty()) {
            text(context, NexusData.t("Import a 64×64 or legacy 64×32 PNG.", "Импортируй PNG 64×64 или старый 64×32."), x, y + 52, MUTED, false);
        }
        text(context, NexusData.t("Saved on this PC in .minecraft/nexus/skins.", "Сохранено на этом ПК: .minecraft/nexus/skins."), x, panelY + panelH - 34, MUTED, false);
    }

    private void renderMore(DrawContext context) {
        int x = contentX, y = contentY + 6;
        text(context, NexusData.t("EXPERIMENTAL FEATURES", "ДОПОЛНИТЕЛЬНЫЕ ФУНКЦИИ"), x, y, PINK, true);
        y += 24;
        statusRow(context, x, y, "01", NexusData.t("WORLD INVITES", "ПРИГЛАШЕНИЯ В МИР"), NexusData.t("Needs a public tunnel/relay server.", "Нужен сервер туннеля/ретрансляции."));
        y += 32;
        statusRow(context, x, y, "02", NexusData.t("VOICE CHAT", "ГОЛОСОВОЙ ЧАТ"), NexusData.t("Audio transport is not connected.", "Передача голоса не подключена."));
        y += 32;
        statusRow(context, x, y, "03", NexusData.t("EMOTES & COSMETICS", "ЭМОУТЫ И КОСМЕТИКА"), NexusData.t("UI concepts only; no remote animations.", "Пока интерфейс; синхронизации анимаций нет."));
        y += 40;
        text(context, NexusData.t("INTERFACE LANGUAGE", "ЯЗЫК ИНТЕРФЕЙСА"), x, y, PINK, true);
        text(context, NexusData.language().toUpperCase() + "  •  " + NexusData.t("click the button below to switch", "нажми кнопку ниже, чтобы сменить"), x, y + 15, MUTED, false);
    }

    private void statusRow(DrawContext context, int x, int y, String number, String title, String subtitle) {
        context.fill(x, y, x + contentW, y + 28, PANEL_ALT);
        context.fill(x, y, x + 2, y + 28, RED);
        text(context, number, x + 8, y + 5, RED, true);
        text(context, fit(title, Math.max(8, contentW / 6)), x + 30, y + 5, 0xFFF2EDF5, true);
        text(context, fit(subtitle, Math.max(12, contentW / 5)), x + 30, y + 17, MUTED, false);
    }

    private void drawSkinHead(DrawContext context, int x, int y, int pixelSize) {
        int[] pixels = SkinLibrary.activeSkinHeadPixels();
        context.fill(x - 2, y - 2, x + 8 * pixelSize + 2, y + 8 * pixelSize + 2, 0xFF3B2631);
        context.fill(x - 1, y - 1, x + 8 * pixelSize + 1, y + 8 * pixelSize + 1, 0xFF09090D);
        for (int py = 0; py < 8; py++) {
            for (int px = 0; px < 8; px++) {
                int color = pixels[py * 8 + px];
                if (((color >>> 24) & 0xFF) > 0) {
                    context.fill(x + px * pixelSize, y + py * pixelSize,
                            x + (px + 1) * pixelSize, y + (py + 1) * pixelSize, color);
                }
            }
        }
    }

    private void text(DrawContext context, String value, int x, int y, int color, boolean bold) {
        String fitted = value == null ? "" : value;
        Text label = Text.literal(fitted).styled(style -> style.withFont(UI_FONT));
        if (bold) label = label.copy().formatted(net.minecraft.util.Formatting.BOLD);
        context.drawTextWithShadow(textRenderer, label, x, y, color);
    }

    private String fit(String value, int approximateChars) {
        if (value == null) return "";
        int max = Math.max(4, approximateChars);
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 1)) + "…";
    }

    private String pageTitle() {
        return switch (page) {
            case HOME -> NexusData.t("CONTROL CENTER", "ЦЕНТР УПРАВЛЕНИЯ");
            case FRIENDS -> NexusData.t("YOUR PEOPLE", "ТВОИ ДРУЗЬЯ");
            case CHAT -> NexusData.t("DIRECT MESSAGES", "ЛИЧНЫЕ СООБЩЕНИЯ");
            case PROFILE -> NexusData.t("PLAYER PROFILE", "ПРОФИЛЬ ИГРОКА");
            case SKINS -> NexusData.t("SKIN LIBRARY", "КОЛЛЕКЦИЯ СКИНОВ");
            case MORE -> NexusData.t("MORE / SETTINGS", "ЕЩЁ / НАСТРОЙКИ");
        };
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
