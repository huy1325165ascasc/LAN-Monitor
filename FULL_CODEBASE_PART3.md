# =============================================
# PHẦN 3: LAN-SERVER (HTML Templates)
# =============================================

## FILE: dashboard.html (Giao diện Giám thị)

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>LAN Monitoring Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <link rel="stylesheet" href="/css/style.css" />
  </head>
  <body class="bg-slate-900 text-slate-100 min-h-screen">
    <header class="bg-slate-800 border-b border-slate-700 px-6 py-4 flex justify-between items-center shadow-md">
      <div class="flex items-center space-x-3">
        <div class="w-3 h-3 bg-emerald-500 rounded-full animate-pulse"></div>
        <h1 class="text-xl font-bold tracking-wide">LAN Monitor & Remote Management</h1>
      </div>
      <div class="flex items-center space-x-4">
        <span id="socket-status" class="px-3 py-1 text-xs rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">Đang kết nối WebSocket...</span>
        <span class="text-xs bg-slate-700 text-slate-300 px-2.5 py-1 rounded-md font-mono">Port: 9999</span>
      </div>
    </header>

    <main class="max-w-[1400px] mx-auto p-6 grid grid-cols-1 lg:grid-cols-12 gap-6">
      <div class="lg:col-span-9 space-y-6">
        <section class="bg-slate-800 border border-slate-700 rounded-xl p-5 shadow-lg">
          <div class="flex items-center justify-between border-b border-slate-700 pb-3 mb-4">
            <h2 class="font-semibold text-slate-200">Bảng Lệnh Điều Khiển</h2>
            <span id="selected-client-tag" class="text-xs text-slate-400 font-mono">Chưa chọn máy trạm</span>
            <div class="flex gap-3">
              <button onclick="openWhitelistModal()" class="bg-indigo-600 hover:bg-indigo-500 px-4 py-2 rounded-lg text-sm font-medium transition flex items-center gap-2 shadow-md">
                Cấu Hình Bài Thi
              </button>
            </div>
          </div>
        </section>

        <section class="bg-slate-800 border border-slate-700 rounded-xl p-5 shadow-lg min-h-[400px]">
          <div class="flex justify-between items-center border-b border-slate-700 pb-3 mb-4">
            <h2 class="font-semibold text-slate-200">Danh Sách Máy Trạm & Trạng Thái</h2>
            <span id="client-count" class="text-xs px-2 py-0.5 rounded-full bg-slate-700 text-slate-300 font-mono">0 online</span>
          </div>
          <div id="client-grid" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          </div>
        </section>
      </div>

      <div class="lg:col-span-3 bg-slate-800 border border-slate-700 rounded-xl p-5 shadow-lg flex flex-col h-[calc(100vh-100px)] sticky top-6">
        <div class="flex justify-between items-center mb-4 border-b border-slate-700 pb-3">
          <h2 class="font-semibold text-slate-200 flex items-center gap-2">
            <span class="w-2.5 h-2.5 rounded-full bg-rose-500 animate-pulse"></span> Sự Kiện Khả Nghi
          </h2>
        </div>
        <div id="alert-feed" class="flex-1 overflow-y-auto space-y-3 pr-2 custom-scroll">
        </div>
      </div>
    </main>

    <div id="livestream-modal" class="fixed inset-0 bg-slate-900 z-50 hidden flex-col w-screen h-screen">
      <div class="flex justify-between items-center px-6 py-4 border-b border-slate-700 bg-slate-800 shadow-md">
        <h3 class="text-xl font-bold text-slate-200 flex items-center gap-3">
          <span class="w-3.5 h-3.5 rounded-full bg-rose-500 animate-pulse"></span> LIVE STREAM: <span id="livestream-title" class="text-emerald-400 tracking-wide">Máy 01</span>
        </h3>
        <div class="flex items-center gap-4">
          <button onclick="captureLivestreamFrame()" class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-medium transition flex items-center gap-2 shadow-lg">Chụp Ảnh</button>
          <button onclick="openImagesFolder()" class="bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded-lg text-sm font-medium transition flex items-center gap-2 shadow-lg">Mở Thư Mục Ảnh</button>
          <div class="w-px h-6 bg-slate-600 mx-2"></div>
          <button onclick="closeLiveStream()" class="text-slate-300 hover:text-white bg-slate-700 hover:bg-rose-500 px-3 py-2 rounded-lg transition flex items-center gap-2">Đóng Live</button>
        </div>
      </div>
      <div class="flex-1 flex justify-center items-center bg-black overflow-hidden relative p-4">
        <img id="livestream-img" src="" class="w-full h-full object-contain rounded-md" alt="Đang kết nối tín hiệu..." />
      </div>
    </div>

    <div id="whitelist-modal" class="fixed inset-0 bg-black/80 z-50 hidden flex-col items-center justify-center backdrop-blur-sm">
      <div class="w-full max-w-lg bg-slate-800 rounded-xl overflow-hidden shadow-2xl border border-slate-700">
        <div class="px-6 py-4 border-b border-slate-700 flex justify-between items-center bg-slate-900">
          <h3 class="text-lg font-bold text-slate-200">Cấu Hình Danh Sách Hợp Lệ</h3>
          <button onclick="closeWhitelistModal()" class="text-slate-400 hover:text-rose-500">X</button>
        </div>
        <div class="p-6">
          <p class="text-sm text-slate-400 mb-4">Nhập các từ khóa tiêu đề ứng dụng hợp lệ (cách nhau bởi dấu phẩy).</p>
          <textarea id="whitelist-input" rows="4" class="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-sm text-emerald-400 font-mono focus:border-indigo-500 focus:outline-none"></textarea>
          <div class="flex justify-end gap-3 mt-6">
            <button onclick="closeWhitelistModal()" class="px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded-lg text-sm text-white transition">Hủy</button>
            <button onclick="saveWhitelist()" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 rounded-lg text-sm text-white font-medium transition flex items-center gap-2">Lưu & Áp Dụng Ngay</button>
          </div>
        </div>
      </div>
    </div>

    <script src="/js/app.js"></script>
  </body>
</html>
```

## FILE: login.html

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đăng nhập - Sinh viên</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="/css/login.css" />
  </head>
  <body class="d-flex align-items-center justify-content-center vh-100">
    <div class="card login-card p-4 p-md-5 bg-white">
      <div class="text-center mb-4">
        <div class="icon-wrapper mb-3"><i class="bx bxs-user-badge fs-1"></i></div>
        <h3 class="fw-bold text-dark mb-1">Sinh Viên Đăng Nhập</h3>
        <p class="text-muted small">Hệ thống Thi & Giám sát nội bộ VKU</p>
      </div>
      <form id="loginForm">
        <div class="mb-3">
          <label class="form-label fw-semibold text-secondary small">Mã Sinh Viên</label>
          <div class="input-group">
            <span class="input-group-text bg-transparent text-muted"><i class="bx bx-id-card fs-5"></i></span>
            <input type="text" class="form-control" id="studentId" placeholder="Ví dụ: 24ITB217" required autocomplete="off" />
          </div>
        </div>
        <div class="mb-4">
          <label class="form-label fw-semibold text-secondary small">Mật khẩu</label>
          <div class="input-group">
            <span class="input-group-text bg-transparent text-muted"><i class="bx bx-lock-alt fs-5"></i></span>
            <input type="password" class="form-control" id="password" placeholder="Nhập mật khẩu..." required />
          </div>
        </div>
        <button type="submit" class="btn btn-login btn-primary w-100 fw-bold rounded-pill py-2.5 d-flex align-items-center justify-content-center gap-2">
          Vào Phòng Thi <i class="bx bx-right-arrow-alt fs-4"></i>
        </button>
      </form>
    </div>
    <script>
      if (sessionStorage.getItem("student_id")) { window.location.href = "/"; }
    </script>
    <script src="/js/login.js"></script>
  </body>
</html>
```

## FILE: quiz.html (Soạn đề thi)

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý Đề Thi - Giám Thị</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="/css/quiz.css" />
    <link rel="stylesheet" href="/css/toast.css" />
  </head>
  <body class="bg-light">
    <div class="container py-4" id="quiz-container">
      <div class="row mb-4">
        <div class="col-12 d-flex justify-content-between align-items-center flex-wrap gap-3">
          <h3 class="fw-bold m-0 d-flex align-items-center text-dark"><i class="bx bx-edit-alt text-primary me-2"></i> Soạn Đề Thi Trắc Nghiệm</h3>
          <div class="d-flex align-items-center gap-2">
            <a href="/admin" class="btn btn-outline-secondary rounded-pill fw-semibold me-2"><i class="bx bx-arrow-back me-1"></i> Về Dashboard</a>
            <button id="btn-copy-prompt" class="btn btn-outline-primary rounded-pill fw-semibold d-flex align-items-center"><i class="bx bx-copy me-1"></i> Sao chép lệnh AI</button>
            <button class="btn btn-primary rounded-pill fw-semibold d-flex align-items-center shadow-sm" data-bs-toggle="modal" data-bs-target="#addQuizModal"><i class="bx bx-plus me-1"></i> Tạo Đề Mới</button>
          </div>
        </div>
      </div>
      <div class="row mb-2">
        <div class="col-12">
          <div id="quizzes-list" class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4 mt-1"></div>
          <div id="quizzes-empty" class="text-center text-muted py-5 d-none">
            <i class="bx bx-folder-open fs-1 mb-2 opacity-50"></i>
            <p>Chưa có đề thi nào. Bấm 'Tạo Đề Mới' để bắt đầu.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Tạo Đề -->
    <div class="modal fade" id="addQuizModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">Tạo bài thi mới</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-4">
              <label class="form-label fw-semibold text-muted small">Tên bài thi</label>
              <input type="text" id="quiz-name-input" class="form-control bg-light border-0" placeholder="Ví dụ: Thi cuối kỳ..." />
            </div>
            <button type="button" id="btn-create-quiz" class="btn btn-primary w-100 rounded-pill fw-bold py-2">Lưu Tên Đề Thi</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Dán JSON -->
    <div class="modal fade" id="editQuizModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow">
          <div class="modal-header border-bottom-0 pb-0">
            <h5 class="modal-title fw-bold">Nhập dữ liệu trắc nghiệm</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-4 bg-white p-3 rounded-3 border">
              <label class="form-label fw-bold text-dark mb-1">Tên bài thi</label>
              <input type="text" id="edit-quiz-name-input" class="form-control bg-light border-0" />
            </div>
            <div class="mb-3 d-flex align-items-center justify-content-between bg-white p-3 rounded-3 border">
              <div><label class="form-label fw-bold text-dark mb-0">Thời gian làm bài</label></div>
              <div class="d-flex align-items-center gap-3">
                <button type="button" id="btn-time-down" class="btn btn-light border rounded-circle"><i class="bx bx-chevron-down fs-4"></i></button>
                <span id="quiz-time-display" class="fw-bold fs-5 text-primary">15 phút</span>
                <button type="button" id="btn-time-up" class="btn btn-light border rounded-circle"><i class="bx bx-chevron-up fs-4"></i></button>
              </div>
            </div>
            <div class="mb-3 d-flex align-items-center justify-content-between bg-white p-3 rounded-3 border">
              <div><label class="form-label fw-bold text-dark mb-0">Xáo trộn câu hỏi</label></div>
              <div class="form-check form-switch fs-4 mb-0"><input class="form-check-input" type="checkbox" id="quiz-random-switch" style="cursor: pointer" /></div>
            </div>
            <div class="mb-1">
              <label class="form-label fw-semibold text-muted small">Dán mảng JSON AI tạo ra vào đây</label>
              <textarea id="edit-quiz-data" class="form-control bg-light border-0 font-monospace" rows="8"></textarea>
            </div>
            <hr class="text-muted my-4" />
            <div class="mb-3">
              <label class="form-label fw-bold text-dark mb-2">Đính kèm hình ảnh</label>
              <div class="row g-2 mb-3">
                <div class="col-md-4">
                  <select id="question-image-select" class="form-select bg-light border-0 cursor-pointer">
                    <option value="" disabled selected>-- Chọn câu --</option>
                  </select>
                </div>
                <div class="col-md-8 d-flex gap-2">
                  <input type="file" id="quiz-image-upload" class="d-none" accept="image/*" multiple />
                  <button type="button" id="btn-browse-image" class="btn btn-outline-primary fw-semibold flex-fill"><i class="bx bx-folder-open me-1"></i> Chọn ảnh</button>
                </div>
              </div>
              <ul id="attached-images-list" class="list-group list-group-flush border rounded-3 overflow-hidden d-none mt-2"></ul>
            </div>
          </div>
          <div class="modal-footer border-top-0 pt-0">
            <button type="button" class="btn btn-outline-secondary rounded-pill" data-bs-dismiss="modal">Hủy</button>
            <button type="button" id="btn-save-quiz-data" class="btn btn-primary rounded-pill px-4">Lưu Đề Thi</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div class="modal fade" id="confirmModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-bottom-0 pt-4 px-4 pb-0">
            <h5 class="modal-title fw-bold text-dark"><i class="bx bx-error-circle text-danger me-2"></i>Xác nhận</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body px-4 py-3 text-secondary fw-semibold" id="confirmModalMessage"></div>
          <div class="modal-footer border-top-0 pb-4 px-4 d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-light fw-bold px-4 rounded-3" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn btn-danger fw-bold px-4 rounded-3" id="confirmModalBtn">Đồng ý</button>
          </div>
        </div>
      </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/toast.js"></script>
    <script src="/js/quiz.js"></script>
  </body>
</html>
```

## FILE: student_home.html

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Phòng Chờ - Chọn Bài Thi</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet" />
  </head>
  <body class="bg-light">
    <header class="bg-white shadow-sm py-3 mb-5">
      <div class="container d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
          <i class="bx bxs-graduation text-primary fs-2"></i>
          <h4 class="m-0 fw-bold text-dark">Hệ Thống Thi Trực Tuyến</h4>
        </div>
        <div class="d-flex align-items-center gap-3">
          <span class="fw-semibold text-secondary" id="student-name-display"><i class="bx bxs-user-circle me-1"></i>Đang tải...</span>
          <button onclick="logout()" class="btn btn-outline-danger btn-sm rounded-pill fw-bold px-3">Đăng xuất <i class="bx bx-log-out ms-1"></i></button>
        </div>
      </div>
    </header>

    <main class="container">
      <div class="text-center mb-5">
        <h2 class="fw-bold">Danh Sách Bài Thi</h2>
        <p class="text-muted">Vui lòng chọn bài thi bên dưới để bắt đầu.</p>
      </div>
      <div class="row g-4 justify-content-center" id="quiz-container">
        <div class="col-12 text-center text-muted" id="loading-msg">
          <div class="spinner-border text-primary" role="status"></div>
          <p class="mt-2">Đang tải danh sách bài thi...</p>
        </div>
      </div>
    </main>

    <script src="/js/student_home.js"></script>
  </body>
</html>
```

## FILE: take_quiz.html

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đang làm bài thi - Study Space</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="/css/take_quiz.css" />
  </head>
  <body class="bg-light">
    <div id="start-overlay" class="start-overlay">
      <div class="text-center bg-white p-5 rounded-4 shadow">
        <h3 class="fw-bold mb-3">Bạn đã sẵn sàng chưa?</h3>
        <p class="text-muted mb-4">Hệ thống sẽ chuyển sang chế độ toàn màn hình để làm bài.</p>
        <button id="btn-start-quiz-now" class="btn btn-primary btn-lg rounded-pill px-5 fw-bold"><i class="bx bx-play-circle me-1"></i> Bắt đầu làm bài</button>
        <div class="mt-3"><a href="/" class="text-muted small text-decoration-none">Quay lại</a></div>
      </div>
    </div>

    <div id="quiz-workspace" class="d-none">
      <header class="quiz-header bg-white shadow-sm d-flex justify-content-between align-items-center px-4 py-3">
        <div class="d-flex align-items-center gap-3">
          <button id="btn-back-quiz" class="btn btn-outline-secondary rounded-pill btn-sm d-flex align-items-center"><i class="bx bx-arrow-back me-1"></i> Quay lại</button>
          <h5 class="fw-bold m-0" id="quiz-title-display">Đang tải dữ liệu...</h5>
        </div>
        <div id="timer-container" class="quiz-timer bg-dark text-white fw-bold px-4 py-2 rounded-pill fs-5 d-flex align-items-center">
          <i class="bx bx-time-five me-2"></i><span id="timer-display">00:00</span>
        </div>
      </header>

      <main class="container-fluid px-5 pt-5 pb-4">
        <div class="row h-100">
          <div class="col-md-1">
            <div class="card border-0 shadow-sm rounded-4 text-center p-3">
              <h5 class="fw-bold text-dark mb-4 mt-2">Câu <br /><span id="current-q-num" class="text-primary fs-3">1</span></h5>
              <button class="btn btn-light border d-flex flex-column align-items-center justify-content-center mx-auto mb-4 flag-btn" style="width: 70px; height: 70px; border-radius: 12px">
                <i class="bx bx-flag fs-3 text-secondary mb-1 flag-icon"></i>
                <span class="small fw-semibold text-secondary" style="font-size: 11px">Gắn cờ</span>
              </button>
            </div>
          </div>

          <div class="col-md-7">
            <div class="card border-0 shadow-sm rounded-4 p-4 h-100 d-flex flex-column">
              <div class="flex-grow-1">
                <h5 class="fw-bold mb-3" id="question-text" style="line-height: 1.6">Đang tải câu hỏi...</h5>
                <div id="question-images-area" class="d-flex flex-column gap-3 mb-4"></div>
                <div id="options-area" class="d-flex flex-column gap-3 mb-4"></div>
              </div>
              <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                <button id="btn-prev-question" class="btn btn-light border fw-bold px-4 rounded-pill text-secondary"><i class="bx bx-left-arrow-alt me-1"></i> Câu trước</button>
                <button id="btn-next-question" class="btn btn-primary fw-bold px-4 rounded-pill">Câu sau <i class="bx bx-right-arrow-alt ms-1"></i></button>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="card border-0 shadow-sm rounded-4 p-4 h-100 d-flex flex-column">
              <h6 class="fw-bold mb-4 d-flex justify-content-between align-items-center">
                <span>Bảng câu hỏi</span>
              </h6>
              <div class="question-grid flex-grow-1 align-content-start" id="question-navigation-grid"></div>
              <div class="mt-4 pt-3 border-top text-center">
                <a href="#" id="btn-finish-quiz" class="text-dark fw-bold text-decoration-underline fs-6">Làm xong!</a>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/take_quiz.js"></script>
  </body>
</html>
```

## FILE: quiz_result.html

```html
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Kết quả bài thi - Study Space</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="/css/take_quiz.css" />
    <link rel="stylesheet" href="/css/quiz_result.css" />
  </head>
  <body class="bg-light">
    <header class="quiz-header bg-white shadow-sm d-flex justify-content-between align-items-center px-4 py-3 sticky-top">
      <div class="d-flex align-items-center gap-3">
        <h4 class="fw-bold m-0 d-flex align-items-center text-dark"><i class="bx bx-bar-chart-alt-2 text-primary me-2 fs-3"></i> Xem kết quả</h4>
      </div>
      <a href="/" class="btn btn-outline-danger rounded-pill fw-bold px-4">Thoát xem kết quả</a>
    </header>

    <main class="container py-5" style="margin-top: 75px;">
      <div class="row mb-5 justify-content-center">
        <div class="col-md-10">
          <div class="d-flex justify-content-around align-items-center bg-white p-4 rounded-4 shadow-sm border">
            <div class="text-center">
              <p class="text-muted fw-semibold mb-1 small text-uppercase">Tên bài kiểm tra</p>
              <h5 class="fw-bold text-dark m-0" id="res-quiz-name">Đang tải...</h5>
            </div>
            <div style="width: 2px; height: 40px; background-color: #e2e8f0"></div>
            <div class="text-center">
              <p class="text-muted fw-semibold mb-1 small text-uppercase">Tổng điểm</p>
              <h3 class="fw-black text-primary m-0" id="res-score">0,00</h3>
            </div>
            <div style="width: 2px; height: 40px; background-color: #e2e8f0"></div>
            <div class="text-center">
              <p class="text-muted fw-semibold mb-1 small text-uppercase">Thời gian đã làm</p>
              <h5 class="fw-bold text-dark m-0" id="res-time">00:00</h5>
            </div>
          </div>
        </div>
      </div>
      <div class="row justify-content-center">
        <div class="col-md-10">
          <h5 class="fw-bold mb-4 text-dark border-bottom pb-3">Chi tiết câu trắc nghiệm đã làm</h5>
          <div id="result-details-list" class="d-flex flex-column gap-4 pb-5"></div>
        </div>
      </div>
    </main>

    <script src="/js/quiz_result.js"></script>
  </body>
</html>
```
