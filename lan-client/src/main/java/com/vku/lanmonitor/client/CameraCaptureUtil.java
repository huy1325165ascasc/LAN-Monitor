package com.vku.lanmonitor.client;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Utility class for webcam capture operations.
 * Supports single snapshot capture and short video clip recording (5s).
 * Uses webcam-capture (sarxos) library.
 */
public class CameraCaptureUtil {

    private static Webcam sharedWebcam = null;
    private static final Object WEBCAM_LOCK = new Object();

    /**
     * Check if a webcam is available on this machine.
     * Uses timeout to avoid blocking if webcam driver is unresponsive.
     */
    public static boolean isCameraAvailable() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    return Webcam.getDefault() != null;
                } catch (Throwable e) {
                    return false;
                }
            });
            Boolean result = future.get(3, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Throwable e) {
            return false;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Open the shared webcam instance (keeps it open for faster repeated captures).
     * Call this once during startup, then use captureBase64Jpeg() for snapshots.
     */
    public static boolean openCamera() {
        synchronized (WEBCAM_LOCK) {
            try {
                if (sharedWebcam != null && sharedWebcam.isOpen()) {
                    return true;
                }
                sharedWebcam = Webcam.getDefault();
                if (sharedWebcam == null) {
                    return false;
                }
                sharedWebcam.open();
                return sharedWebcam.isOpen();
            } catch (Throwable e) {
                System.err.println("[CAMERA] Failed to open webcam: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Close the shared webcam instance. Call on shutdown.
     */
    public static void closeCamera() {
        synchronized (WEBCAM_LOCK) {
            try {
                if (sharedWebcam != null && sharedWebcam.isOpen()) {
                    sharedWebcam.close();
                }
            } catch (Throwable ignored) {
            } finally {
                sharedWebcam = null;
            }
        }
    }

    /**
     * Capture a single webcam frame and return as Base64-encoded JPEG string.
     * Image is resized to 640x480 and compressed at 0.7 quality for network efficiency.
     *
     * @return Base64 JPEG string, or null if capture fails
     */
    public static String captureBase64Jpeg() {
        synchronized (WEBCAM_LOCK) {
            try {
                if (sharedWebcam == null || !sharedWebcam.isOpen()) {
                    if (!openCamera()) {
                        return null;
                    }
                }
                BufferedImage image = sharedWebcam.getImage();
                if (image == null) {
                    return null;
                }
                return encodeImageToBase64(image, 640, 480, 0.7f);
            } catch (Throwable e) {
                System.err.println("[CAMERA] Capture failed: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Record a short video clip (approximately 5 seconds) from webcam.
     * Captures frames at ~5 FPS, encodes each as compressed JPEG,
     * and returns a list of Base64-encoded frames.
     *
     * The server will reassemble these frames or save them individually.
     *
     * @param durationSeconds how many seconds to record (default 5)
     * @param fps target frames per second (default 5)
     * @return list of Base64 JPEG strings (one per frame), or empty list on failure
     */
    public static List<String> recordClipFrames(int durationSeconds, int fps) {
        List<String> frames = new ArrayList<>();
        synchronized (WEBCAM_LOCK) {
            try {
                if (sharedWebcam == null || !sharedWebcam.isOpen()) {
                    if (!openCamera()) {
                        return frames;
                    }
                }

                int totalFrames = durationSeconds * fps;
                long intervalMs = 1000L / fps;

                System.out.println("[CAMERA] Recording " + durationSeconds + "s clip (" + totalFrames + " frames)...");

                for (int i = 0; i < totalFrames; i++) {
                    long frameStart = System.currentTimeMillis();

                    BufferedImage image = sharedWebcam.getImage();
                    if (image != null) {
                        // Smaller resolution for clip frames to reduce bandwidth
                        String base64 = encodeImageToBase64(image, 320, 240, 0.5f);
                        if (base64 != null) {
                            frames.add(base64);
                        }
                    }

                    // Maintain target FPS
                    long elapsed = System.currentTimeMillis() - frameStart;
                    long sleepTime = intervalMs - elapsed;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }

                System.out.println("[CAMERA] Clip recorded: " + frames.size() + " frames captured");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[CAMERA] Clip recording interrupted");
            } catch (Throwable e) {
                System.err.println("[CAMERA] Clip recording failed: " + e.getMessage());
            }
        }
        return frames;
    }

    /**
     * Record a 5-second clip and return all frames concatenated as a single
     * Base64 string with frame separator "|||".
     * This is optimized for sending over the text-based TCP protocol.
     *
     * @return concatenated Base64 frames separated by "|||", or null if failed
     */
    public static String recordClipAsString() {
        List<String> frames = recordClipFrames(5, 5);
        if (frames.isEmpty()) {
            return null;
        }
        return String.join("|||", frames);
    }

    /**
     * Encode a BufferedImage to Base64 JPEG with specified dimensions and quality.
     */
    private static String encodeImageToBase64(BufferedImage image, int width, int height, float quality) {
        try {
            // Resize
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(image, 0, 0, width, height, null);
            g2d.dispose();

            // Compress to JPEG
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return null;
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(mcios);
                writer.write(null, new IIOImage(resized, null, null), param);
            }
            writer.dispose();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Throwable e) {
            System.err.println("[CAMERA] Image encoding failed: " + e.getMessage());
            return null;
        }
    }
}