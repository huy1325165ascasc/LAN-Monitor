# FULL CODEBASE - LAN Monitor & Exam System (VKU)

## TỔNG QUAN DỰ ÁN
- Hệ thống giám sát phòng thi qua mạng LAN
- 2 module: lan-client (Java desktop chạy trên máy SV) + lan-server (Spring Boot Web + TCP Server)
- Client kết nối TCP port 9999, gửi ảnh màn hình, giám sát cửa sổ theo whitelist
- Server: Dashboard giám thị (TailwindCSS), trang thi trắc nghiệm cho SV (Bootstrap), WebSocket real-time

---

## CẤU TRÚC THƯ MỤC

```
Do_An_Mang/
├── lan-client (1)/
│   ├── pom.xml
│   └── src/main/java/com/vku/lanmonitor/client/
│       ├── ClientApp.java
│       ├── service/SocketClientService.java
│       └── util/
│           ├── ActiveWindowUtil.java
│           └── ScreenCaptureUtil.java
└── lan-server/
    ├── pom.xml
    └── src/main/
        ├── java/com/vku/lanmonitor/server/
        │   ├── LanServerApplication.java
        │   ├── config/WebConfig.java, WebSocketConfig.java
        │   ├── controller/api/ClientApiController.java, QuizApiController.java, SettingsController.java
        │   ├── controller/web/AdminWebController.java, StudentWebController.java
        │   ├── model/ClientSession.java
        │   ├── socket/TcpServerService.java
        │   └── websocket/RealtimeNotificationService.java
        └── resources/
            ├── application.properties
            ├── static/css/ (style.css, login.css, quiz.css, quiz_result.css, take_quiz.css, toast.css)
            ├── static/js/ (app.js, login.js, quiz.js, quiz_result.js, student_home.js, take_quiz.js, toast.js)
            └── templates/ (dashboard.html, login.html, quiz.html, quiz_result.html, student_home.html, take_quiz.html)
```

---

# =============================================
# PHẦN 1: LAN-CLIENT (Java Desktop)
# =============================================

## FILE: lan-client/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.vku.lanmonitor</groupId>
    <artifactId>lan-client</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>net.java.dev.jna</groupId>
            <artifactId>jna-platform</artifactId>
            <version>5.14.0</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.vku.lanmonitor.client.ClientApp</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals><goal>single</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## FILE: ClientApp.java

```java
package com.vku.lanmonitor.client;

import com.vku.lanmonitor.client.service.SocketClientService;
import java.net.InetAddress;

public class ClientApp {
    public static void main(String[] args) {
        String pcName = "Unknown_PC";
        try {
            pcName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            pcName = System.getProperty("user.name", "Client_PC");
        }
        String serverIp = "10.54.144.242";
        int serverPort = 9999;
        SocketClientService clientService = new SocketClientService(serverIp, serverPort, pcName);
        clientService.start();
    }
}
```

## FILE: SocketClientService.java

```java
package com.vku.lanmonitor.client.service;

import com.vku.lanmonitor.client.util.ActiveWindowUtil;
import com.vku.lanmonitor.client.util.ScreenCaptureUtil;
import javax.swing.JOptionPane;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class SocketClientService {
    private final String serverIp;
    private final int serverPort;
    private final String studentId;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isRunning = false;
    private String lastAlertTab = "";
    private ScheduledExecutorService monitorScheduler;
    private List<String> whitelist = new ArrayList<>();

    public SocketClientService(String serverIp, int serverPort, String studentId) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.studentId = studentId;
    }

    public void start() {
        while (true) {
            try {
                System.out.println("Connecting to Server [" + serverIp + ":" + serverPort + "]...");
                socket = new Socket(serverIp, serverPort);
                isRunning = true;
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out.println("CONNECT:" + studentId);
                startBehaviorMonitor();
                String line;
                while (isRunning && (line = in.readLine()) != null) {
                    handleCommand(line);
                }
            } catch (Exception e) {
                System.err.println("Disconnect: " + e.getMessage() + ". Retry after 5s...");
            } finally {
                cleanup();
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void startBehaviorMonitor() {
        monitorScheduler = Executors.newSingleThreadScheduledExecutor();
        monitorScheduler.scheduleAtFixedRate(() -> {
            try {
                String activeTitle = ActiveWindowUtil.getActiveWindowTitle();
                if (activeTitle.isEmpty() || activeTitle.equals(lastAlertTab)) return;
                if (whitelist.isEmpty()) return;
                if (containsKeyword(activeTitle, whitelist)) { lastAlertTab = activeTitle; return; }
                out.println("SUSPICIOUS:RED|" + activeTitle);
                lastAlertTab = activeTitle;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private boolean containsKeyword(String title, List<String> keywords) {
        String lowerTitle = title.toLowerCase();
        for (String kw : keywords) {
            if (kw.trim().isEmpty()) continue;
            if (lowerTitle.contains(kw.toLowerCase().trim())) return true;
        }
        return false;
    }

    private void handleCommand(String command) {
        if (command.startsWith("ALERT:")) {
            String message = command.substring(6);
            new Thread(() -> JOptionPane.showMessageDialog(null, message, "Administrative Notification", JOptionPane.WARNING_MESSAGE)).start();
        } else if ("CAPTURE_SCREEN".equals(command)) {
            byte[] imgBytes = ScreenCaptureUtil.captureCurrentScreen();
            if (imgBytes != null && imgBytes.length > 0) {
                out.println("SCREEN:" + Base64.getEncoder().encodeToString(imgBytes));
            }
        } else if (command.startsWith("UPDATE_WHITELIST:")) {
            whitelist = new ArrayList<>(Arrays.asList(command.substring(17).split(",")));
        }
    }

    private void cleanup() {
        isRunning = false;
        if (monitorScheduler != null) monitorScheduler.shutdownNow();
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
```

## FILE: ActiveWindowUtil.java

```java
package com.vku.lanmonitor.client.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class ActiveWindowUtil {
    public static String getActiveWindowTitle() {
        HWND fgWindow = User32.INSTANCE.GetForegroundWindow();
        if (fgWindow == null) return "";
        int titleLength = User32.INSTANCE.GetWindowTextLength(fgWindow) + 1;
        char[] title = new char[titleLength];
        User32.INSTANCE.GetWindowText(fgWindow, title, titleLength);
        return Native.toString(title).trim();
    }
}
```

## FILE: ScreenCaptureUtil.java

```java
package com.vku.lanmonitor.client.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ScreenCaptureUtil {
    public static byte[] captureCurrentScreen() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenFullImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Lỗi khi chụp màn hình: " + e.getMessage());
            return new byte[0];
        }
    }
}
```
