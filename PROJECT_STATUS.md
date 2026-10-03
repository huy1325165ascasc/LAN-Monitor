# 📊 LAN MONITOR — PROJECT STATUS

## 📌 Metadata
- **Ngày cập nhật**: 2026-10-03 12:56:05
- **Git HEAD**: 14c0e63 Phase 4E: LivestreamDialog with Base64 image pipeline fixed
- **Người cập nhật**: xuanvinh

## 🏗️ Cấu trúc thư mục

```
F:\Do_An_Mang\
├── lan-server\        # Spring Boot server (port 9999)
├── lan-student\       # Swing client cho sinh viên
├── lan-admin\         # Swing client cho giám thị
├── lan-client\        # Anti-cheat client (máy trạm)
├── storage\           # quizzes.json, exam_rules.json, results\
├── images\            # Ảnh gian lận cheat_*.jpg
├── ANALYSIS.md        # Phân tích kiến trúc
├── AI_CONTEXT.md      # Ngữ cảnh cho AI
├── PROJECT_STATUS.md  # File này
└── (đã xóa backup cũ, file rác)
```

## ✅ Phases đã hoàn thành

| Phase | Nội dung | Status |
|---|---|---|
| 0 | Backup + Analysis | ✅ |
| 1 | Refactor lan-server TCP-only | ✅ |
| 2A | Verify lan-client tương thích | ✅ |
| 2B+2C | Skeleton lan-student + lan-admin | ✅ |
| 3A | LoginFrame + StudentTcpClient | ✅ |
| 3B | StudentHomeFrame (quiz list) | ✅ |
| 3C | TakeQuizFrame (timer + submit) | ✅ |
| 3D | ResultFrame (điểm + chi tiết) | ✅ |
| 4A | AdminApp + LoginFrame + skeleton | ✅ |
| 4B | DashboardPanel | ✅ |
| 4C | QuizEditorPanel | ✅ |
| 4D | ConfigPanel | ✅ |
| 4E | LivestreamDialog | ✅ |

## 🚀 Lệnh khởi động

```powershell
# Kill java cũ
taskkill /F /IM java.exe

# Terminal 1 — Server
cd F:\Do_An_Mang\lan-server
java -jar target\lan-server-0.0.1-SNAPSHOT.jar

# Terminal 2 — Student (login 24ITB217 / 123456)
cd F:\Do_An_Mang\lan-student
java -jar target\lan-student-1.0.0-jar-with-dependencies.jar

# Terminal 3 — Admin (login admin / 123456)
cd F:\Do_An_Mang\lan-admin
java -jar target\lan-admin-1.0.0-jar-with-dependencies.jar
```

## 🔑 Tài khoản mặc định
- Sinh viên: bất kỳ MSV / 123456
- Giám thị: admin / 123456

## 📡 Protocol TCP (port 9999)
```
STUDENT_LOGIN, ADMIN_LOGIN, CONNECT,
REQ_QUIZ_LIST, REQ_QUIZ_DETAIL, SUBMIT_QUIZ,
REQ_CLIENT_LIST, SAVE_QUIZ, DELETE_QUIZ,
CAPTURE, SEND_ALERT, UPDATE_WHITELIST_CONFIG
```

## 🎯 AdminApp đã có 3 tab hoàn chỉnh

### Tab 1: Dashboard
- Hiển thị danh sách clients đang online realtime
- Auto refresh mỗi 3 giây
- Hiển thị: ID, IP, PC Name
- Nút "Làm mới" thủ công

### Tab 2: Quiz Editor
- JTable hiển thị danh sách đề thi (ID, Tên, Thời gian, Số câu, Trạng thái)
- Tạo đề thi mới (với JSON editor)
- Sửa đề thi hiện có
- Xóa đề thi (có xác nhận)
- Nút "Làm mới" danh sách

### Tab 3: Config
- Quản lý Whitelist (danh sách app được phép)
- TextArea nhập keywords (hỗ trợ dấu phẩy hoặc xuống dòng)
- Nút "Tải lại" từ server
- Nút "Lưu & Áp dụng" (gửi tới tất cả clients)

## 🏆 Thành tựu
- 4 phân hệ hoàn chỉnh: server, student, admin, anti-cheat client
- 20+ TCP commands
- 3 tab AdminApp + Livestream
- Test 6 apps cùng lúc OK

## ⚠️ Vấn đề tồn đọng
- Log server hiển thị tiếng Việt lỗi font: chạy `chcp 65001` trước khi start
- Chưa test trên 2 máy LAN thật (hiện tại chạy localhost)

## 🎯 Bước tiếp theo
1. Test tích hợp 2 máy LAN (tùy chọn)
2. Viết báo cáo + slide
3. Đóng gói zip nộp bài

## 📝 Lịch sử thay đổi

| Ngày | Thay đổi |
|---|---|
| 2026-09-30 | Phase 0-1: backup + refactor server |
| 2026-10-01 | Phase 2-3D: hoàn thành Exam System cho sinh viên |
| 2026-10-01 | Phase 4A: Admin skeleton |
| 2026-10-02 | Dọn dẹp dự án: xóa 5 backup cũ + 7 file rác, đổi tên lan-client, tạo PROJECT_STATUS.md |
| 2026-10-03 | Phase 4B-4E: hoàn thành AdminApp với 3 tabs và LivestreamDialog |

## 🧹 Dọn dẹp đã thực hiện (2026-10-02)

**Đã xóa (5 backup folders):**
- lan-student_backup_phase2
- lan-student_backup_phase3A
- lan-student_backup_phase3B
- lan-student_backup_phase3C
- lan-admin_backup_phase2

**Đã xóa (7 file rác):**
- FULL_CODEBASE_PART1.md
- FULL_CODEBASE_PART2.md
- FULL_CODEBASE_PART3.md
- FULL_CODEBASE_PART4.md
- fullcode.txt
- TestFrame.java
- TestFrame.class

**Đã đổi tên:**
- `lan-client (1)` → `lan-client`

**Backup trước khi dọn:**
- `F:\Do_An_Mang_BACKUP_BEFORE_CLEANUP`
