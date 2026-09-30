document.addEventListener("DOMContentLoaded", function () {
  // 1. Kiểm tra Sinh viên đã đăng nhập chưa
  const studentId = sessionStorage.getItem("student_id");
  if (!studentId) {
    alert("Vui lòng đăng nhập để làm bài!");
    window.location.href = "/login";
    return;
  }

  // 2. Lấy ID bài thi
  const urlParams = new URLSearchParams(window.location.search);
  const quizId = urlParams.get("id");

  const btnStart = document.getElementById("btn-start-quiz-now");
  const overlay = document.getElementById("start-overlay");
  const workspace = document.getElementById("quiz-workspace");
  const btnBack = document.getElementById("btn-back-quiz");
  const gridContainer = document.getElementById("question-navigation-grid");
  const flagBtn = document.querySelector(".flag-btn");

  let timerInterval;
  let currentQuiz = null;
  let currentIndex = 0;
  let userAnswers = [];
  let flaggedQuestions = [];
  let startTime = Date.now();

  async function fetchQuizData() {
    try {
      const response = await fetch("/api/quizzes");
      const quizzes = await response.json();
      currentQuiz = quizzes.find((q) => q.id == quizId);

      if (!currentQuiz || !currentQuiz.data) {
        alert("Không tìm thấy dữ liệu bài thi!");
        window.location.href = "/";
        return;
      }

      currentQuiz.data.forEach((q, idx) => {
        q.originalIndex = idx;
      });

      // Đảo câu hỏi nếu cài đặt Random
      if (currentQuiz.isRandom) {
        for (let i = currentQuiz.data.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [currentQuiz.data[i], currentQuiz.data[j]] = [
            currentQuiz.data[j],
            currentQuiz.data[i],
          ];
        }
      }

      document.getElementById("quiz-title-display").textContent =
        currentQuiz.name;

      userAnswers = new Array(currentQuiz.data.length).fill(null);
      flaggedQuestions = new Array(currentQuiz.data.length).fill(false);

      renderQuestionGrid(currentQuiz.data.length);
      renderQuestion(0);
      startTime = Date.now();
    } catch (error) {
      console.error("Lỗi:", error);
      alert("Mất kết nối máy chủ!");
    }
  }

  function startTimer(durationInSeconds) {
    let timer = durationInSeconds;
    const display = document.getElementById("timer-display");

    timerInterval = setInterval(function () {
      let minutes = parseInt(timer / 60, 10);
      let seconds = parseInt(timer % 60, 10);
      display.textContent =
        (minutes < 10 ? "0" + minutes : minutes) +
        ":" +
        (seconds < 10 ? "0" + seconds : seconds);

      if (--timer < 0) {
        clearInterval(timerInterval);
        alert("Đã hết thời gian! Hệ thống tự động nộp bài.");
        submitQuiz();
      }
    }, 1000);
  }

  btnStart.addEventListener("click", function () {
    document.documentElement.requestFullscreen().catch(() => {});
    overlay.classList.add("d-none");
    workspace.classList.remove("d-none");
    fetchQuizData().then(() => {
      if (currentQuiz) startTimer(currentQuiz.timeLimit * 60);
    });
  });

  btnBack.addEventListener("click", function () {
    if (confirm("Thoát bài thi? Kết quả sẽ không được lưu!")) {
      clearInterval(timerInterval);
      window.location.href = "/";
    }
  });

  function renderQuestion(index) {
    currentIndex = index;
    const qData = currentQuiz.data[index];

    document.getElementById("current-q-num").textContent = index + 1;
    document.getElementById("question-text").innerHTML = qData.question;

    const imagesArea = document.getElementById("question-images-area");
    imagesArea.innerHTML = "";
    (currentQuiz.images || [])
      .filter((img) => img.qIndex === (qData.originalIndex ?? index))
      .forEach((img) => {
        imagesArea.innerHTML += `<img src="${img.dataUrl}" class="question-attached-img" alt="Ảnh câu hỏi">`;
      });

    flagBtn.classList.toggle("flagged", flaggedQuestions[index]);

    const optionsArea = document.getElementById("options-area");
    optionsArea.innerHTML = "";

    for (const [key, value] of Object.entries(qData.options)) {
      const isSelected = userAnswers[index] === key;
      const optDiv = document.createElement("div");
      optDiv.className = `option-box ${isSelected ? "selected" : ""}`;
      optDiv.innerHTML = `<span class="option-prefix">${key}.</span> <span>${value}</span>`;

      optDiv.addEventListener("click", function () {
        document
          .querySelectorAll(".option-box")
          .forEach((el) => el.classList.remove("selected"));
        this.classList.add("selected");
        userAnswers[index] = key;

        const gridBox = document.querySelector(
          `.q-nav-box[data-idx="${index}"]`,
        );
        if (gridBox) gridBox.classList.add("answered");
      });
      optionsArea.appendChild(optDiv);
    }

    document
      .querySelectorAll(".q-nav-box")
      .forEach((el) => el.classList.remove("active"));
    const activeBox = document.querySelector(
      `.q-nav-box[data-idx="${currentIndex}"]`,
    );
    if (activeBox) activeBox.classList.add("active");
  }

  document.getElementById("btn-prev-question").addEventListener("click", () => {
    if (currentIndex > 0) renderQuestion(currentIndex - 1);
  });

  document.getElementById("btn-next-question").addEventListener("click", () => {
    if (currentIndex < currentQuiz.data.length - 1)
      renderQuestion(currentIndex + 1);
  });

  function renderQuestionGrid(total) {
    gridContainer.innerHTML = "";
    for (let i = 0; i < total; i++) {
      const box = document.createElement("div");
      box.className = "q-nav-box";
      box.dataset.idx = i;
      box.innerHTML = `<span>${i + 1}</span><i class='bx bxs-flag flag-indicator'></i>`;
      box.addEventListener("click", () => renderQuestion(i));
      gridContainer.appendChild(box);
    }
  }

  flagBtn.addEventListener("click", function () {
    flaggedQuestions[currentIndex] = !flaggedQuestions[currentIndex];
    this.classList.toggle("flagged", flaggedQuestions[currentIndex]);
    document
      .querySelector(`.q-nav-box[data-idx="${currentIndex}"]`)
      .classList.toggle("flagged", flaggedQuestions[currentIndex]);
  });

  async function submitQuiz() {
    clearInterval(timerInterval);
    const resultData = {
      studentId: studentId,
      quizId: currentQuiz.id,
      quizName: currentQuiz.name,
      timeSpent: Math.floor((Date.now() - startTime) / 1000),
      questions: currentQuiz.data,
      userAnswers: userAnswers,
    };

    try {
      await fetch("/api/quizzes/submit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(resultData),
      });
    } catch (err) {
      alert("Lỗi khi nộp bài lên Server!");
    }

    resultData.images = currentQuiz.images || [];
    sessionStorage.setItem("current_quiz_result", JSON.stringify(resultData));
    window.location.href = "/quiz_result";
  }

  document
    .getElementById("btn-finish-quiz")
    .addEventListener("click", function (e) {
      e.preventDefault();
      if (confirm("Bạn có chắc chắn muốn nộp bài ngay bây giờ?")) submitQuiz();
    });
});
