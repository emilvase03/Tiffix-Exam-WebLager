package dk.easv.tiffixexamweblager.BLL.Utils;

import dk.easv.tiffixexamweblager.BE.Rule;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ImageTransformations {

    public static BufferedImage applyRules(BufferedImage image, List<Rule> rules) {
        if (image == null || rules == null || rules.isEmpty()) return image;

        BufferedImage result = image;
        for (Rule rule : rules) {
            switch (rule.getType()) {
                case ROTATE     -> result = rotate(result, rule.getAmount());
                case BRIGHTNESS -> result = adjustBrightness(result, rule.getAmount());
                default         -> {  }
            }
        }
        return result;
    }

    public static BufferedImage applyAll(BufferedImage source, int rotation, int brightness) {
        if (source == null) return null;

        BufferedImage result = source;
        if (brightness != 0) result = adjustBrightness(result, brightness);
        if (rotation   != 0) result = rotate(result, rotation);
        return result;
    }
//method is applied during rendering to keep image edits non‑destructive and performant.
    public static BufferedImage rotate(BufferedImage image, int degrees) {
        degrees = ((degrees % 360) + 360) % 360;   // normalise to 0-359
        if (degrees == 0) return image;

        double radians = Math.toRadians(degrees);
        int w = image.getWidth();
        int h = image.getHeight();
        int type = safeType(image);

        if (degrees % 180 != 0) {

            BufferedImage rotated = new BufferedImage(h, w, type);
            Graphics2D g2d = rotated.createGraphics();
            applyQualityHints(g2d);
            g2d.translate(h / 2.0, w / 2.0);
            g2d.rotate(radians);
            g2d.translate(-w / 2.0, -h / 2.0);
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return rotated;
        } else {
            // 180° — dimensions stay the same
            BufferedImage rotated = new BufferedImage(w, h, type);
            Graphics2D g2d = rotated.createGraphics();
            applyQualityHints(g2d);
            g2d.rotate(radians, w / 2.0, h / 2.0);
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return rotated;
        }
    }

    public static BufferedImage adjustBrightness(BufferedImage image, int amount) {
        int type = safeType(image);
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                int r = clamp(c.getRed()   + amount);
                int g = clamp(c.getGreen() + amount);
                int b = clamp(c.getBlue()  + amount);
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }

    private static int safeType(BufferedImage image) {
        int t = image.getType();
        return (t == BufferedImage.TYPE_CUSTOM) ? BufferedImage.TYPE_INT_ARGB : t;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void applyQualityHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }
}