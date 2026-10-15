package dev.nexus.client;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public final class NexusData {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static String language = "ru";

    private NexusData() {}

    private static Path root() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("nexus");
    }

    private static Path friendsFile() { return root().resolve("friends.txt"); }
    private static Path historyFile() { return root().resolve("chat-history.txt"); }
    private static Path settingsFile() { return root().resolve("settings.properties"); }

    public static void initialize() {
        try {
            Files.createDirectories(root());
            Properties properties = new Properties();
            if (Files.exists(settingsFile())) {
                try (var reader = Files.newBufferedReader(settingsFile(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
            language = properties.getProperty("language", "ru").equalsIgnoreCase("en") ? "en" : "ru";
            if (!Files.exists(friendsFile())) Files.createFile(friendsFile());
            if (!Files.exists(historyFile())) Files.createFile(historyFile());
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not load local data: " + e.getMessage());
        }
    }

    public static String language() { return language; }

    public static String t(String english, String russian) {
        return language.equals("ru") ? russian : english;
    }

    public static void toggleLanguage() {
        language = language.equals("ru") ? "en" : "ru";
        try {
            Files.createDirectories(root());
            Properties properties = new Properties();
            properties.setProperty("language", language);
            try (var writer = Files.newBufferedWriter(settingsFile(), StandardCharsets.UTF_8)) {
                properties.store(writer, "NEXUS interface settings");
            }
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not save language: " + e.getMessage());
        }
    }

    public static boolean validUsername(String name) {
        return name != null && USERNAME.matcher(name.trim()).matches();
    }

    public static List<String> friends() {
        try {
            if (!Files.exists(friendsFile())) return List.of();
            List<String> names = new ArrayList<>();
            for (String line : Files.readAllLines(friendsFile(), StandardCharsets.UTF_8)) {
                String name = line.trim();
                if (validUsername(name) && names.stream().noneMatch(n -> n.equalsIgnoreCase(name))) names.add(name);
            }
            return List.copyOf(names);
        } catch (IOException e) {
            return List.of();
        }
    }

    private static boolean addFriend(String name) {
        if (!validUsername(name)) return false;
        if (friends().stream().anyMatch(n -> n.equalsIgnoreCase(name))) return true;
        try {
            Files.createDirectories(root());
            if (friends().size() >= 100) return false;
            Files.writeString(friendsFile(), name + System.lineSeparator(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not save friend: " + e.getMessage());
            return false;
        }
    }

    public static String requestFriend(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (!validUsername(name)) return t("Enter a valid Minecraft name (3–16 letters, numbers or _).", "Введи корректный ник Minecraft (3–16 символов: буквы, цифры или _).");
        if (!addFriend(name)) return t("Could not save this contact. The local list may be full.", "Не удалось сохранить контакт. Возможно, список заполнен.");
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || client.player == null) {
            return t("Saved locally. Join a server to send the request via /msg.", "Контакт сохранён локально. Зайди на сервер, чтобы отправить заявку через /msg.");
        }
        String request = "Hi! Want to add each other on NEXUS? Message me back.";
        client.getNetworkHandler().sendChatCommand("msg " + name + " " + request);
        recordOutgoing(name, request);
        return t("Contact saved; request message sent through the current server's /msg command.", "Контакт сохранён; сообщение-заявка отправлено командой /msg текущего сервера.");
    }

    public static String sendMessage(String rawName, String rawMessage) {
        String name = rawName == null ? "" : rawName.trim();
        String message = rawMessage == null ? "" : rawMessage.replaceAll("[\\r\\n\\t]", " ").replace('|', '/').trim();
        if (!validUsername(name)) return t("Enter the recipient's Minecraft name.", "Введи ник получателя.");
        if (message.isBlank()) return t("Write a message first.", "Сначала напиши сообщение.");
        if (message.length() > 220) message = message.substring(0, 220);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || client.player == null) {
            return t("Join a multiplayer server first. Cross-server chat is not configured.", "Сначала зайди на сервер. Межсерверный чат пока не подключён.");
        }
        client.getNetworkHandler().sendChatCommand("msg " + name + " " + message);
        recordOutgoing(name, message);
        return t("Message sent to the server. Replies appear in Minecraft chat.", "Сообщение отправлено серверу. Ответы появятся в обычном чате Minecraft.");
    }

    private static void recordOutgoing(String name, String message) {
        try {
            Files.createDirectories(root());
            String safeMessage = message.replaceAll("[\\r\\n\\t]", " ").replace('|', '/');
            String line = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "\t" + name + "\t" + safeMessage + System.lineSeparator();
            Files.writeString(historyFile(), line, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not save chat history: " + e.getMessage());
        }
    }

    public static List<String> recentOutgoing(String friend, int limit) {
        if (friend == null || friend.isBlank()) return List.of();
        try {
            if (!Files.exists(historyFile())) return List.of();
            List<String> entries = new ArrayList<>();
            for (String line : Files.readAllLines(historyFile(), StandardCharsets.UTF_8)) {
                String[] fields = line.split("\\t", 3);
                if (fields.length == 3 && fields[1].equalsIgnoreCase(friend)) {
                    entries.add(fields[0] + "  " + fields[2]);
                }
            }
            int from = Math.max(0, entries.size() - limit);
            return List.copyOf(entries.subList(from, entries.size()));
        } catch (IOException e) {
            return List.of();
        }
    }
}
