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
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.*;

public class CameraCaptureUtil {

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
            // Thêm timeout cho chắc chắn để không bị treo
            Boolean result = future.get(3, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Throwable e) {
            return false;
        } finally {
            executor.shutdownNow();
        }
    }

    public static String captureBase64Jpeg() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(CameraCaptureUtil::captureInternal);
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return null;
        } catch (Throwable e) {
            return null;
        } finally {
            executor.shutdownNow();
        }
    }

    private static String captureInternal() {
        Webcam webcam = null;
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                return null;
            }

            if (!webcam.isOpen()) {
                webcam.open();
            }

            BufferedImage image = webcam.getImage();
            if (image == null) {
                return null;
            }

            // Resize image to 640x480
            BufferedImage resizedImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(image, 0, 0, 640, 480, null);
            g2d.dispose();

            // Compress to JPEG with quality 0.7
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return null;
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
                writer.setOutput(mcios);
                writer.write(null, new IIOImage(resizedImage, null, null), param);
            }
            writer.dispose();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Throwable e) {
            return null;
        } finally {
            try {
                if (webcam != null && webcam.isOpen()) {
                    webcam.close();
                }
            } catch (Throwable t) {
                // Ignore exception on close
            }
        }
    }
}