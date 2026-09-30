document.getElementById("loginForm").addEventListener("submit", function (e) {
  e.preventDefault();

  const msvInput = document
    .getElementById("studentId")
    .value.trim()
    .toUpperCase();
  const pwdInput = document.getElementById("password").value;

  // Logic Demo: Kiểm tra mật khẩu
  if (pwdInput === "123456") {
    // LƯU Ý QUAN TRỌNG: Lưu Mã Sinh Viên vào sessionStorage
    sessionStorage.setItem("student_id", msvInput);

    // Chuyển hướng vào trang chủ phòng chờ (student_home.html)
    window.location.href = "/";
  } else {
    alert("Mật khẩu không chính xác! (Mật khẩu mặc định: 123456)");
  }
});
