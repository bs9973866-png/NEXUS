package dev.nexus.client;

import net.minecraft.client.MinecraftClient;

import javax.imageio.ImageIO;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
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
    private static String cachedPreviewName = "";
    private static int[] cachedHeadPixels = new int[64];

    private SkinLibrary() {}

    public static void initialize() {
        try {
            Files.createDirectories(DIRECTORY);
            List<String> skins = listSkins();
            if (!skins.isEmpty() && (activeSkin.isBlank() || !skins.contains(activeSkin))) activeSkin = skins.get(0);
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not initialize skin library: " + e.getMessage());
        }
        invalidatePreview();
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
        if (listSkins().contains(name)) {
            activeSkin = name;
            invalidatePreview();
        }
    }

    /**
     * Returns the active skin's front head texture as 64 ARGB pixels.
     * Cached so rendering the profile screen never performs image IO each frame.
     */
    public static int[] activeSkinHeadPixels() {
        if (activeSkin.isBlank()) return cachedHeadPixels;
        if (activeSkin.equals(cachedPreviewName)) return cachedHeadPixels;
        try {
            BufferedImage image = ImageIO.read(DIRECTORY.resolve(activeSkin).toFile());
            int[] pixels = new int[64];
            if (image != null && image.getWidth() == 64 && image.getHeight() >= 32) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        int base = image.getRGB(8 + x, 8 + y);
                        int hat = image.getRGB(40 + x, 8 + y);
                        int alpha = (hat >>> 24) & 0xFF;
                        pixels[y * 8 + x] = alpha > 0 ? hat : base;
                    }
                }
            }
            cachedHeadPixels = pixels;
            cachedPreviewName = activeSkin;
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not preview skin: " + e.getMessage());
        }
        return cachedHeadPixels;
    }

    private static void invalidatePreview() {
        cachedPreviewName = "";
        cachedHeadPixels = new int[64];
    }

    public static String importSkin() {
        if (listSkins().size() >= MAX_SKINS) return "LIMIT";
        if (GraphicsEnvironment.isHeadless()) return "NO_PICKER";
        FileDialog dialog = new FileDialog((Frame) null, "Choose a Minecraft skin PNG", FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".png"));
        dialog.setVisible(true);
        String filename = dialog.getFile();
        String directory = dialog.getDirectory();
        dialog.dispose();
        if (filename == null || directory == null) return "CANCELLED";
        File file = new File(directory, filename);
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null || !((image.getWidth() == 64 && image.getHeight() == 64) || (image.getWidth() == 64 && image.getHeight() == 32))) {
                return "INVALID_SIZE";
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
            invalidatePreview();
            return "SAVED";
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not import skin: " + e.getMessage());
            return "IO_ERROR";
        }
    }
}
