# AI CONTEXT — LAN Monitor Network Exam System

## 🎯 1. PROJECT OVERVIEW
- **Project Name**: LAN Monitor (Mô Phỏng Thi Trực Tuyến Trên LAN)
- **Purpose**: Network-based exam monitoring system for educational institutions on local area networks
- **Users**: Students taking exams, Admins monitoring exam progress, IT staff managing exam infrastructure
- **Main Features**:
  - Student login and exam participation
  - Real-time quiz/exam distribution from server
  - Student progress tracking and quiz submission
  - Admin dashboard for monitoring and exam management
  - Anti-cheat monitoring (screenshot capture, screen monitoring)
  - Quiz CRUD operations (create, read, update, delete)

## 🛠️ 2. TECHNOLOGY STACK
- **Language**: Java 17
- **GUI Framework**: Swing (JFrame, JPanel, BoxLayout)
- **Networking**: TCP sockets with custom text-based protocol
- **Data Format**: JSON (Jackson ObjectMapper 2.17.2)
- **Build Tool**: Maven 3
- **Server Framework**: Spring Boot 4.1.1
- **Utilities**: Lombok 1.18.30, SLF4J logging
- **Architecture Pattern**: Client-Server (3-tier: Presentation, Network, Business Logic)
- **Encoding**: UTF-8 throughout

## 📁 3. PROJECT STRUCTURE
```
F:\Do_An_Mang
├── lan-server/                          # Server application (Spring Boot)
│   ├── src/main/java/com/vku/lanmonitor/server/
│   │   ├── LanServerApplication.java    # Spring Boot entry point
│   │   ├── socket/
│   │   │   ├── TcpServerService.java    # Main TCP server (port 9999)
│   │   │   │   └── ClientHandler (inner class)  # Per-client request handler
│   │   │   └── TcpBroadcastService.java # Broadcast/multicast utility
│   │   ├── service/
│   │   │   └── QuizService.java         # Quiz persistence (file-based JSON)
│   │   └── model/
│   │       └── ClientSession.java       # Client metadata (id, IP, pcName)
│   └── pom.xml
│
├── lan-student/                         # Student client (Swing GUI)
│   ├── src/main/java/com/vku/lanmonitor/student/
│   │   ├── StudentApp.java              # Main entry, Swing UIManager setup
│   │   ├── ui/
│   │   │   ├── LoginFrame.java          # Login UI (Phase 2 - Completed)
│   │   │   └── StudentHomeFrame.java    # Quiz list UI (Phase 3B - In Progress)
│   │   └── net/
│   │       └── StudentTcpClient.java    # TCP client wrapper, reader thread
│   └── pom.xml
│
├── lan-admin/                           # Admin client (stub)
│   ├── src/main/java/com/vku/lanmonitor/admin/
│   │   └── AdminApp.java                # Admin entry (not detailed in current phase)
│   └── pom.xml
│
├── lan-client (1)/                      # Generic/reference client (backup)
│   ├── src/main/java/com/vku/lanmonitor/client/
│   └── pom.xml
│
├── lan-student_backup_phase3A/          # Backup of Phase 3A state
├── lan-student_backup_phase2/           # Backup of Phase 2 state
├── lan-admin_backup_phase2/             # Backup of admin Phase 2
│
├── storage/                             # Runtime directory (created at startup)
│   ├── quizzes.json                     # All quiz definitions
│   ├── quiz_images/                     # Quiz images/attachments
│   ├── results/                         # Submitted quiz results
│   └── exam_rules.json                  # Exam rule configuration
│
├── images/                              # Runtime directory (for screenshots)
│   └── cheat_*.jpg                      # Captured screenshots for suspicious activity
│
└── .kilo/                               # Kilo IDE configuration
    └── agent/                           # Agent scripts (if any)
```

## 🏗️ 4. SYSTEM ARCHITECTURE

### Communication Model: CLIENT-SERVER over TCP
```
┌─────────────────┐                ┌──────────────────┐
│  StudentApp     │                │   AdminApp       │
│   (Swing GUI)   │                │   (Swing GUI)    │
└────────┬────────┘                └────────┬─────────┘
         │                                   │
         │  TCP Socket (port 9999)          │
         │  UTF-8 encoded text lines        │
         │                                   │
         ├───────────────────────┬──────────┤
         │                       │          │
    ┌────▼───────────────────────▼──────────▼─────┐
    │         TcpServerService                    │
    │  (ServerSocket listening on :9999)          │
    │  ├── ClientHandler #1 (Student thread)      │
    │  ├── ClientHandler #2 (Student thread)      │
    │  └── ClientHandler #N (Admin/Client thread) │
    └────┬──────────────────────────────────────┬─┘
         │                                      │
    ┌────▼──────────────┐         ┌────────────▼────┐
    │  QuizService      │         │ TcpBroadcast    │
    │  (Persistence)    │         │ Service         │
    └────┬──────────────┘         └────────────┬────┘
         │                                    │
    ┌────▼───────────────────────────────────▼──────┐
    │  storage/quizzes.json                        │
    │  storage/results/*.json                      │
    │  images/cheat_*.jpg                          │
    └─────────────────────────────────────────────┘
```

### Protocol: Text-Based Commands (newline-delimited)
```
CLIENT → SERVER:    STUDENT_LOGIN:24ITB217:123456
SERVER → CLIENT:    LOGIN_OK (or LOGIN_FAIL)

CLIENT → SERVER:    REQ_QUIZ_LIST
SERVER → CLIENT:    RES_QUIZ_LIST:[{"id":1790260345630,"name":"Quiz1",...}]

CLIENT → SERVER:    SUBMIT_QUIZ:{"studentId":"24ITB217","quizId":"1790260345630",...}
SERVER → CLIENT:    SUBMIT_OK (or SUBMIT_FAIL:reason)
```

### Layers
1. **UI Layer** (Swing JFrame/JPanel)
   - LoginFrame: student authentication UI
   - StudentHomeFrame: quiz selection UI
   - (AdminFrame: monitoring dashboard - TBD)

2. **Network Layer** (TCP sockets)
   - StudentTcpClient: client-side wrapper
   - TcpServerService: server-side listener
   - TcpBroadcastService: multicast/targeted messaging

3. **Business Logic** (Services)
   - QuizService: quiz CRUD and persistence
   - ClientSession: state tracking

4. **Data Layer** (File-based JSON)
   - storage/quizzes.json: quiz definitions
   - storage/results/: submission results
   - images/: screenshots for monitoring

## 🚀 5. ENTRY POINTS & EXECUTION FLOW

### Server Startup
```
1. LanServerApplication.main(args)
   ↓
2. SpringApplication.run(LanServerApplication.class, args)
   ↓
3. Spring injects TcpServerService, QuizService, TcpBroadcastService
   ↓
4. TcpServerService.run() (CommandLineRunner interface)
   ↓
5. startServer() spawns new Thread → ServerSocket listening on :9999
   ↓
6. Creates storage/ and images/ directories
   ↓
7. Enters while loop: serverSocket.accept() for each incoming client
   ↓
8. For each socket: threadPool.execute(new ClientHandler(socket))
   ↓
9. Each ClientHandler runs in thread pool, reads/writes commands
```

### Student Client Startup
```
1. StudentApp.main(args)
   ↓
2. UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
   ↓
3. SwingUtilities.invokeLater(() → new LoginFrame())
   ↓
4. LoginFrame shows: student ID input, password input, server settings button
   ↓
5. User enters credentials, clicks "Đăng nhập"
   ↓
6. LoginFrame.handleLogin():
   - Gets serverHost ("localhost") and serverPort (9999) from config
   - StudentTcpClient.connect(host, port)
   - Sends STUDENT_LOGIN:24ITB217:password
   - Listener waits for LOGIN_OK response
   ↓
7. Server ClientHandler validates → sends LOGIN_OK
   ↓
8. LoginFrame receives LOGIN_OK
   ↓
9. LoginFrame.openStudentHome(studentId):
   - Creates new StudentHomeFrame(studentId, host, port)
   - Closes LoginFrame
   ↓
10. StudentHomeFrame constructor:
    - Builds UI: north (header), center (scrollPane with quizPanel), south (status)
    - Spawns background thread → connectToServer()
    - Sets frame visible
    ↓
11. connectToServer():
    - tcpClient.connect(serverHost, serverPort)
    - Spawns reader thread in StudentTcpClient
    - Sends REQ_QUIZ_LIST command
    ↓
12. Server ClientHandler processes REQ_QUIZ_LIST:
    - quizService.loadAllQuizzes() from storage/quizzes.json
    - mapper.writeValueAsString(quizList)
    - Sends RES_QUIZ_LIST:[json]
    ↓
13. StudentHomeFrame.handleServerMessage() receives on EDT:
    - Extracts JSON from RES_QUIZ_LIST:...
    - Calls displayQuizzes(json)
    ↓
14. displayQuizzes():
    - mapper.readValue(json, List.class)
    - quizPanel.removeAll()
    - For each quiz: creates JPanel card with name, timeLimit, question count
    - Adds card to quizPanel
    - quizPanel.revalidate() and repaint()
    - scrollPane updated via full frame hierarchy
```

### Login Flow Sequence Diagram
```
┌─────────┐                ┌───────────────┐                ┌──────────┐
│ Student │                │  StudentApp   │                │  Server  │
└────┬────┘                └───────┬───────┘                └────┬─────┘
     │                             │                             │
     │                       LoginFrame created                   │
     │                             │                              │
     │  Types: 24ITB217 / 123456   │                              │
     │             click Login →   │                              │
     │                             │                              │
     │                    connect to localhost:9999               │
     │                             │  TCP SYN                     │
     │                             ├─────────────────────────────→│
     │                             │  TCP ACK                     │
     │                             │←─────────────────────────────┤
     │                             │                              │
     │                      STUDENT_LOGIN:24ITB217:123456         │
     │                             ├─────────────────────────────→│
     │                             │                              │
     │                             │      Validate credentials    │
     │                             │←─────────────────────────────┤
     │                             │  LOGIN_OK                    │
     │                             │                              │
     │                   StudentHomeFrame created                 │
     │                   LoginFrame closed                        │
     │                             │                              │
     │                      REQ_QUIZ_LIST                         │
     │                             ├─────────────────────────────→│
     │                             │                              │
     │                             │   Load quizzes from JSON     │
     │                             │←─────────────────────────────┤
     │                             │  RES_QUIZ_LIST:[...]         │
     │                             │                              │
     │                      Parse JSON & display cards            │
     │                             │                              │
```

## 📋 6. KEY FILES & MODULES

| File | Location | Role | Key Methods |
|------|----------|------|-------------|
| **LanServerApplication.java** | lan-server/src/.../server/ | Spring Boot entry point | main(), auto-starts TcpServerService |
| **TcpServerService.java** | lan-server/src/.../socket/ | Main TCP server, command router | run(), startServer(), ClientHandler.handleLine() |
| **TcpBroadcastService.java** | lan-server/src/.../socket/ | Multicast message dispatcher | broadcastToStudents(), broadcastToAdmins(), sendToClient() |
| **QuizService.java** | lan-server/src/.../service/ | Quiz persistence (file-based) | loadAllQuizzes(), saveAllQuizzes(), loadQuizById(), saveQuizResult() |
| **ClientSession.java** | lan-server/src/.../model/ | Client metadata model | id, ipAddress, pcName (fields) |
| **StudentApp.java** | lan-student/src/.../student/ | Student client launcher | main() |
| **LoginFrame.java** | lan-student/src/.../ui/ | Login UI + server config | handleLogin(), showSettingsDialog() |
| **StudentHomeFrame.java** | lan-student/src/.../ui/ | Quiz list UI, main app window | StudentHomeFrame(), connectToServer(), displayQuizzes() |
| **StudentTcpClient.java** | lan-student/src/.../net/ | TCP connection wrapper | connect(), disconnect(), sendCommand(), readMessages() |

## 🔄 7. DATA FLOW & PROTOCOL SPECIFICATION

### Command Reference (TcpServerService.ClientHandler.handleLine)

#### Authentication
```
STUDENT_LOGIN:<studentId>:<password>
  → Validates DEFAULT_PASSWORD ("123456")
  → Response: LOGIN_OK or LOGIN_FAIL
  → Role set to "STUDENT", clientId = studentId

ADMIN_LOGIN:<username>:<password>
  → Validates ADMIN_USER ("admin") + DEFAULT_PASSWORD ("123456")
  → Response: ADMIN_OK or ADMIN_FAIL
  → Role set to "ADMIN"

CONNECT:<pcName>
  → For monitoring clients
  → Creates ClientSession entry
  → Role set to "CLIENT"
```

#### Quiz Operations
```
REQ_QUIZ_LIST
  → Response: RES_QUIZ_LIST:<json array of all quizzes>
  → JSON format: [{"id": <long>, "name": <string>, "timeLimit": <int>, "type": <string>, "data": [...]}]

REQ_QUIZ_DETAIL:<quizId>
  → Response: RES_QUIZ_DETAIL:<json quiz object> or null

SAVE_QUIZ:<json quiz or json array>
  → Updates quiz in storage/quizzes.json (replace or add)
  → Response: SAVE_QUIZ_OK or SAVE_QUIZ_FAIL:<error>

DELETE_QUIZ:<quizId>
  → Removes quiz by ID
  → Response: DELETE_QUIZ_OK or DELETE_QUIZ_FAIL:<error>

SUBMIT_QUIZ:<json result>
  → Saves result to storage/results/<studentId>_<quizId>_<timestamp>.json
  → Response: SUBMIT_OK or SUBMIT_FAIL:<error>
```

#### Monitoring/Anti-Cheat
```
CAPTURE
  → Server requests screenshot from client

SEND_ALERT:<json alert>
  → Client sends suspicious activity alert

SCREEN:<base64 image data>
  → Client sends captured screen

SUSPICIOUS:<json suspicious data>
  → Client reports suspicious activity

REQ_CLIENT_LIST
  → Response: RES_CLIENT_LIST:<comma-separated client names>

UPDATE_WHITELIST_CONFIG:<json config>
  → Updates exam_rules.json
```

#### System
```
REQ_WHITELIST
  → Response: RES_WHITELIST:<comma-separated whitelist>
```

### JSON Schema Examples

#### Quiz Object
```json
{
  "id": 1790260345630,
  "name": "Công nghệ thông tin",
  "timeLimit": 50,
  "type": "delayed",
  "data": [
    {
      "question": "Câu hỏi 1?",
      "options": ["A", "B", "C", "D"],
      "correct": 0
    }
  ]
}
```

#### Quiz List Response
```json
[
  {
    "id": 1790260345630,
    "name": "Công nghệ thông tin",
    "timeLimit": 50,
    "type": "delayed",
    "data": [...]
  },
  ...
]
```

#### Quiz Result (Submission)
```json
{
  "studentId": "24ITB217",
  "quizId": "1790260345630",
  "answers": [0, 1, 2, ...],
  "timeSpent": 1800,
  "timestamp": 1790260345630
}
```

## 🔑 8. CONFIGURATION & HARDCODED VALUES

| Component | Key | Value | Location | Purpose |
|-----------|-----|-------|----------|---------|
| **Server** | LISTEN_PORT | 9999 | TcpServerService:74 | TCP server port |
| **Server** | ADMIN_USER | "admin" | TcpServerService:35 | Admin login username |
| **Server** | DEFAULT_PASSWORD | "123456" | TcpServerService:36 | Universal password (all users) |
| **Server** | STORAGE_DIR | "storage" | TcpServerService:37 | Quiz/result storage directory |
| **Server** | QUIZ_FILE | "storage/quizzes.json" | QuizService:20 | Quiz database file |
| **Student** | serverHost | "localhost" | LoginFrame:17 | Default server IP |
| **Student** | serverPort | 9999 | LoginFrame:18 | Default server port |
| **Student** | ENCODING | UTF-8 | StudentTcpClient:7 | Text encoding for TCP |
| **UI** | NIMBUS_LAF | "javax.swing.plaf.nimbus.NimbusLookAndFeel" | StudentApp:10 | Swing look and feel |

## ⚙️ 9. BUILD & RUN INSTRUCTIONS

### Prerequisites
- Java 17+ (JDK)
- Maven 3.6+
- Network: both machines on same LAN or localhost for testing

### Build Server
```bash
cd F:\Do_An_Mang\lan-server
mvn clean package
# Output: target/lan-server-0.0.1-SNAPSHOT.jar
```

### Build Student Client
```bash
cd F:\Do_An_Mang\lan-student
mvn clean package
# Output: target/lan-student-1.0.0-jar-with-dependencies.jar
```

### Run Server (Terminal 1)
```bash
cd F:\Do_An_Mang\lan-server
java -jar target/lan-server-0.0.1-SNAPSHOT.jar
# Logs: ">>> Đã mở cổng 9999 đón sinh viên vào thi <<<"
```

### Run Student Client (Terminal 2+)
```bash
cd F:\Do_An_Mang\lan-student
java -jar target/lan-student-1.0.0-jar-with-dependencies.jar
# OR run multiple for multiple students:
#   java -jar target/lan-student-1.0.0-jar-with-dependencies.jar
#   java -jar target/lan-student-1.0.0-jar-with-dependencies.jar
# Login: studentId=24ITB217, password=123456
```

### Test Connection (Manual)
```bash
# Check server listening:
netstat -an | findstr :9999

# Test TCP connection:
telnet localhost 9999
# Type: STUDENT_LOGIN:24ITB217:123456
# Expect: LOGIN_OK
```

### Environment Variables (None required - all hardcoded)
All configuration is currently hardcoded. To externalize:
1. Create application.properties in lan-server/src/main/resources/
2. Create config.properties in lan-student/ for UI defaults

## 📊 10. PROJECT PHASES & STATUS

### Phase 1: Architecture & Setup (✅ Completed)
- Project structure created (server, student, admin)
- Maven build files configured
- Spring Boot server initialized
- TCP server skeleton implemented

### Phase 2: Student Login (✅ Completed)
- LoginFrame UI with student ID + password fields
- TCP client connection to server
- STUDENT_LOGIN command sent
- LOGIN_OK/LOGIN_FAIL response handling
- LoginFrame → StudentHomeFrame transition
- Server-side login validation

### Phase 3A: Quiz List Display (✅ Completed)
- StudentHomeFrame created with BorderLayout
- Header (north): greeting + logout button
- Center: JScrollPane with JPanel (quizPanel)
- Status bar (south): connection status
- REQ_QUIZ_LIST command sent on frame creation
- Background thread for server communication
- Message listener pattern implemented

### Phase 3B: Quiz Card Rendering (🔄 In Progress)
- JSON parsing with Jackson ObjectMapper
- Quiz card creation: name, timeLimit, question count
- CardLayout or BoxLayout for quiz cards
- UI rendering and revalidate/repaint
- ✅ Fixed: EDT blocking issues with double disconnect()
- ✅ Fixed: displayQuizzes() now properly revalidates full hierarchy
- ✅ Fixed: Cards now render with Timer-based repaint in some cases
- ⚠️ Status: Cards display correctly, but may need further UI polish

### Phase 3C: Quiz Detail & Submission (❌ Not Started)
- QuizDetailFrame for viewing questions
- Answer selection UI
- Submit button and validation
- SUBMIT_QUIZ command and result persistence

### Phase 4: Admin Monitoring (❌ Not Started)
- AdminApp with monitoring dashboard
- Real-time student progress display
- Screenshot viewing capability
- Alert management
- Exam control (pause, stop, extend)

### Phase 5: Anti-Cheat Features (❌ Not Started)
- Client-side screenshot capture
- Suspicious activity detection (alt-tab, etc.)
- Server-side alert aggregation
- Whitelist management for allowed apps

## 🧠 11. KEY DESIGN PATTERNS & ANTI-PATTERNS

### Good Patterns
✅ **Listener Pattern**: StudentTcpClient uses Consumer<String> messageListener for decoupled message handling

✅ **Thread Pool**: TcpServerService uses ExecutorService for concurrent client handling

✅ **Separation of Concerns**: UI (LoginFrame), Network (StudentTcpClient), Business (QuizService)

✅ **Spring Boot CommandLineRunner**: Automatic server startup without explicit thread management in main

✅ **JSON Serialization**: Jackson ObjectMapper for type-safe data exchange

### Anti-Patterns / Improvements Needed
⚠️ **Hardcoded Configuration**: Server IP/port/password should be externalized to properties files

⚠️ **No Connection Recovery**: If server disconnects mid-session, no automatic reconnect

⚠️ **Minimal Error Handling**: Some UI updates may fail silently; add defensive null checks

⚠️ **Thread Sleep in EDT**: Some debug code uses Thread.sleep() blocking the event dispatcher

⚠️ **No Logging Framework in Client**: StudentTcpClient uses System.err/out; should use SLF4J

⚠️ **File-Based Persistence**: QuizService stores all data in single JSON file; no database, no transactions

⚠️ **Default Password "123456"**: Not secure; should use proper authentication (hashing, tokens)

⚠️ **No Request/Response Validation**: Missing checks for malformed JSON or command injection

⚠️ **Single Threaded UI Updates**: Some operations may block Swing EDT

## ⚠️ 12. KNOWN ISSUES & RECENT FIXES

### Recently Fixed (Phase 3B)
✅ **EDT Blocking from Double disconnect()**: Removed redundant disconnect() call in LoginFrame.openStudentHome(); now uses single disconnect in finally block

✅ **Quiz Cards Not Rendering**: Fixed by ensuring displayQuizzes() is called on EDT via SwingUtilities.invokeLater()

✅ **Layout Not Updating**: Fixed by calling revalidate()/repaint() on full frame hierarchy (frame.revalidate() not just panel)

✅ **Jackson Parsing Errors**: JSON now properly formatted and parsed as List<Map<String,Object>>

### Current Issues
⚠️ **Timer-Based Repaints**: Some UI rendering still relies on delayed timers; should investigate why immediate repaint() doesn't work

⚠️ **Build Artifacts**: [DEBUG] and [BUILD] prefixes in console output are debug artifacts; should be removed before commit

### Workarounds Currently in Place
- setVisible(true) called last in StudentHomeFrame to ensure layout is computed
- full component hierarchy revalidate() before repaint()
- SwingUtilities.invokeLater() for all UI updates from background threads

## 🚨 13. CRITICAL ZONES (DO NOT MODIFY WITHOUT UNDERSTANDING)

### Server Port Protocol
- Port 9999 is hardcoded in multiple places (TcpServerService, LoginFrame)
- Changing it requires coordinating both client and server
- Changing protocol format breaks all client communication

### Student Credentials
- Default password is "123456" (hardcoded, NOT production-grade)
- Changing server-side password requires updating all test clients
- Currently no hashing or encryption

### Quiz Data Format
- JSON schema matches server persistence; changes require migration
- Client assumes specific structure (id, name, timeLimit, type, data)
- Add new fields only at end of object to maintain backward compatibility

### Message Protocol
- Commands are newline-delimited plain text (no binary encoding)
- Adding new commands requires updating ClientHandler.handleLine()
- Payload extraction uses substring() — fragile to protocol changes

## 📌 14. NOTES FOR AI READING THIS FILE

**This is a SWING GUI + TCP network exam system for educational use.**

### When Adding Features
1. ✅ Always test both server and student client together
2. ✅ Use SwingUtilities.invokeLater() for ALL UI updates from background threads
3. ✅ Network operations must run in background threads (not EDT)
4. ✅ Call revalidate()/repaint() on full frame hierarchy when UI structure changes significantly
5. ✅ Keep commands newline-delimited and UTF-8 encoded
6. ✅ Store all persistent data in storage/ directory (created at startup)

### When Modifying Code
1. ❌ Do NOT commit debug logs with [DEBUG], [BUILD], or print statements
2. ❌ Do NOT hardcode new IP addresses or ports
3. ❌ Do NOT use Thread.sleep() to sync UI updates
4. ❌ Do NOT modify student credential validation logic without understanding security implications
5. ❌ Do NOT change JSON schema without updating both client and server

### Code Style Guidelines
- **UI Code**: Use BoxLayout for vertical stacking, BorderLayout for main containers
- **Network Code**: Follow StudentTcpClient pattern (connect, disconnect, sendCommand, setMessageListener)
- **Commands**: Use uppercase for command names, colon-delimited for payloads (COMMAND:payload)
- **Comments**: Mix of English and Vietnamese; use English for technical comments, Vietnamese for UI labels

### Testing Checklist
- [ ] Server starts on port 9999 without errors
- [ ] Student can login with ID/password
- [ ] LoginFrame successfully transitions to StudentHomeFrame
- [ ] Quiz list loads and displays correctly
- [ ] All commands round-trip correctly (request sent, response received)
- [ ] Closing app doesn't crash server
- [ ] Multiple students can connect simultaneously
- [ ] No EDT blocking or UI freezing

### Language Mix
- **Code comments**: English preferred, Vietnamese acceptable for UI-specific comments
- **UI labels/messages**: Vietnamese (for end users)
- **Protocol/commands**: English (for technical consistency)
- **Log messages**: Mix (server logs use Vietnamese for readability)

## 📈 15. CODEBASE STATISTICS

| Metric | Value |
|--------|-------|
| **Total Java files (server)** | 5 |
| **Total Java files (student)** | 4 |
| **Total Java files (admin)** | 1 (stub) |
| **Total nodes (graph)** | 2,572 |
| **Total edges (graph)** | 9,486 |
| **Primary language** | Java 17 |
| **Build tool** | Maven 3 |
| **Server framework** | Spring Boot 4.1.1 |
| **GUI framework** | Swing (standard library) |
| **Main dependencies** | Jackson 2.17.2, Lombok 1.18.30, SLF4J |
| **Encoding** | UTF-8 |
| **Minimum Java version** | Java 17 |

---

**Document Version**: 1.0
**Last Updated**: 2026-10-01
**Indexed Project**: LAN-Monitor (F:\Do_An_Mang)
**Graph Nodes**: 2,572 | **Graph Edges**: 9,486
**Coverage**: Full architecture, entry points, protocols, and known issues documented
