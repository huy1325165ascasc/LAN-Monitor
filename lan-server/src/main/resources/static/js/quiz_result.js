document.addEventListener("DOMContentLoaded", function () {
  const resultDataString = sessionStorage.getItem("current_quiz_result");

  if (!resultDataString) {
    alert("Không tìm thấy kết quả làm bài!");
    window.location.href = "/";
    return;
  }

  const resultData = JSON.parse(resultDataString);
  const questions = resultData.questions;
  const userAnswers = resultData.userAnswers;

  let correctCount = 0;
  questions.forEach((q, i) => {
    if (userAnswers[i] === q.correctAnswer) correctCount++;
  });

  const totalScore = ((correctCount * 10) / questions.length)
    .toFixed(2)
    .replace(".", ",");

  let m = Math.floor(resultData.timeSpent / 60);
  let s = resultData.timeSpent % 60;

  document.getElementById("res-quiz-name").textContent = resultData.quizName;
  document.getElementById("res-score").textContent = `${totalScore} / 10`;
  document.getElementById("res-time").textContent =
    `${m < 10 ? "0" + m : m} phút ${s < 10 ? "0" + s : s} giây`;

  const detailsList = document.getElementById("result-details-list");

  questions.forEach((q, index) => {
    const userAnswer = userAnswers[index];
    const isAnswered = userAnswer !== null;

    let imagesHtml = "";
    if (resultData.images) {
      const matched = resultData.images.filter(
        (img) => img.qIndex === q.originalIndex,
      );
      if (matched.length > 0) {
        imagesHtml = `<div class="d-flex flex-column gap-3 my-4">`;
        matched.forEach(
          (img) =>
            (imagesHtml += `<img src="${img.dataUrl}" class="question-attached-img">`),
        );
        imagesHtml += `</div>`;
      }
    }

    let optionsHtml = '<div class="d-flex flex-column gap-3 mb-4">';
    for (const [key, value] of Object.entries(q.options)) {
      const isCorrectChoice = q.correctAnswer === key;
      const isUserChoice = userAnswer === key;

      let boxClass = "result-option-box";
      let iconHtml = "";

      if (isCorrectChoice) {
        boxClass += " correct-answer";
        iconHtml = `<i class='bx bxs-check-circle text-success fs-4 ms-auto align-self-center'></i>`;
      } else if (isUserChoice && !isCorrectChoice) {
        boxClass += " wrong-answer";
        iconHtml = `<i class='bx bxs-x-circle text-danger fs-4 ms-auto align-self-center'></i>`;
      }

      optionsHtml += `
        <div class="${boxClass}">
          <span class="option-prefix">${key}.</span> <span>${value}</span> ${iconHtml}
        </div>
      `;
    }
    optionsHtml += "</div>";

    const cardHtml = `
      <div class="card border-0 shadow-sm rounded-4 p-4 mb-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold m-0">Câu ${index + 1}</h5>
          ${!isAnswered ? '<span class="badge bg-warning text-dark py-2 px-3 rounded-pill">Chưa làm</span>' : ""}
        </div>
        <p class="fs-6 text-dark" style="line-height: 1.6;">${q.question}</p>
        ${imagesHtml}
        ${optionsHtml}
      </div>
    `;
    detailsList.insertAdjacentHTML("beforeend", cardHtml);
  });
});
