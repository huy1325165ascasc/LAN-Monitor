(function () {
  const QUIZ_PROMPT_TEMPLATE = `Bạn đóng vai trò là một chuyên gia xử lý dữ liệu giáo dục và là một học giả uyên bác. Hãy phân tích file tài liệu đính kèm (Word/TXT) chứa các câu hỏi trắc nghiệm và trích xuất toàn bộ nội dung thành một mảng JSON thuần túy (Pure JSON Array).

NHỮNG YÊU CẦU BẮT BUỘC (TUYỆT ĐỐI TUÂN THỦ):
1. ĐỊNH DẠNG ĐẦU RA: Chỉ trả về duy nhất mảng JSON, bắt đầu bằng "[" và kết thúc bằng "]". Tuyệt đối không thêm text ở ngoài.
2. CHUẨN HÓA: Bỏ các tiền tố thứ tự (Câu 1:, A., B., C., D.).
3. XÁC ĐỊNH ĐÁP ÁN ĐÚNG (correctAnswer): Điền chữ cái tương ứng (A, B, C hoặc D) vào trường "correctAnswer". Tuyệt đối không được để trống.
4. LỜI GIẢI THÍCH (explanation): Tự biên soạn hoặc trích xuất lời giải thích.

CẤU TRÚC JSON MẪU BẮT BUỘC:
[ { "question": "Nội dung câu hỏi", "options": { "A": "Đáp án 1", "B": "Đáp án 2", "C": "Đáp án 3", "D": "Đáp án 4" }, "correctAnswer": "A", "explanation": "Lời giải thích" } ]`;

  let quizzes = [];
  let currentEditId = null;
  let currentEditTime = 15;
  let currentAttachedImages = [];

  document.addEventListener("DOMContentLoaded", function () {
    const els = {
      list: document.getElementById("quizzes-list"),
      emptyDisplay: document.getElementById("quizzes-empty"),
      addModal: new bootstrap.Modal(document.getElementById("addQuizModal")),
      editModal: new bootstrap.Modal(document.getElementById("editQuizModal")),
      quizNameInput: document.getElementById("quiz-name-input"),
      btnCreate: document.getElementById("btn-create-quiz"),
      btnCopyPrompt: document.getElementById("btn-copy-prompt"),
      editNameInput: document.getElementById("edit-quiz-name-input"),
      editDataInput: document.getElementById("edit-quiz-data"),
      btnSaveData: document.getElementById("btn-save-quiz-data"),
      btnTimeUp: document.getElementById("btn-time-up"),
      btnTimeDown: document.getElementById("btn-time-down"),
      timeDisplay: document.getElementById("quiz-time-display"),
      randomSwitch: document.getElementById("quiz-random-switch"),
      questionImageSelect: document.getElementById("question-image-select"),
      fileUploadInput: document.getElementById("quiz-image-upload"),
      btnBrowseImage: document.getElementById("btn-browse-image"),
      attachedImagesList: document.getElementById("attached-images-list"),
    };

    function showConfirmModal(message, onConfirm) {
      const modalEl = document.getElementById("confirmModal");
      const modalInstance =
        bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      document.getElementById("confirmModalMessage").innerText = message;

      const btnConfirm = document.getElementById("confirmModalBtn");
      const newBtnConfirm = btnConfirm.cloneNode(true);
      btnConfirm.parentNode.replaceChild(newBtnConfirm, btnConfirm);

      newBtnConfirm.addEventListener("click", () => {
        if (onConfirm) onConfirm();
        modalInstance.hide();
      });
      modalInstance.show();
    }

    async function loadQuizzesFromAPI() {
      try {
        const res = await fetch("/api/quizzes");
        quizzes = res.ok ? await res.json() : [];
      } catch (error) {
        quizzes = [];
      }
    }

    async function saveQuizzesToAPI() {
      try {
        await fetch("/api/quizzes", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(quizzes),
        });
      } catch (error) {
        if (window.notify) window.notify("Lỗi lưu API", "error");
      }
    }

    async function initApp() {
      await loadQuizzesFromAPI();
      renderQuizzes();
      bindEvents();
    }

    function bindEvents() {
      els.btnTimeUp.addEventListener("click", () => {
        currentEditTime += 5;
        els.timeDisplay.textContent = `${currentEditTime} phút`;
      });

      els.btnTimeDown.addEventListener("click", () => {
        if (currentEditTime > 5) {
          currentEditTime -= 5;
          els.timeDisplay.textContent = `${currentEditTime} phút`;
        } else {
          if (window.notify) window.notify("Tối thiểu 5 phút!", "warning");
        }
      });

      els.btnCopyPrompt.addEventListener("click", () => {
        navigator.clipboard.writeText(QUIZ_PROMPT_TEMPLATE).then(() => {
          if (window.notify) window.notify("Đã chép lệnh Prompt!", "success");
        });
      });

      els.btnCreate.addEventListener("click", () => {
        const name = els.quizNameInput.value.trim();
        if (!name) return window.notify("Nhập tên bài thi!", "warning");

        const newQuiz = {
          id: Date.now(),
          name: name,
          type: "delayed",
          data: null,
          timeLimit: 15,
          isRandom: false,
          images: [],
          isValid: false,
          createdAt: Date.now(),
        };

        quizzes.unshift(newQuiz);
        saveQuizzesToAPI();
        renderQuizzes();

        els.quizNameInput.value = "";
        els.addModal.hide();
        window.notify("Đã tạo sườn bài thi!", "success");
      });

      els.editDataInput.addEventListener("input", updateQuestionSelect);
      els.btnBrowseImage.addEventListener("click", () =>
        els.fileUploadInput.click(),
      );

      els.fileUploadInput.addEventListener("change", async function () {
        const qIndex = els.questionImageSelect.value;
        if (qIndex === "") {
          window.notify("Chọn câu hỏi trước!", "warning");
          this.value = "";
          return;
        }

        for (let file of Array.from(this.files)) {
          try {
            const base64String = await new Promise((resolve, reject) => {
              const reader = new FileReader();
              reader.readAsDataURL(file);
              reader.onload = () => resolve(reader.result);
              reader.onerror = (error) => reject(error);
            });
            const ext = file.name.split(".").pop();
            const uniqueFileName = `img_${Date.now()}_${Math.floor(Math.random() * 1000)}.${ext}`;

            currentAttachedImages.push({
              qIndex: parseInt(qIndex),
              fileName: uniqueFileName,
              dataUrl: base64String,
            });
          } catch (err) {}
        }
        renderAttachedImages();
        this.value = "";
      });

      els.btnSaveData.addEventListener("click", () => {
        if (!currentEditId) return;

        const newName = els.editNameInput.value.trim();
        if (!newName) return window.notify("Tên không được trống!", "warning");

        const rawData = els.editDataInput.value.trim();
        let parsedData = null;
        let isValid = false;

        if (rawData) {
          try {
            parsedData = JSON.parse(rawData);
            isValid =
              Array.isArray(parsedData) &&
              parsedData.every(
                (item) => item.question && item.options && item.correctAnswer,
              );
            if (!isValid) throw new Error("JSON lỗi");
          } catch (error) {
            return window.notify("Dữ liệu JSON sai cấu trúc!", "error");
          }
        }

        const quizIndex = quizzes.findIndex((q) => q.id === currentEditId);
        if (quizIndex > -1) {
          quizzes[quizIndex].name = newName;
          quizzes[quizIndex].data = parsedData;
          quizzes[quizIndex].timeLimit = currentEditTime;
          quizzes[quizIndex].isRandom = els.randomSwitch.checked;
          quizzes[quizIndex].images = currentAttachedImages;
          quizzes[quizIndex].isValid = isValid;

          saveQuizzesToAPI();
          renderQuizzes();
          els.editModal.hide();
          window.notify("Đã lưu đề thi!", "success");
        }
      });
    }

    function updateQuestionSelect() {
      els.questionImageSelect.innerHTML =
        '<option value="" disabled selected>-- Chọn câu --</option>';
      try {
        const data = JSON.parse(els.editDataInput.value);
        if (Array.isArray(data)) {
          data.forEach((_, idx) => {
            const opt = document.createElement("option");
            opt.value = idx;
            opt.textContent = `Câu ${idx + 1}`;
            els.questionImageSelect.appendChild(opt);
          });
        }
      } catch (e) {}
    }

    function renderAttachedImages() {
      els.attachedImagesList.innerHTML = "";
      if (currentAttachedImages.length === 0) {
        els.attachedImagesList.classList.add("d-none");
        return;
      }
      els.attachedImagesList.classList.remove("d-none");

      currentAttachedImages
        .sort((a, b) => a.qIndex - b.qIndex)
        .forEach((img) => {
          const li = document.createElement("li");
          li.className = "attached-img-item";
          li.innerHTML = `
          <span class="attached-img-name">Câu ${img.qIndex + 1} - ${img.fileName}</span>
          <i class='bx bx-trash attached-img-delete' title="Xóa" style="cursor:pointer; color:#ef4444;"></i>
        `;

          li.querySelector(".attached-img-delete").addEventListener(
            "click",
            async () => {
              const idx = currentAttachedImages.findIndex(
                (i) => i.fileName === img.fileName,
              );
              if (idx > -1) {
                try {
                  await fetch(`/api/quizzes/image/${img.fileName}`, {
                    method: "DELETE",
                  });
                } catch (e) {}
                currentAttachedImages.splice(idx, 1);
                renderAttachedImages();
              }
            },
          );
          els.attachedImagesList.appendChild(li);
        });
    }

    function renderQuizzes() {
      els.list.innerHTML = "";
      if (quizzes.length === 0) {
        els.emptyDisplay.classList.remove("d-none");
        return;
      }
      els.emptyDisplay.classList.add("d-none");

      quizzes.forEach((quiz) => {
        const col = document.createElement("div");
        col.className = "col position-relative";

        const borderColor = quiz.isValid ? "#10b981" : "#cbd5e1";
        const bgGradient = "linear-gradient(to right, #ffffff, #f8fafc)";
        const iconHtml = quiz.isValid
          ? `<i class="bx bxs-check-circle text-success quiz-status-icon" title="Sẵn sàng"></i>`
          : `<i class="bx bxs-x-circle text-danger quiz-status-icon" title="Thiếu dữ liệu JSON"></i>`;

        col.innerHTML = `
          <div class="quiz-card" style="border-left: 5px solid ${borderColor}; background: ${bgGradient};">
            ${iconHtml}
            <h5 class="fw-bold mb-3 text-dark text-truncate" title="${escapeHtml(quiz.name)}" style="padding-right: 25px;">
              ${escapeHtml(quiz.name)}
            </h5>
            <p class="text-muted small mb-2"><i class="bx bx-time me-2 fs-6"></i> ${quiz.timeLimit} Phút</p>
            <p class="text-muted small mb-4"><i class="bx bx-collection me-2 fs-6"></i> ${quiz.isValid ? quiz.data.length : 0} câu hỏi</p>
            <div class="d-flex flex-wrap gap-2 mt-auto">
              <button class="btn btn-sm rounded-pill btn-start shadow-sm" style="background-color: ${quiz.isValid ? "#3b82f6" : "#cbd5e1"}; color: white; border: none;" ${quiz.isValid ? "" : "disabled"}>
                <i class="bx bx-play fs-5 me-1"></i> Thi Thử
              </button>
              <button class="btn btn-outline-secondary btn-sm rounded-pill btn-edit shadow-sm"><i class="bx bx-edit fs-6 me-1"></i> Sửa JSON</button>
              <button class="btn btn-outline-danger btn-sm rounded-pill btn-delete bg-white shadow-sm"><i class="bx bx-trash fs-6"></i></button>
            </div>
          </div>
        `;

        col.querySelector(".btn-edit").addEventListener("click", () => {
          currentEditId = quiz.id;
          els.editNameInput.value = quiz.name;
          currentEditTime = quiz.timeLimit || 15;
          els.timeDisplay.textContent = `${currentEditTime} phút`;
          els.randomSwitch.checked = quiz.isRandom || false;
          els.editDataInput.value = quiz.data
            ? JSON.stringify(quiz.data, null, 2)
            : "";
          updateQuestionSelect();
          currentAttachedImages = quiz.images ? [...quiz.images] : [];
          renderAttachedImages();
          els.editModal.show();
        });

        col.querySelector(".btn-delete").addEventListener("click", () => {
          showConfirmModal("Xác nhận xóa bộ đề thi này?", () => {
            quizzes = quizzes.filter((q) => q.id !== quiz.id);
            saveQuizzesToAPI();
            renderQuizzes();
            window.notify("Đã xóa đề thi!", "success");
          });
        });

        col.querySelector(".btn-start").addEventListener("click", () => {
          sessionStorage.setItem("student_id", "GIAO_VIEN_THI_THU");
          window.open(`/take_quiz?id=${quiz.id}`, "_blank");
        });

        els.list.appendChild(col);
      });
    }

    function escapeHtml(value) {
      return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
    }

    initApp();
  });
})();
