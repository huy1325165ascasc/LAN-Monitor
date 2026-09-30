# BÁO CÁO PHÂN TÍCH KIẾN TRÚC TOÀN BỘ DỰ ÁN LAN MONITOR & QUIZ SYSTEM

> **Dự án:** LAN Monitoring & Remote Management System  
> **Phạm vi phân tích:** `lan-client` và `lan-server`  
> **Mục tiêu:** Đánh giá hiện trạng toàn bộ codebase và lập kế hoạch chuyển đổi hệ thống từ mô hình Web-based (Spring Boot REST + STOMP WebSocket) sang mô hình ứng dụng thuần mạng Socket TCP / Desktop Client-Server.

---

## 1. Bảng Liệt Kê Tất Cả File Java Hiện Có

| STT | Module | File / Class | Package | LOC (Total / Code) | Chức năng chi tiết |
|---|---|---|---|---|---|
| 1 | `lan-client` | `ClientApp.java`<br>`class ClientApp` | `com.vku.lanmonitor.client` | 27 / 21 | Entry point của ứng dụng Client trên máy trạm sinh viên. Tự động lấy hostname (`InetAddress.getLocalHost().getHostName()`) làm định danh mặc định, thiết lập IP/Port server (mặc định `10.54.144.242:9999`) và khởi chạy `SocketClientService`. |
| 2 | `lan-client` | `SocketClientService.java`<br>`class SocketClientService` | `com.vku.lanmonitor.client.service` | 152 / 132 | Quản lý kết nối TCP Socket tới Server. Gửi lệnh báo danh `CONNECT:<studentId>`; chạy ScheduledExecutor định kỳ 1 giây quét tiêu đề cửa sổ đang active qua `ActiveWindowUtil`, đối chiếu Whitelist để gửi cảnh báo `SUSPICIOUS:RED\|<title>`; lắng nghe và xử lý lệnh từ Server: `ALERT:<msg>` (bật JOptionPane), `CAPTURE_SCREEN` (gửi `SCREEN:<base64>`), `UPDATE_WHITELIST:<rules>`. |
| 3 | `lan-client` | `ActiveWindowUtil.java`<br>`class ActiveWindowUtil` | `com.vku.lanmonitor.client.util` | 21 / 19 | Sử dụng JNA (Java Native Access) tương tác với Windows API (`User32.dll` qua các hàm `GetForegroundWindow`, `GetWindowTextLength`, `GetWindowText`) để lấy tiêu đề của cửa sổ/tiến trình đang active trên máy tính. |
| 4 | `lan-client` | `ScreenCaptureUtil.java`<br>`class ScreenCaptureUtil` | `com.vku.lanmonitor.client.util` | 25 / 21 | Sử dụng `java.awt.Robot` và `Toolkit` chụp toàn bộ màn hình máy trạm, nén thành định dạng ảnh JPG dạng byte array (`byte[]`) phục vụ gửi về server qua Socket. |
| 5 | `lan-server` | `LanServerApplication.java`<br>`class LanServerApplication` | `com.vku.lanmonitor.server` | 13 / 9 | Class chính khởi chạy Spring Boot Application của Server (Spring Boot 4.x). |
| 6 | `lan-server` | `LanServerApplicationTests.java`<br>`class LanServerApplicationTests` | `com.vku.lanmonitor.server` | 13 / 9 | Unit test kiểm tra khả năng nạp Spring Application Context (`@SpringBootTest`). |
| 7 | `lan-server` | `ClientSession.java`<br>`class ClientSession` | `com.vku.lanmonitor.server.model` | 16 / 13 | Model dữ liệu lưu thông tin phiên làm việc của Client kết nối vào TCP Server: `id` (chuỗi dạng `IP:Port`), `ipAddress`, `pcName` (Mã sinh viên / tên máy). |
| 8 | `lan-server` | `TcpServerService.java`<br>`class TcpServerService`<br>`class ClientHandler` | `com.vku.lanmonitor.server.socket` | 160 / 139 | Dịch vụ TCP Socket Server mở cổng 9999 trên nền `ServerSocket`. Sử dụng Cached ThreadPool để phục vụ đồng thời nhiều Client thông qua `ClientHandler`. Nhận và xử lý: `CONNECT:`, `SCREEN:`, `SUSPICIOUS:`; phát sóng `UPDATE_WHITELIST:`; gửi hình ảnh/cảnh báo sang WebSocket; lưu ảnh chụp vào thư mục `images/`. |
| 9 | `lan-server` | `RealtimeNotificationService.java`<br>`class RealtimeNotificationService` | `com.vku.lanmonitor.server.websocket` | 44 / 37 | Dịch vụ trung gian sử dụng `SimpMessagingTemplate` để đẩy thông điệp từ tầng TCP Socket lên Web UI qua các topic WebSocket: `/topic/clients`, `/topic/screenshot/{clientId}`, `/topic/alerts`. |
| 10 | `lan-server` | `WebSocketConfig.java`<br>`class WebSocketConfig` | `com.vku.lanmonitor.server.config` | 26 / 22 | Cấu hình Spring STOMP WebSocket Message Broker: bật broker `/topic`, prefix gửi lên `/app`, đăng ký endpoint bắt tay `/ws` có hỗ trợ SockJS fallback. |
| 11 | `lan-server` | `WebConfig.java`<br>`class WebConfig` | `com.vku.lanmonitor.server.config` | 23 / 19 | Cấu hình Spring MVC Static Resource Handlers ánh xạ URL `/images/**` và `/temp_images/**` đến các thư mục vật lý tương ứng trên ổ cứng Server. |
| 12 | `lan-server` | `ClientApiController.java`<br>`class ClientApiController` | `com.vku.lanmonitor.server.controller.api` | 61 / 50 | REST Controller cung cấp các API quản lý và điều khiển máy trạm: Lấy danh sách client (`GET /api/clients`), gửi lệnh chụp ảnh (`POST /api/clients/{id}/capture`), gửi alert (`POST /api/clients/{id}/alert`), lưu frame livestream (`POST /api/clients/{id}/save-frame`), mở thư mục ảnh trên Server (`POST /api/clients/open-images-folder`). |
| 13 | `lan-server` | `QuizApiController.java`<br>`class QuizApiController` | `com.vku.lanmonitor.server.controller.api` | 94 / 82 | REST Controller quản lý đề thi và bài nộp: Lưu danh sách đề vào `storage/quizzes.json` (`POST /api/quizzes`), đọc đề thi (`GET /api/quizzes`), nhận và lưu bài làm của sinh viên vào `storage/results/...json` (`POST /api/quizzes/submit`), xóa ảnh câu hỏi (`DELETE /api/quizzes/image/{name}`). |
| 14 | `lan-server` | `SettingsController.java`<br>`class SettingsController` | `com.vku.lanmonitor.server.controller.api` | 59 / 51 | REST Controller quản lý cấu hình whitelist: Đọc danh sách ứng dụng hợp lệ từ `storage/exam_rules.json` (`GET /api/settings/whitelist`), cập nhật danh sách và phát lệnh TCP broadcast `UPDATE_WHITELIST:` xuống các client (`POST /api/settings/whitelist`). |
| 15 | `lan-server` | `AdminWebController.java`<br>`class AdminWebController` | `com.vku.lanmonitor.server.controller.web` | 25 / 20 | Spring MVC View Controller cho Giám thị: Điều hướng `GET /admin` -> redirect `/admin/monitor`, `GET /admin/monitor` -> render `dashboard.html`, `GET /admin/quizzes` -> render `quiz.html`. |
| 16 | `lan-server` | `StudentWebController.java`<br>`class StudentWebController` | `com.vku.lanmonitor.server.controller.web` | 28 / 22 | Spring MVC View Controller cho Sinh viên: `GET /login` -> `login.html`, `GET /` -> `student_home.html`, `GET /take_quiz` -> `take_quiz.html`, `GET /quiz_result` -> `quiz_result.html`. |

---

## 2. Bảng Liệt Kê Tất Cả File HTML/CSS/JS (Size, Chức Năng, Cần Chuyển Thành Gì)

### 2.1. Nhóm File Giao Diện HTML (Thymeleaf Templates)

| STT | File Path | Dung lượng | LOC | Chức năng hiện tại | Cần chuyển thành gì trong mô hình mới |
|---|---|---|---|---|---|
| 1 | `templates/dashboard.html` | 14.02 KB | 373 | Dashboard Giám thị: Lưới hiển thị danh sách máy trạm sinh viên, feed cảnh báo gian lận trực tiếp, modal xem màn hình Livestream (4 FPS) kèm nút chụp & mở thư mục ảnh, modal cấu hình Whitelist. | **Admin Dashboard GUI** trên Desktop Server (Java Swing / JavaFX - `AdminDashboardFrame` / `MonitorPanel`): Gồm bảng danh sách Client, bảng Log vi phạm/cảnh báo, Cửa sổ hiển thị Livestream màn hình và Dialog cấu hình luật thi. |
| 2 | `templates/login.html` | 2.82 KB | 91 | Form đăng nhập sinh viên: Nhập Mã sinh viên, Mật khẩu (mặc định 123456), lưu mã vào `sessionStorage` và điều hướng về trang chủ phòng chờ. | **Client Login Frame** (Java Swing / JavaFX - `LoginFrame` / `LoginDialog`): Cho phép sinh viên nhập Mã sinh viên và Mật khẩu, gửi gói tin xác thực `LOGIN` qua TCP socket tới Server. |
| 3 | `templates/student_home.html` | 2.08 KB | 60 | Phòng chờ sinh viên: Hiển thị Mã SV đăng nhập, gọi API lấy danh sách bài thi đang có trên server, hiển thị danh sách thẻ bài thi và nút "Bắt đầu làm bài". | **Student Home Panel / Quiz Picker** (Java Swing / JavaFX - `StudentHomePanel`): Hiển thị thông tin sinh viên, danh sách các đề thi nhận được từ TCP Server và nút vào phòng thi. |
| 4 | `templates/quiz.html` | 10.17 KB | 282 | Giao diện Quản lý / Soạn đề thi của Giám thị: Tạo đề mới, modal dán JSON trắc nghiệm sinh từ AI, cấu hình thời gian thi, xáo trộn câu hỏi, tải ảnh đính kèm câu hỏi, xóa đề. | **Admin Quiz Manager Panel** (Java Swing / JavaFX - `QuizManagementPanel`): Giao diện Giám thị quản lý ngân hàng đề, import JSON câu hỏi, đính kèm file ảnh, thiết lập cấu hình đề thi. |
| 5 | `templates/take_quiz.html` | 9.46 KB | 265 | Giao diện Làm bài thi trắc nghiệm của sinh viên: Màn hình kích hoạt Fullscreen, đồng hồ đếm ngược, hiển thị câu hỏi và đáp án A/B/C/D, đính kèm ảnh câu hỏi, bảng lưới số câu điều hướng (1..N), gắn cờ câu hỏi, nút nộp bài. | **Exam Workspace Frame** (Java Swing / JavaFX - `ExamWorkspaceFrame`): Cửa sổ thi chiếm toàn màn hình (Fullscreen / Kiosk mode), bộ đếm thời gian thi, hiển thị câu hỏi/đáp án/hình ảnh, bảng chọn câu hỏi, nút nộp bài qua TCP socket. |
| 6 | `templates/quiz_result.html` | 3.35 KB | 106 | Giao diện Xem kết quả bài thi: Đọc kết quả từ `sessionStorage`, hiển thị tổng điểm (thang 10), thời gian làm bài, danh sách chi tiết các câu đã chọn và đối chiếu đáp án đúng/sai. | **Quiz Result Dialog / Frame** (Java Swing / JavaFX - `QuizResultFrame`): Hiển thị bảng tổng kết điểm số và chi tiết các câu hỏi đúng/sai sau khi nộp bài. |

### 2.2. Nhóm File Định Kiểu CSS

| STT | File Path | Dung lượng | LOC | Chức năng hiện tại | Cần chuyển thành gì trong mô hình mới |
|---|---|---|---|---|---|
| 7 | `static/css/style.css` | 0.51 KB | 24 | Custom scrollbar mỏng (`.custom-scroll`), hiệu ứng viền sáng của thẻ client đang chọn (`.client-item.active`). | Thiết lập giao diện Swing/JavaFX (Look and Feel / FlatLaf / JavaFX CSS). |
| 8 | `static/css/login.css` | 0.82 KB | 36 | Định kiểu card đăng nhập bo góc tròn, gradient xanh, focus viền input, hiệu ứng hover nút đăng nhập. | Styling giao diện `LoginFrame` Swing/JavaFX. |
| 9 | `static/css/quiz.css` | 2.75 KB | 131 | Thiết kế thẻ bài thi (`.quiz-card`), badge loại đề, danh sách file ảnh đính kèm, hiệu ứng kéo thả thẻ. | Custom Card Renderer / Styling cho `QuizManagementPanel` Swing/JavaFX. |
| 10 | `static/css/take_quiz.css` | 2.12 KB | 117 | Định kiểu overlay fullscreen, nút cờ đỏ, lưới điều hướng câu hỏi (`.q-nav-box`), ô lựa chọn đáp án (`.option-box`), căn chỉnh ảnh đính kèm. | Custom Components & Renderers cho bảng điều hướng và nút chọn đáp án trong `ExamWorkspaceFrame`. |
| 11 | `static/css/quiz_result.css` | 1.16 KB | 56 | Định dạng ô đáp án kết quả đúng (xanh lá), đáp án sai (đỏ), cố định vị trí header tổng kết. | Renderer hiển thị trạng thái kết quả bài thi trong Swing/JavaFX. |
| 12 | `static/css/toast.css` | 1.02 KB | 62 | Định kiểu thông báo Toast góc trên bên phải (animation trượt vào, màu xanh/đỏ/vàng). | Utility hiển thị thông báo popup hoặc JOptionPane / FlatLaf Toast trong ứng dụng Desktop. |

### 2.3. Nhóm File JavaScript (Client-side Logic)

| STT | File Path | Dung lượng | LOC | Chức năng hiện tại | Cần chuyển thành gì trong mô hình mới |
|---|---|---|---|---|---|
| 13 | `static/js/app.js` | 10.72 KB | 295 | Kết nối STOMP WebSocket `/ws`, subscribe `/topic/clients`, `/topic/alerts`, `/topic/screenshot/{clientId}`, gọi REST APIs `/api/clients`, vòng lặp polling 250ms gửi capture (Livestream 4 FPS), gọi lưu frame ảnh, mở thư mục server, lưu whitelist. | **Admin Server Controller & Socket Manager** (Java): Quản lý luồng mạng TCP Server, điều phối dữ liệu Client sang giao diện Giám thị và phát lệnh điều khiển. |
| 14 | `static/js/login.js` | 0.65 KB | 20 | Bắt sự kiện submit form đăng nhập, kiểm tra password, lưu `student_id` vào `sessionStorage`, chuyển hướng trang. | **LoginController / AuthenticationHandler** (Java): Xử lý đăng nhập trên Client và trao đổi gói tin xác thực với Server. |
| 15 | `static/js/student_home.js` | 2.86 KB | 69 | Kiểm tra đăng nhập, gọi API `/api/quizzes`, render thẻ danh sách bài thi lên DOM, xử lý xác nhận bắt đầu thi, xử lý đăng xuất. | **StudentHomeController** (Java): Xử lý hiển thị danh sách đề thi nhận được từ TCP Socket Server. |
| 16 | `static/js/quiz.js` | 13.42 KB | 339 | Quản lý đề thi của Giám thị: Tạo đề, copy prompt AI, parse mảng JSON câu hỏi, upload ảnh chuyển thành base64 dataUrl, gọi API `/api/quizzes` (lưu đề), API xóa ảnh đính kèm. | **QuizEditorController & StorageService** (Java): Quản lý đọc/ghi đề thi từ `storage/quizzes.json`, hỗ trợ import JSON và quản lý file ảnh. |
| 17 | `static/js/take_quiz.js` | 7.12 KB | 217 | Logic phòng thi sinh viên: Tải dữ liệu đề, đảo ngẫu nhiên câu hỏi (nếu `isRandom: true`), đếm ngược thời gian (Timer), chuyển câu, gắn cờ, chọn đáp án, nộp bài qua API `/api/quizzes/submit`, lưu kết quả vào `sessionStorage`. | **ExamEngineController** (Java): Động cơ điều khiển thi trên Client: quản lý thời gian, xáo đề, lưu trạng thái bài làm và đóng gói kết quả gửi qua TCP socket. |
| 18 | `static/js/quiz_result.js` | 3.00 KB | 89 | Đọc kết quả thi từ `sessionStorage`, tính tổng điểm theo thang 10, hiển thị chi tiết câu hỏi kèm đáp án đúng / đáp án sinh viên đã chọn. | **QuizResultController** (Java): Tính toán điểm số và hiển thị kết quả bài thi trên Desktop Client. |
| 19 | `static/js/toast.js` | 2.41 KB | 74 | Tạo và hiển thị thông báo Toast UI nổi trên giao diện với cơ chế chống spam (1.5 giây). | **NotificationDialog / ToastUtil** (Java Swing / JavaFX Utility class). |

---

## 3. Bảng Mapping: REST Endpoint → TCP Command Mới

| STT | REST Endpoint Hiện Tại | HTTP Method | Chức Năng Hiện Tại | TCP Command Mới (Đề Xuất) | Hướng Truyền (Direction) | Payload / Định Dạng Gói Tin |
|---|---|---|---|---|---|---|
| 1 | `/api/clients` | `GET` | Lấy danh sách máy client đang online | `REQ_CLIENTS`<br>`RES_CLIENTS` | Admin UI ⇄ Server | **Request:** `REQ_CLIENTS`<br>**Response:** `RES_CLIENTS:<json_array_clients>` |
| 2 | `/api/clients/{clientId}/capture` | `POST` | Gửi lệnh yêu cầu client chụp ảnh màn hình | `CAPTURE_SCREEN` | Server → Client | **Lệnh:** `CAPTURE_SCREEN`<br>**Phản hồi (từ Client):** `SCREEN:<base64_jpg_data>` |
| 3 | `/api/clients/{clientId}/alert` | `POST` | Gửi tin nhắn cảnh báo tới 1 máy trạm | `SEND_ALERT` | Server → Client | `ALERT:<message_text>` |
| 4 | `/api/clients/{clientId}/save-frame` | `POST` | Yêu cầu Server lưu lại frame livestream hiện tại của client | `SAVE_FRAME` | Admin UI → Server | `SAVE_FRAME:<clientId>` (Server tự động ghi ảnh gần nhất vào thư mục `images/`) |
| 5 | `/api/clients/open-images-folder` | `POST` | Mở thư mục ảnh `images/` trên Server qua File Explorer | `OPEN_FOLDER` | Nội bộ Admin UI / Command | Gọi trực tiếp `Desktop.getDesktop().open(new File("images"))` hoặc `CMD:OPEN_IMAGES` |
| 6 | `/api/quizzes` | `GET` | Lấy toàn bộ danh sách đề thi | `REQ_QUIZZES`<br>`RES_QUIZZES` | Client/Admin ⇄ Server | **Request:** `REQ_QUIZZES`<br>**Response:** `RES_QUIZZES:<json_quizzes_data>` |
| 7 | `/api/quizzes` | `POST` | Giám thị lưu/cập nhật toàn bộ danh sách đề thi | `SAVE_QUIZZES` | Admin UI → Server | `SAVE_QUIZZES:<json_quizzes_data>`<br>**Phản hồi:** `SAVE_QUIZZES_OK` hoặc `SAVE_QUIZZES_FAIL:<reason>` |
| 8 | `/api/quizzes/submit` | `POST` | Sinh viên nộp bài thi trắc nghiệm lên Server | `SUBMIT_QUIZ` | Client → Server | `SUBMIT_QUIZ:<json_result_payload>`<br>**Phản hồi:** `SUBMIT_OK:{"score":...,"message":"Thành công"}` (Server lưu vào `storage/results/`) |
| 9 | `/api/quizzes/image/{fileName}` | `DELETE` | Xóa file ảnh đính kèm câu hỏi trên Server | `DELETE_QUIZ_IMAGE` | Admin UI → Server | `DELETE_QUIZ_IMAGE:<fileName>`<br>**Phản hồi:** `DELETE_IMAGE_OK` / `DELETE_IMAGE_FAIL` |
| 10 | `/api/settings/whitelist` | `GET` | Lấy danh sách từ khóa whitelist ứng dụng hợp lệ | `REQ_WHITELIST`<br>`RES_WHITELIST` | Admin/Client ⇄ Server | **Request:** `REQ_WHITELIST`<br>**Response:** `RES_WHITELIST:<kw1,kw2,kw3>` |
| 11 | `/api/settings/whitelist` | `POST` | Cập nhật danh sách whitelist và phát sóng xuống client | `UPDATE_WHITELIST_CONFIG` | Admin UI → Server | `UPDATE_WHITELIST_CONFIG:<kw1,kw2,kw3>`<br>(Server lưu file `storage/exam_rules.json` và broadcast `UPDATE_WHITELIST:...` xuống client) |
| 12 | `/login` (Web Auth) | `POST` | Xác thực đăng nhập sinh viên | `LOGIN` | Client → Server | **Request:** `LOGIN:<studentId>:<password>`<br>**Response:** `LOGIN_SUCCESS:<studentId>` hoặc `LOGIN_FAIL:<reason>` |

---

## 4. Bảng Mapping: WebSocket Topic → TCP Broadcast Type Mới

| STT | WebSocket Topic Cũ | Mục Đích Sử Dụng Hiện Tại | TCP Broadcast / Push Type Mới | Hướng Truyền | Định Dạng Dữ Liệu Chi Tiết |
|---|---|---|---|---|---|
| 1 | `/topic/clients` | Cập nhật danh sách máy trạm online thời gian thực lên Dashboard khi có máy kết nối, ngắt kết nối hoặc báo danh Mã SV. | `BROADCAST_CLIENTS` (hoặc `EVENT:CLIENTS_UPDATED`) | Server → Admin UI | `BROADCAST_CLIENTS:[{"id":"192.168.1.5:54321","ipAddress":"192.168.1.5","pcName":"24ITB217"}, ...]` |
| 2 | `/topic/screenshot/{clientId}` | Truyền luồng ảnh màn hình (Livestream 4 FPS) của một máy sinh viên cụ thể lên Web Giám thị. | `SCREEN_STREAM` (hoặc `STREAM_FRAME`) | Client → Server → Admin UI | `SCREEN_STREAM:<clientId>|<base64_jpeg_data>`<br>*(Hoặc qua binary socket stream: `[Header: 4 bytes Length + ClientId] [Payload: Raw JPG Bytes]`)* |
| 3 | `/topic/alerts` | Báo động thời gian thực khi sinh viên mở tab/cửa sổ lạ không thuộc danh sách Whitelist. | `BROADCAST_ALERT` (hoặc `EVENT:ALERT`) | Client → Server → Admin UI | `BROADCAST_ALERT:{"clientId":"192.168.1.5:54321","pcName":"24ITB217","severity":"RED","tabName":"ChatGPT - Google Chrome","timestamp":1790476033193}` |
| 4 | *(Luật thi khởi tạo)* | Gửi danh sách Whitelist xuống Client ngay khi kết nối thành công. | `UPDATE_WHITELIST` | Server → Client | `UPDATE_WHITELIST:Bài Thi Trắc Nghiệm,Word,Calculator,Excel` |

---

## 5. Danh Sách File Cần XÓA Khỏi Server

Khi chuyển đổi toàn bộ `lan-server` sang kiến trúc Socket TCP thuần túy kết hợp giao diện Desktop (bỏ hoàn toàn Spring Boot Web, Spring MVC, REST Controllers, STOMP WebSocket và Thymeleaf), các file sau **sẽ bị loại bỏ hoàn toàn** khỏi mã nguồn:

### 5.1. Nhóm REST Controllers & Web Controllers (5 files)
1. `lan-server/src/main/java/com/vku/lanmonitor/server/controller/api/ClientApiController.java`
2. `lan-server/src/main/java/com/vku/lanmonitor/server/controller/api/QuizApiController.java`
3. `lan-server/src/main/java/com/vku/lanmonitor/server/controller/api/SettingsController.java`
4. `lan-server/src/main/java/com/vku/lanmonitor/server/controller/web/AdminWebController.java`
5. `lan-server/src/main/java/com/vku/lanmonitor/server/controller/web/StudentWebController.java`

### 5.2. Nhóm Cấu Hình & Dịch Vụ WebSocket / Web (3 files)
6. `lan-server/src/main/java/com/vku/lanmonitor/server/config/WebSocketConfig.java`
7. `lan-server/src/main/java/com/vku/lanmonitor/server/config/WebConfig.java`
8. `lan-server/src/main/java/com/vku/lanmonitor/server/websocket/RealtimeNotificationService.java`

### 5.3. Nhóm Giao Diện HTML Templates (6 files trong `src/main/resources/templates/`)
9. `lan-server/src/main/resources/templates/dashboard.html`
10. `lan-server/src/main/resources/templates/login.html`
11. `lan-server/src/main/resources/templates/quiz.html`
12. `lan-server/src/main/resources/templates/quiz_result.html`
13. `lan-server/src/main/resources/templates/student_home.html`
14. `lan-server/src/main/resources/templates/take_quiz.html`

### 5.4. Nhóm Tệp Tĩnh JavaScript (7 files trong `src/main/resources/static/js/`)
15. `lan-server/src/main/resources/static/js/app.js`
16. `lan-server/src/main/resources/static/js/login.js`
17. `lan-server/src/main/resources/static/js/quiz.js`
18. `lan-server/src/main/resources/static/js/quiz_result.js`
19. `lan-server/src/main/resources/static/js/student_home.js`
20. `lan-server/src/main/resources/static/js/take_quiz.js`
21. `lan-server/src/main/resources/static/js/toast.js`

### 5.5. Nhóm Tệp Tĩnh CSS (6 files trong `src/main/resources/static/css/`)
22. `lan-server/src/main/resources/static/css/style.css`
23. `lan-server/src/main/resources/static/css/login.css`
24. `lan-server/src/main/resources/static/css/quiz.css`
25. `lan-server/src/main/resources/static/css/quiz_result.css`
26. `lan-server/src/main/resources/static/css/take_quiz.css`
27. `lan-server/src/main/resources/static/css/toast.css`

### 5.6. Các Dependencies Cần Gỡ Bỏ Trong `lan-server/pom.xml`
- `org.springframework.boot:spring-boot-starter-thymeleaf`
- `org.springframework.boot:spring-boot-starter-webmvc`
- `org.springframework.boot:spring-boot-starter-websocket`
- `org.springframework.boot:spring-boot-starter-thymeleaf-test`
- `org.springframework.boot:spring-boot-starter-webmvc-test`
- `org.springframework.boot:spring-boot-starter-websocket-test`

---

## 6. Tổng Kết Số Liệu Kiến Trúc Codebase

- **Tổng số file Java hiện có:** 16 files (Client: 4 files, Server: 12 files bao gồm 1 test file).
- **Tổng số dòng mã Java (LOC):** 724 lines (Client: 225 lines, Server: 499 lines).
- **Tổng số file Web Frontend (HTML/CSS/JS):** 19 files (HTML: 6, CSS: 6, JS: 7) với tổng dung lượng ~82.7 KB.
- **Tổng số file cần xóa khỏi Server:** 27 files (gồm 8 file Java controller/config/service web và 19 file giao diện tài nguyên tĩnh).
- **Mô hình kiến trúc đích:** Hệ thống Desktop Client-Server 100% TCP Socket, phân tách rõ ràng tầng giao thức mạng (`TcpServerService` / `SocketClientService`), tầng xử lý nghiệp vụ (`QuizService`, `ExamService`, `SecurityService`), và tầng giao diện người dùng (`Swing`/`JavaFX`).
