package dev.nexus.client;

import net.minecraft.client.MinecraftClient;

import javax.imageio.ImageIO;
import java.awt.GraphicsEnvironment;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SkinLibrary {
    public static final int MAX_SKINS = 10;
    private static final Path DIRECTORY = MinecraftClient.getInstance().runDirectory.toPath().resolve("nexus").resolve("skins");
    private static String activeSkin = "";

    private SkinLibrary() {}

    public static void initialize() {
        try {
            Files.createDirectories(DIRECTORY);
            List<String> skins = listSkins();
            if (!skins.isEmpty()) activeSkin = skins.get(0);
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not initialize skin library: " + e.getMessage());
        }
    }

    public static List<String> listSkins() {
        try (var paths = Files.list(DIRECTORY)) {
            return paths.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                    .map(p -> p.getFileName().toString()).sorted(Comparator.naturalOrder()).limit(MAX_SKINS).toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static String activeSkin() { return activeSkin; }

    public static void select(String name) {
        if (listSkins().contains(name)) activeSkin = name;
    }

    public static String importSkin() {
        if (listSkins().size() >= MAX_SKINS) return "You already have 10 skins.";
        if (GraphicsEnvironment.isHeadless()) return "File picker is unavailable in this environment.";
        FileDialog dialog = new FileDialog((Frame) null, "Choose a Minecraft skin PNG", FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".png"));
        dialog.setVisible(true);
        String filename = dialog.getFile();
        String directory = dialog.getDirectory();
        dialog.dispose();
        if (filename == null || directory == null) return "Import cancelled.";
        File file = new File(directory, filename);
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null || !((image.getWidth() == 64 && image.getHeight() == 64) || (image.getWidth() == 64 && image.getHeight() == 32))) {
                return "Skin must be a 64x64 or legacy 64x32 PNG.";
            }
            String safeName = file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            if (!safeName.toLowerCase().endsWith(".png")) safeName += ".png";
            Path destination = DIRECTORY.resolve(safeName);
            int suffix = 1;
            while (Files.exists(destination)) {
                String stem = safeName.substring(0, safeName.length() - 4);
                destination = DIRECTORY.resolve(stem + "-" + suffix++ + ".png");
            }
            Files.copy(file.toPath(), destination);
            activeSkin = destination.getFileName().toString();
            return "Saved " + activeSkin + ".";
        } catch (IOException e) {
            return "Could not import skin: " + e.getMessage();
        }
    }
}
