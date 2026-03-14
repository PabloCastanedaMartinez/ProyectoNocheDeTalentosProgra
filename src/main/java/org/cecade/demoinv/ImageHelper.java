package org.cecade.demoinv;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.InputStream;

/**
 * Utility to load product images or generate placeholder images.
 */
public class ImageHelper {

    public static Image cargarImagen(String rutaImagen, double width, double height) {
        // Try loading from resources
        if (rutaImagen != null && !rutaImagen.isBlank()) {
            try {
                InputStream is = ImageHelper.class.getResourceAsStream(rutaImagen);
                if (is != null) {
                    return new Image(is, width, height, true, true);
                }
            } catch (Exception ignored) {}

            // Try as absolute file path
            try {
                File f = new File(rutaImagen);
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString(), width, height, true, true);
                    if (!img.isError()) return img;
                }
            } catch (Exception ignored) {}
        }

        // Generate placeholder
        return generarPlaceholder(width, height, rutaImagen);
    }

    @SuppressWarnings("unused")
    private static Image generarPlaceholder(double w, double h, String label) {
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background
        gc.setFill(Color.rgb(180, 180, 180));
        gc.fillRect(0, 0, w, h);

        // Border
        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(2);
        gc.strokeRect(5, 5, w - 10, h - 10);

        // Mountain icon
        double cx = w / 2, cy = h / 2;
        gc.setFill(Color.rgb(60, 60, 60));

        // Left mountain
        gc.fillPolygon(
                new double[]{w * 0.15, w * 0.45, w * 0.55},
                new double[]{h * 0.75, h * 0.25, h * 0.75},
                3
        );
        // Right mountain
        gc.fillPolygon(
                new double[]{w * 0.40, w * 0.65, w * 0.85},
                new double[]{h * 0.75, h * 0.35, h * 0.75},
                3
        );

        // Sun
        gc.setFill(Color.rgb(80, 80, 80));
        gc.fillOval(w * 0.25, h * 0.12, w * 0.15, w * 0.15);

        // Sun rays
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(1.5);
        double sunCx = w * 0.325, sunCy = h * 0.12 + w * 0.075;
        double rayLen = w * 0.1;
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            gc.strokeLine(
                    sunCx + Math.cos(angle) * w * 0.09,
                    sunCy + Math.sin(angle) * w * 0.09,
                    sunCx + Math.cos(angle) * (w * 0.09 + rayLen * 0.4),
                    sunCy + Math.sin(angle) * (w * 0.09 + rayLen * 0.4)
            );
        }

        WritableImage image = new WritableImage((int) w, (int) h);
        canvas.snapshot(null, image);
        return image;
    }
}





