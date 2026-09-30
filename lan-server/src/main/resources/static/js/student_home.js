// 1. Kiểm tra đăng nhập
const studentId = sessionStorage.getItem("student_id");
if (!studentId) {
  window.location.href = "/login";
} else {
  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("student-name-display").innerHTML =
      `<i class='bx bxs-user-circle me-1'></i>Sinh Viên: ${studentId}`;
  });
}

// 2. Hàm Đăng xuất
window.logout = function () {
  sessionStorage.removeItem("student_id");
  window.location.href = "/login";
};

// 3. Hàm Xác nhận thi
window.confirmStart = function () {
  return confirm(
    "Bạn đã sẵn sàng làm bài chưa? Thời gian sẽ bắt đầu đếm ngược ngay lập tức.",
  );
};

// 4. Tải danh sách đề thi từ Server
document.addEventListener("DOMContentLoaded", async function () {
  const container = document.getElementById("quiz-container");
  try {
    const response = await fetch("/api/quizzes");
    const quizzes = await response.json();

    container.innerHTML = ""; // Xóa Loading

    if (!quizzes || quizzes.length === 0) {
      container.innerHTML = `
        <div class="col-12 text-center text-danger mt-5">
          <i class='bx bx-file-blank fs-1 mb-2 text-secondary'></i>
          <h5>Hiện chưa có bài thi nào!</h5>
          <p class="text-muted small">Vui lòng đợi Giám thị tải đề thi lên hệ thống.</p>
        </div>`;
      return;
    }

    quizzes.forEach((quiz) => {
      const timeText = quiz.timeLimit + " phút";
      const questionCount = quiz.data ? quiz.data.length : 0;

      const cardHtml = `
        <div class="col-md-5 col-lg-4">
          <div class="card border-0 shadow-sm rounded-4 h-100 p-2" style="transition: transform 0.2s;">
            <div class="card-body d-flex flex-column">
              <div class="d-flex justify-content-between align-items-start mb-3">
                <h5 class="card-title fw-bold text-dark m-0 lh-base" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">${quiz.name}</h5>
                <span class="badge bg-primary rounded-pill px-3 py-2 ms-2">${questionCount} câu</span>
              </div>
              <p class="card-text text-muted small mb-4"><i class='bx bx-time-five me-1 text-primary'></i> Thời gian làm bài: <b>${timeText}</b></p>
              <a href="/take_quiz?id=${quiz.id}" class="btn btn-primary rounded-pill fw-bold mt-auto w-100 shadow-sm py-2" onclick="return confirmStart()">
                Bắt Đầu Làm Bài <i class='bx bx-right-arrow-alt fs-5 align-middle ms-1'></i>
              </a>
            </div>
          </div>
        </div>
      `;
      container.insertAdjacentHTML("beforeend", cardHtml);
    });
  } catch (error) {
    container.innerHTML = `<div class="col-12 text-center text-danger mt-5"><i class='bx bx-wifi-off fs-1 mb-2'></i><h5>Lỗi kết nối đến máy chủ!</h5></div>`;
  }
});
