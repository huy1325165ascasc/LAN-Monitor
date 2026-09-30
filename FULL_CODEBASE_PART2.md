# =============================================
# PHẦN 2: LAN-SERVER (Spring Boot)
# =============================================

## FILE: lan-server/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.1.1</version>
		<relativePath/>
	</parent>
	<groupId>com.vku.lanmonitor</groupId>
	<artifactId>lan-server</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-thymeleaf</artifactId></dependency>
		<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-webmvc</artifactId></dependency>
		<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>
		<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-devtools</artifactId><scope>runtime</scope><optional>true</optional></dependency>
		<dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
		<dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId></dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
		</plugins>
	</build>
</project>
```

## FILE: application.properties

```properties
spring.application.name=lan-server
```

## FILE: LanServerApplication.java

```java
package com.vku.lanmonitor.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LanServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(LanServerApplication.class, args);
	}
}
```

## FILE: WebConfig.java

```java
package com.vku.lanmonitor.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imagesPath = new File("images").getAbsolutePath();
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + imagesPath + "/");
        String tempImagesPath = new File("temp_images").getAbsolutePath();
        registry.addResourceHandler("/temp_images/**").addResourceLocations("file:" + tempImagesPath + "/");
    }
}
```

## FILE: WebSocketConfig.java

```java
package com.vku.lanmonitor.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
```

## FILE: ClientSession.java

```java
package com.vku.lanmonitor.server.model;

import lombok.Data;

@Data
public class ClientSession {
    private String id;
    private String ipAddress;
    private String pcName;

    public ClientSession(String id, String ipAddress, String pcName) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.pcName = pcName;
    }
}
```

## FILE: TcpServerService.java

```java
package com.vku.lanmonitor.server.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.server.model.ClientSession;
import com.vku.lanmonitor.server.websocket.RealtimeNotificationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcpServerService implements CommandLineRunner {

    private final RealtimeNotificationService notificationService;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Map<String, ClientSession> activeClients = new ConcurrentHashMap<>();
    private final Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> captureRequests = new ConcurrentHashMap<>();

    public List<ClientSession> getAllClients() { return new ArrayList<>(activeClients.values()); }

    public void sendCommand(String clientId, String cmd) {
        if (clientWriters.containsKey(clientId)) clientWriters.get(clientId).println(cmd);
    }

    public void broadcast(String cmd) { clientWriters.values().forEach(out -> out.println(cmd)); }

    public void requestCapture(String clientId) { captureRequests.put(clientId, true); }

    @Override
    public void run(String... args) { new Thread(this::startServer).start(); }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(9999);
            isRunning = true;
            log.info(">>> Đã mở cổng 9999 đón sinh viên vào thi <<<");
            Files.createDirectories(Paths.get("images"));
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) { log.error("Lỗi Server: {}", e.getMessage()); }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                ClientSession session = new ClientSession(clientId, socket.getInetAddress().getHostAddress(), "Chưa nhập tên");
                activeClients.put(clientId, session);
                clientWriters.put(clientId, out);
                notificationService.notifyClientListUpdated(getAllClients());

                File ruleFile = new File("storage/exam_rules.json");
                if (ruleFile.exists()) {
                    String[] rules = new ObjectMapper().readValue(ruleFile, String[].class);
                    out.println("UPDATE_WHITELIST:" + String.join(",", rules));
                }

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("CONNECT:")) {
                        session.setPcName(line.substring(8));
                        notificationService.notifyClientListUpdated(getAllClients());
                    } else if (line.startsWith("SCREEN:")) {
                        String base64Image = line.substring(7);
                        notificationService.sendScreenshotToWeb(clientId, base64Image);
                        if (captureRequests.getOrDefault(clientId, false)) {
                            captureRequests.put(clientId, false);
                            String fName = "cheat_" + clientId.replace(":", "_") + "_" + System.currentTimeMillis() + ".jpg";
                            Files.write(Paths.get("images", fName), Base64.getDecoder().decode(base64Image));
                        }
                    } else if (line.startsWith("SUSPICIOUS:")) {
                        String[] parts = line.substring(11).split("\\|", 2);
                        if (parts.length == 2)
                            notificationService.sendSuspiciousAlertToWeb(clientId, session.getPcName(), parts[0], parts[1], null);
                    }
                }
            } catch (Exception e) {
            } finally {
                activeClients.remove(clientId);
                clientWriters.remove(clientId);
                captureRequests.remove(clientId);
                notificationService.notifyClientListUpdated(getAllClients());
            }
        }
    }

    @PreDestroy
    public void stopServer() {
        isRunning = false;
        try { if (serverSocket != null) serverSocket.close(); threadPool.shutdown(); } catch (Exception ignored) {}
    }
}
```

## FILE: RealtimeNotificationService.java

```java
package com.vku.lanmonitor.server.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyClientListUpdated(Object clients) {
        messagingTemplate.convertAndSend("/topic/clients", clients);
    }

    public void sendScreenshotToWeb(String clientId, String base64Image) {
        messagingTemplate.convertAndSend("/topic/screenshot/" + clientId, base64Image);
    }

    public void sendSuspiciousAlertToWeb(String clientId, String pcName, String severity, String tabName, String imageUrl) {
        java.util.Map<String, String> alertData = new java.util.HashMap<>();
        alertData.put("clientId", clientId);
        alertData.put("pcName", pcName != null ? pcName : "Máy Trạm");
        alertData.put("severity", severity);
        alertData.put("tabName", tabName);
        alertData.put("imageUrl", imageUrl);
        messagingTemplate.convertAndSend("/topic/alerts", alertData);
    }
}
```

## FILE: ClientApiController.java

```java
package com.vku.lanmonitor.server.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vku.lanmonitor.server.socket.TcpServerService;
import java.io.File;
import java.awt.Desktop;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientApiController {
    private final TcpServerService tcpServer;

    @GetMapping
    public ResponseEntity<?> getClients() { return ResponseEntity.ok(tcpServer.getAllClients()); }

    @PostMapping("/{clientId}/capture")
    public ResponseEntity<?> requestScreenshot(@PathVariable String clientId) {
        tcpServer.sendCommand(clientId, "CAPTURE_SCREEN");
        return ResponseEntity.ok(Map.of("message", "Đã gửi lệnh chụp màn hình"));
    }

    @PostMapping("/{clientId}/alert")
    public ResponseEntity<?> sendAlert(@PathVariable String clientId, @RequestBody Map<String, String> body) {
        tcpServer.sendCommand(clientId, "ALERT:" + body.getOrDefault("message", ""));
        return ResponseEntity.ok(Map.of("message", "Đã gửi cảnh báo thành công"));
    }

    @PostMapping("/{clientId}/save-frame")
    public ResponseEntity<?> saveLivestreamFrame(@PathVariable String clientId) {
        tcpServer.requestCapture(clientId);
        return ResponseEntity.ok(Map.of("message", "Đã yêu cầu lưu ảnh Livestream"));
    }

    @PostMapping("/open-images-folder")
    public ResponseEntity<?> openImagesFolder() {
        try {
            File dir = new File("images");
            if (!dir.exists()) dir.mkdirs();
            Desktop.getDesktop().open(dir);
            return ResponseEntity.ok(Map.of("message", "Đã mở thư mục ảnh"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
```

## FILE: QuizApiController.java

```java
package com.vku.lanmonitor.server.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
public class QuizApiController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String DIR = "storage";
    private final String QUIZ_FILE = DIR + "/quizzes.json";

    public QuizApiController() {
        try {
            Files.createDirectories(Paths.get(DIR));
            Files.createDirectories(Paths.get(DIR + "/quiz_images"));
            Files.createDirectories(Paths.get(DIR + "/results"));
        } catch (Exception e) { log.error("Không thể tạo thư mục storage: ", e); }
    }

    @PostMapping
    public ResponseEntity<?> saveQuizzes(@RequestBody List<Object> quizzes) {
        try {
            mapper.writeValue(new File(QUIZ_FILE), quizzes);
            return ResponseEntity.ok(Map.of("message", "Đã lưu đề thi thành công!"));
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    @GetMapping
    public ResponseEntity<List<Object>> getQuizzes() {
        try {
            File file = new File(QUIZ_FILE);
            if (file.exists()) return ResponseEntity.ok(mapper.readValue(file, List.class));
        } catch (Exception e) {}
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuizResult(@RequestBody Map<String, Object> resultData) {
        try {
            String studentId = (String) resultData.getOrDefault("studentId", "UNKNOWN");
            String quizId = String.valueOf(resultData.get("quizId"));
            String fileName = DIR + "/results/" + studentId + "_" + quizId + "_" + System.currentTimeMillis() + ".json";
            mapper.writeValue(new File(fileName), resultData);
            return ResponseEntity.ok(Map.of("message", "Nộp bài thành công!"));
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    @DeleteMapping("/image/{fileName}")
    public ResponseEntity<?> deleteImage(@PathVariable String fileName) {
        try {
            File img = new File(DIR + "/quiz_images/" + fileName);
            if (img.exists()) img.delete();
            return ResponseEntity.ok(Map.of("message", "Đã xóa ảnh vật lý"));
        } catch (Exception e) { return ResponseEntity.internalServerError().build(); }
    }
}
```

## FILE: SettingsController.java

```java
package com.vku.lanmonitor.server.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vku.lanmonitor.server.socket.TcpServerService;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final TcpServerService tcpServer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String STORAGE_DIR = "storage";
    private final String RULES_FILE = STORAGE_DIR + "/exam_rules.json";

    @GetMapping("/whitelist")
    public ResponseEntity<List<String>> getWhitelist() {
        try {
            File file = new File(RULES_FILE);
            if (file.exists()) return ResponseEntity.ok(Arrays.asList(objectMapper.readValue(file, String[].class)));
        } catch (Exception e) {}
        return ResponseEntity.ok(Arrays.asList("Bài Thi Trắc Nghiệm", "Word", "Calculator", "Excel"));
    }

    @PostMapping("/whitelist")
    public ResponseEntity<?> updateWhitelist(@RequestBody Map<String, List<String>> body) {
        List<String> newWhitelist = body.get("whitelist");
        if (newWhitelist == null || newWhitelist.isEmpty())
            return ResponseEntity.badRequest().body("Danh sách trắng không được để trống");
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            objectMapper.writeValue(new File(RULES_FILE), newWhitelist);
            tcpServer.broadcast("UPDATE_WHITELIST:" + String.join(",", newWhitelist));
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật danh sách và gửi lệnh!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}
```

## FILE: AdminWebController.java

```java
package com.vku.lanmonitor.server.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminWebController {
    @GetMapping
    public String adminPanel() { return "redirect:/admin/monitor"; }

    @GetMapping("/monitor")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/quizzes")
    public String quizManager() { return "quiz"; }
}
```

## FILE: StudentWebController.java

```java
package com.vku.lanmonitor.server.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentWebController {
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String studentWaitingRoom() { return "student_home"; }

    @GetMapping("/take_quiz")
    public String takeQuiz() { return "take_quiz"; }

    @GetMapping("/quiz_result")
    public String quizResult() { return "quiz_result"; }
}
```
