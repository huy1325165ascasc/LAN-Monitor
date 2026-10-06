package com.vku.lanmonitor.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CameraCaptureUtilTest {

    @Test
    public void testIsCameraAvailable_NoException() {
        assertDoesNotThrow(() -> {
            boolean available = CameraCaptureUtil.isCameraAvailable();
            // Nó có thể true hoặc false tùy vào máy tính có webcam hay không
            // Nhưng quan trọng là không được crash (throw exception)
        });
    }

    @Test
    public void testCaptureBase64Jpeg_NoExceptionAndValidBase64() {
        assertDoesNotThrow(() -> {
            String base64 = CameraCaptureUtil.captureBase64Jpeg();
            if (base64 != null) {
                // Nếu trả về chuỗi, nó phải chạy được Base64 decode mà không lỗi (tức là valid base64)
                java.util.Base64.getDecoder().decode(base64);
            }
        });
    }
}