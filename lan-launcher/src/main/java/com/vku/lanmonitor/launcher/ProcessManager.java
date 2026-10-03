package com.vku.lanmonitor.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProcessManager {
    private static final String BASE_DIR = "F:\\Do_An_Mang";

    private Process serverProcess;
    private final List<Process> childProcesses = new ArrayList<>();

    public boolean isServerRunning() {
        return serverProcess != null && serverProcess.isAlive();
    }

    public boolean isPortInUse(int port) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress("localhost", port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Process startServer(Consumer<String> logCallback) throws IOException {
        if (isServerRunning()) {
            logCallback.accept("[LAUNCHER] Server đã chạy rồi");
            return serverProcess;
        }

        File serverDir = new File(BASE_DIR, "lan-server");
        File serverJar = new File(serverDir, "target\\lan-server-0.0.1-SNAPSHOT.jar");

        if (!serverJar.exists()) {
            throw new IOException("Không tìm thấy Server JAR: " + serverJar.getAbsolutePath()
                    + "\nVui lòng build trước: cd lan-server && mvn clean package");
        }

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", serverJar.getAbsolutePath());
        pb.directory(serverDir);
        pb.redirectErrorStream(true);
        serverProcess = pb.start();

        final Process proc = serverProcess;
        Thread logReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logCallback.accept("[SERVER] " + line);
                }
                logCallback.accept("[LAUNCHER] Server đã tắt");
            } catch (Exception e) {
                if (proc.isAlive()) {
                    logCallback.accept("[LAUNCHER] Lỗi đọc log: " + e.getMessage());
                }
            }
        }, "ServerLogReader");
        logReader.setDaemon(true);
        logReader.start();

        return serverProcess;
    }

    public void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            try {
                if (!serverProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    serverProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Process launchApp(String moduleName, String jarName) throws IOException {
        File dir = new File(BASE_DIR, moduleName);
        File jar = new File(dir, "target\\" + jarName);

        if (!jar.exists()) {
            throw new IOException("Không tìm thấy JAR: " + jar.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder(
                "javaw", "-jar", jar.getAbsolutePath());
        pb.directory(dir);
        Process p = pb.start();
        childProcesses.add(p);
        return p;
    }

    public void stopAll() {
        stopServer();
        for (Process p : childProcesses) {
            if (p.isAlive()) {
                p.destroy();
            }
        }
    }

    public static String getBaseDir() {
        return BASE_DIR;
    }
}
