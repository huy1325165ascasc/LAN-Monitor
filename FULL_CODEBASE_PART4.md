# =============================================
# PHẦN 4: LAN-SERVER (CSS & JS Files)
# =============================================

## FILE: static/css/style.css

```css
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-track { background: #0f172a; border-radius: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #334155; border-radius: 4px; }
.custom-scroll::-webkit-scrollbar-thumb:hover { background: #475569; }
.client-item { transition: all 0.2s ease-in-out; }
.client-item.active { border-color: #6366f1; background-color: rgba(99, 102, 241, 0.1); }
```

## FILE: static/css/login.css

```css
body { background-color: #f8fafc; }
.login-card { width: 100%; max-width: 420px; border: none; border-radius: 20px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05); }
.icon-wrapper { width: 70px; height: 70px; background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3); }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 0.25rem rgba(59, 130, 246, 0.1); }
.btn-login { background: linear-gradient(135deg, #3b82f6, #2563eb); border: none; transition: transform 0.2s, box-shadow 0.2s; }
.btn-login:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3); }
```

## FILE: static/css/quiz.css

```css
.quiz-card { position: relative; background: #ffffff; border-radius: 16px; padding: 20px; border: 1px solid #e0e0e0; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.04); transition: all 0.3s ease; display: flex; flex-direction: column; height: 100%; }
.quiz-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); }
.quiz-status-icon { position: absolute; top: 10px; right: 10px; font-size: 20px; }
.quiz-title { font-size: 18px; font-weight: 700; color: #1e293b; margin-bottom: 8px; padding-right: 30px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.quiz-type-badge { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; font-weight: 600; padding: 5px 10px; border-radius: 20px; background-color: #f1f5f9; color: #475569; margin-bottom: 20px; width: fit-content; }
.attached-img-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; background: #ffffff; border-bottom: 1px solid #f1f5f9; }
.attached-img-item:last-child { border-bottom: none; }
.attached-img-name { font-weight: 500; color: #334155; font-size: 14px; }
.attached-img-delete { color: #ef4444; cursor: pointer; padding: 6px; border-radius: 6px; transition: all 0.2s; font-size: 18px; }
.attached-img-delete:hover { background: #fee2e2; }
```

## FILE: static/css/take_quiz.css

```css
body { background-color: #f8f9fa; min-height: 100vh; }
.start-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(236, 243, 251, 0.95); z-index: 9999; display: flex; align-items: center; justify-content: center; }
.flag-btn:hover { background-color: #f1f5f9; border-color: #94a3b8; }
.flag-btn.flagged .flag-icon, .flag-btn.flagged span { color: #ef4444 !important; }
.question-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-bottom: 20px; }
.q-nav-box { background-color: #ffffff; border: 1.5px solid #cbd5e1; color: #334155; border-radius: 8px; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 15px; cursor: pointer; transition: all 0.2s; position: relative; }
.q-nav-box:hover { background-color: #f1f5f9; }
.q-nav-box.active { border-color: #3b82f6; background-color: #eff6ff; color: #2563eb; }
.q-nav-box.answered { background-color: #10b981; border-color: #10b981; color: #ffffff; }
.q-nav-box .flag-indicator { display: none; position: absolute; top: -6px; right: -6px; width: 18px; height: 18px; background-color: #ef4444; color: #ffffff; border-radius: 50%; border: 2px solid #ffffff; font-size: 10px; align-items: center; justify-content: center; z-index: 5; }
.q-nav-box.flagged .flag-indicator { display: flex; }
.option-box { border: 2px solid #e2e8f0; border-radius: 12px; padding: 14px 20px; cursor: pointer; transition: all 0.2s; font-weight: 500; color: #334155; display: flex; align-items: flex-start; gap: 12px; background: #fff; }
.option-box:hover { background-color: #f8fafc; border-color: #cbd5e1; }
.option-box.selected { border-color: #3b82f6; background-color: #eff6ff; color: #1d4ed8; }
.option-prefix { font-weight: 700; min-width: 24px; }
.question-attached-img { max-width: 100%; max-height: 400px; object-fit: contain; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05); background-color: #f8fafc; padding: 4px; }
```

## FILE: static/css/quiz_result.css

```css
.fw-black { font-weight: 900; }
.result-option-box { border: 2px solid #e2e8f0; border-radius: 12px; padding: 14px 20px; font-weight: 500; color: #334155; display: flex; align-items: flex-start; gap: 12px; background-color: #ffffff; }
.result-option-box.correct-answer { border-color: #10b981 !important; background-color: #ecfdf5 !important; }
.result-option-box.wrong-answer { border-color: #ef4444 !important; background-color: #fef2f2 !important; color: #b91c1c !important; }
.quiz-header { position: fixed; top: 0; left: 0; width: 100%; z-index: 1050; }
#quiz-workspace main, body > main.container { margin-top: 75px; }
```

## FILE: static/css/toast.css

```css
#toast-container { position: fixed; top: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px; }
.toast { min-width: 280px; padding: 12px 20px; border-radius: 8px; color: #fff; font-size: 14px; font-weight: 500; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); animation: slideIn 0.3s ease forwards; }
.toast-success { background-color: #10b981; }
.toast-error { background-color: #ef4444; }
.toast-warning { background-color: #f59e0b; }
.toast-close { cursor: pointer; font-size: 18px; opacity: 0.7; margin-left: 10px; }
@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
```

## FILE: static/js/toast.js

```javascript
let lastToastMessage = "";
let lastToastTime = 0;

function getOrCreateToastContainer() {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.style.cssText = "position: fixed; top: 20px; right: 20px; z-index: 99999; display: flex; flex-direction: column; gap: 10px; pointer-events: none;";
    document.body.appendChild(container);
  }
  return container;
}

window.showToast = function (message, type = "success") {
  const now = Date.now();
  if (message === lastToastMessage && now - lastToastTime < 1500) return;
  lastToastMessage = message; lastToastTime = now;
  
  const container = getOrCreateToastContainer();
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.style.cssText = `pointer-events: auto; min-width: 280px; padding: 12px 20px; border-radius: 8px; color: #fff; font-size: 14px; font-weight: 500; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); background-color: ${type === 'danger' || type === 'error' ? '#ef4444' : (type === 'warning' ? '#f59e0b' : '#10b981')}; opacity: 0; transform: translateX(100%); transition: all 0.3s ease;`;
  toast.innerHTML = `<span style="flex-grow: 1; margin-right: 12px;">${message}</span><span style="cursor: pointer; font-size: 18px; opacity: 0.8; font-weight: bold;" onclick="this.parentElement.remove()">×</span>`;
  container.appendChild(toast);
  
  setTimeout(() => { toast.style.opacity = "1"; toast.style.transform = "translateX(0)"; }, 10);
  setTimeout(() => { toast.style.opacity = "0"; toast.style.transform = "translateX(50%)"; setTimeout(() => toast.remove(), 300); }, 3500);
};

window.notify = function (msg, type = 'success') { window.showToast(msg, type); };
```

## FILE: static/js/app.js

```javascript
let stompClient = null;
let selectedClientId = null;
let currentSubscriptions = {};

function connectWebSocket() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = null;
  stompClient.connect({}, function () {
      const statusEl = document.getElementById("socket-status");
      statusEl.innerText = "WebSocket Đã Kết Nối";
      statusEl.className = "px-3 py-1 text-xs rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20";
      stompClient.subscribe("/topic/clients", function (message) {
        renderClientList(JSON.parse(message.body));
      });
      stompClient.subscribe("/topic/alerts", function (message) {
        const alertData = JSON.parse(message.body);
        addSuspiciousAlert(alertData.pcName, alertData.tabName);
        const clientCard = document.querySelector(`.client-item[onclick*="${alertData.clientId}"]`);
        if (clientCard) {
          clientCard.classList.add("border-rose-500", "animate-pulse");
          setTimeout(() => clientCard.classList.remove("border-rose-500", "animate-pulse"), 5000);
        }
      });
      fetchInitialClients();
    },
    function () {
      document.getElementById("socket-status").innerText = "Mất kết nối WebSocket!";
      setTimeout(connectWebSocket, 4000);
    }
  );
}

async function fetchInitialClients() {
  try {
    const res = await fetch("/api/clients");
    renderClientList(await res.json());
  } catch (e) {}
}

function renderClientList(clients) {
  const container = document.getElementById("client-grid");
  document.getElementById("client-count").innerText = `${clients.length} online`;
  if (clients.length === 0) {
    container.innerHTML = `<div class="col-span-full text-center py-10 text-slate-500 text-sm">Chưa có máy nào kết nối...</div>`;
    selectedClientId = null; updateSelectedTag();
    return;
  }
  container.innerHTML = "";
  clients.forEach((c) => {
    const isActive = c.id === selectedClientId;
    const item = document.createElement("div");
    item.className = `client-item flex flex-col justify-between p-4 rounded-xl border ${isActive ? "border-indigo-500 bg-indigo-500/10" : "border-slate-700 bg-slate-900/60"} transition-all`;
    item.innerHTML = `
      <div class="flex items-center justify-between mb-4 border-b border-slate-700/50 pb-3">
        <div class="flex items-center space-x-2 cursor-pointer" onclick="selectClient('${c.id}')">
          <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
          <span class="font-bold text-sm text-slate-200">${c.pcName || "Máy Trạm"}</span>
        </div>
        <span class="text-xs font-mono text-slate-400">${c.ipAddress}</span>
      </div>
      <div class="mt-auto">
        <button onclick="openLiveStream('${c.id}', '${c.pcName || "Máy Trạm"}')" class="w-full bg-indigo-600/80 hover:bg-indigo-500 text-white text-sm py-2 rounded-md transition flex items-center justify-center gap-2">Xem Live Stream</button>
      </div>`;
    container.appendChild(item);
  });
  if (!selectedClientId && clients.length > 0) selectClient(clients[0].id);
}

async function openImagesFolder() {
  try {
    await fetch("/api/clients/open-images-folder", { method: "POST" });
  } catch (e) {}
}

function selectClient(clientId) {
  selectedClientId = clientId;
  updateSelectedTag();
}
function updateSelectedTag() {
  document.getElementById("selected-client-tag").innerText = selectedClientId ? `Đang điều khiển: ${selectedClientId}` : "Chưa chọn máy trạm";
}

function addSuspiciousAlert(machineName, tabName) {
  const feedContainer = document.getElementById("alert-feed");
  const timeString = new Date().toLocaleTimeString("vi-VN", { hour12: false });
  const alertBox = document.createElement("div");
  alertBox.className = "bg-slate-900 border-l-4 border-rose-500 rounded-r-lg p-3 shadow-md animate-[pulse_0.5s_ease-in-out]";
  alertBox.innerHTML = `
    <div class="flex justify-between items-start mb-1"><span class="text-[11px] font-mono text-slate-400">${timeString}</span></div>
    <div><span class="font-bold text-sm text-slate-200">${machineName}</span><p class="text-xs text-rose-400 mt-0.5 line-clamp-2">Đang mở: ${tabName}</p></div>`;
  feedContainer.prepend(alertBox);
}

let liveStreamInterval = null;
let activeLiveStreamClient = null;

function openLiveStream(clientId, pcName) {
  activeLiveStreamClient = clientId;
  document.getElementById("livestream-title").innerText = pcName;
  const modal = document.getElementById("livestream-modal");
  modal.classList.remove("hidden"); modal.classList.add("flex");
  const topic = `/topic/screenshot/${clientId}`;
  if (!currentSubscriptions[topic] && stompClient) {
    currentSubscriptions[topic] = stompClient.subscribe(topic, function (msg) {
      if (activeLiveStreamClient === clientId) document.getElementById("livestream-img").src = "data:image/jpeg;base64," + msg.body;
    });
  }
  if (liveStreamInterval) clearInterval(liveStreamInterval);
  liveStreamInterval = setInterval(() => {
    fetch(`/api/clients/${encodeURIComponent(clientId)}/capture`, { method: "POST" }).catch(e => {});
  }, 250);
}

function closeLiveStream() {
  if (liveStreamInterval) clearInterval(liveStreamInterval);
  activeLiveStreamClient = null;
  const modal = document.getElementById("livestream-modal");
  modal.classList.add("hidden"); modal.classList.remove("flex");
  document.getElementById("livestream-img").src = "";
}

async function captureLivestreamFrame() {
  if (!activeLiveStreamClient) return;
  try {
    const res = await fetch(`/api/clients/${encodeURIComponent(activeLiveStreamClient)}/save-frame`, { method: "POST" });
    if (res.ok) alert("📸 Đã chụp và lưu thành công tấm ảnh minh chứng!");
  } catch (e) {}
}

async function openWhitelistModal() {
  document.getElementById("whitelist-modal").classList.remove("hidden");
  document.getElementById("whitelist-modal").classList.add("flex");
  try {
    const res = await fetch("/api/settings/whitelist");
    document.getElementById("whitelist-input").value = (await res.json()).join(", ");
  } catch (e) {}
}

function closeWhitelistModal() {
  document.getElementById("whitelist-modal").classList.add("hidden");
  document.getElementById("whitelist-modal").classList.remove("flex");
}

async function saveWhitelist() {
  const inputStr = document.getElementById("whitelist-input").value;
  const whitelistArray = inputStr.split(",").map(item => item.trim()).filter(item => item.length > 0);
  if (whitelistArray.length === 0) return alert("Vui lòng nhập ít nhất một từ khóa hợp lệ!");
  try {
    const res = await fetch("/api/settings/whitelist", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ whitelist: whitelistArray }),
    });
    if (res.ok) { alert("Đã lưu cấu hình!"); closeWhitelistModal(); }
  } catch (e) { alert("Lỗi khi lưu: " + e.message); }
}

document.addEventListener("DOMContentLoaded", connectWebSocket);
```

## FILE: static/js/login.js

```javascript
document.getElementById("loginForm").addEventListener("submit", function (e) {
  e.preventDefault();
  const msvInput = document.getElementById("studentId").value.trim().toUpperCase();
  const pwdInput = document.getElementById("password").value;
  if (pwdInput === "123456") {
    sessionStorage.setItem("student_id", msvInput);
    window.location.href = "/";
  } else {
    alert("Mật khẩu không chính xác! (Mật khẩu mặc định: 123456)");
  }
});
```

## FILE: static/js/student_home.js

```javascript
const studentId = sessionStorage.getItem("student_id");
if (!studentId) { window.location.href = "/login"; } 
else { document.addEventListener("DOMContentLoaded", () => { document.getElementById("student-name-display").innerHTML = `<i class='bx bxs-user-circle me-1'></i>Sinh Viên: ${studentId}`; }); }

window.logout = function () { sessionStorage.removeItem("student_id"); window.location.href = "/login"; };
window.confirmStart = function () { return confirm("Bạn đã sẵn sàng làm bài chưa?"); };

document.addEventListener("DOMContentLoaded", async function () {
  const container = document.getElementById("quiz-container");
  try {
    const response = await fetch("/api/quizzes");
    const quizzes = await response.json();
    container.innerHTML = "";
    if (!quizzes || quizzes.length === 0) {
      container.innerHTML = `<div class="col-12 text-center mt-5"><h5>Hiện chưa có bài thi nào!</h5></div>`; return;
    }
    quizzes.forEach((quiz) => {
      const cardHtml = `
        <div class="col-md-5 col-lg-4">
          <div class="card border-0 shadow-sm rounded-4 h-100 p-2">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title fw-bold text-dark m-0 lh-base">${quiz.name}</h5>
              <p class="card-text text-muted small mb-4">Thời gian: ${quiz.timeLimit} phút</p>
              <a href="/take_quiz?id=${quiz.id}" class="btn btn-primary rounded-pill fw-bold mt-auto w-100 py-2" onclick="return confirmStart()">Bắt Đầu Làm Bài</a>
            </div>
          </div>
        </div>`;
      container.insertAdjacentHTML("beforeend", cardHtml);
    });
  } catch (error) {}
});
```

## FILE: static/js/quiz.js

```javascript
(function () {
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
      const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      document.getElementById("confirmModalMessage").innerText = message;
      const btnConfirm = document.getElementById("confirmModalBtn");
      const newBtnConfirm = btnConfirm.cloneNode(true);
      btnConfirm.parentNode.replaceChild(newBtnConfirm, btnConfirm);
      newBtnConfirm.addEventListener("click", () => { if (onConfirm) onConfirm(); modalInstance.hide(); });
      modalInstance.show();
    }

    async function loadQuizzesFromAPI() { try { quizzes = await (await fetch("/api/quizzes")).json(); } catch (e) { quizzes = []; } }
    async function saveQuizzesToAPI() { await fetch("/api/quizzes", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(quizzes) }); }

    async function initApp() { await loadQuizzesFromAPI(); renderQuizzes(); bindEvents(); }

    function bindEvents() {
      els.btnTimeUp.addEventListener("click", () => { currentEditTime += 5; els.timeDisplay.textContent = `${currentEditTime} phút`; });
      els.btnTimeDown.addEventListener("click", () => { if (currentEditTime > 5) { currentEditTime -= 5; els.timeDisplay.textContent = `${currentEditTime} phút`; } });
      els.btnCreate.addEventListener("click", () => {
        const name = els.quizNameInput.value.trim();
        if (!name) return;
        quizzes.unshift({ id: Date.now(), name: name, data: null, timeLimit: 15, isRandom: false, images: [], isValid: false, createdAt: Date.now() });
        saveQuizzesToAPI(); renderQuizzes();
        els.quizNameInput.value = ""; els.addModal.hide();
      });
      els.editDataInput.addEventListener("input", updateQuestionSelect);
      els.btnBrowseImage.addEventListener("click", () => els.fileUploadInput.click());
      els.fileUploadInput.addEventListener("change", async function () {
        const qIndex = els.questionImageSelect.value;
        if (qIndex === "") return;
        for (let file of Array.from(this.files)) {
          const base64String = await new Promise((resolve) => { const reader = new FileReader(); reader.readAsDataURL(file); reader.onload = () => resolve(reader.result); });
          currentAttachedImages.push({ qIndex: parseInt(qIndex), fileName: `img_${Date.now()}.jpg`, dataUrl: base64String });
        }
        renderAttachedImages(); this.value = "";
      });
      els.btnSaveData.addEventListener("click", () => {
        const newName = els.editNameInput.value.trim();
        let parsedData = null, isValid = false;
        try { parsedData = JSON.parse(els.editDataInput.value.trim()); isValid = Array.isArray(parsedData); } catch (e) {}
        const quizIndex = quizzes.findIndex((q) => q.id === currentEditId);
        if (quizIndex > -1) {
          quizzes[quizIndex].name = newName; quizzes[quizIndex].data = parsedData; quizzes[quizIndex].timeLimit = currentEditTime;
          quizzes[quizIndex].isRandom = els.randomSwitch.checked; quizzes[quizIndex].images = currentAttachedImages; quizzes[quizIndex].isValid = isValid;
          saveQuizzesToAPI(); renderQuizzes(); els.editModal.hide();
        }
      });
    }

    function updateQuestionSelect() {
      els.questionImageSelect.innerHTML = '<option value="" disabled selected>-- Chọn câu --</option>';
      try { const data = JSON.parse(els.editDataInput.value); if (Array.isArray(data)) data.forEach((_, idx) => els.questionImageSelect.insertAdjacentHTML('beforeend', `<option value="${idx}">Câu ${idx + 1}</option>`)); } catch (e) {}
    }

    function renderAttachedImages() {
      els.attachedImagesList.innerHTML = "";
      currentAttachedImages.forEach(img => {
        const li = document.createElement("li"); li.className = "attached-img-item";
        li.innerHTML = `<span class="attached-img-name">Câu ${img.qIndex + 1}</span><i class='bx bx-trash attached-img-delete'></i>`;
        li.querySelector(".attached-img-delete").addEventListener("click", () => {
          currentAttachedImages = currentAttachedImages.filter(i => i.fileName !== img.fileName); renderAttachedImages();
        });
        els.attachedImagesList.appendChild(li);
      });
    }

    function renderQuizzes() {
      els.list.innerHTML = "";
      if (quizzes.length === 0) { els.emptyDisplay.classList.remove("d-none"); return; }
      els.emptyDisplay.classList.add("d-none");
      quizzes.forEach((quiz) => {
        const col = document.createElement("div"); col.className = "col position-relative";
        col.innerHTML = `
          <div class="quiz-card" style="border-left: 5px solid ${quiz.isValid ? "#10b981" : "#cbd5e1"}">
            <h5 class="fw-bold mb-3 text-dark text-truncate">${quiz.name}</h5>
            <div class="d-flex flex-wrap gap-2 mt-auto">
              <button class="btn btn-sm rounded-pill btn-start" ${quiz.isValid ? "" : "disabled"}>Thi Thử</button>
              <button class="btn btn-outline-secondary btn-sm rounded-pill btn-edit">Sửa JSON</button>
              <button class="btn btn-outline-danger btn-sm rounded-pill btn-delete"><i class="bx bx-trash fs-6"></i></button>
            </div>
          </div>`;
        col.querySelector(".btn-edit").addEventListener("click", () => {
          currentEditId = quiz.id; els.editNameInput.value = quiz.name; currentEditTime = quiz.timeLimit || 15; els.timeDisplay.textContent = `${currentEditTime} phút`;
          els.randomSwitch.checked = quiz.isRandom || false; els.editDataInput.value = quiz.data ? JSON.stringify(quiz.data, null, 2) : "";
          updateQuestionSelect(); currentAttachedImages = quiz.images ? [...quiz.images] : []; renderAttachedImages(); els.editModal.show();
        });
        col.querySelector(".btn-delete").addEventListener("click", () => showConfirmModal("Xóa đề này?", () => { quizzes = quizzes.filter(q => q.id !== quiz.id); saveQuizzesToAPI(); renderQuizzes(); }));
        col.querySelector(".btn-start").addEventListener("click", () => { sessionStorage.setItem("student_id", "GIAO_VIEN_THI_THU"); window.open(`/take_quiz?id=${quiz.id}`, "_blank"); });
        els.list.appendChild(col);
      });
    }
    initApp();
  });
})();
```

## FILE: static/js/take_quiz.js

```javascript
document.addEventListener("DOMContentLoaded", function () {
  const studentId = sessionStorage.getItem("student_id");
  if (!studentId) { alert("Vui lòng đăng nhập!"); window.location.href = "/login"; return; }
  const quizId = new URLSearchParams(window.location.search).get("id");
  let timerInterval, currentQuiz = null, currentIndex = 0, userAnswers = [], flaggedQuestions = [], startTime = Date.now();

  async function fetchQuizData() {
    try {
      const quizzes = await (await fetch("/api/quizzes")).json();
      currentQuiz = quizzes.find((q) => q.id == quizId);
      currentQuiz.data.forEach((q, idx) => { q.originalIndex = idx; });
      if (currentQuiz.isRandom) {
        for (let i = currentQuiz.data.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [currentQuiz.data[i], currentQuiz.data[j]] = [currentQuiz.data[j], currentQuiz.data[i]];
        }
      }
      document.getElementById("quiz-title-display").textContent = currentQuiz.name;
      userAnswers = new Array(currentQuiz.data.length).fill(null);
      flaggedQuestions = new Array(currentQuiz.data.length).fill(false);
      renderQuestionGrid(currentQuiz.data.length); renderQuestion(0); startTime = Date.now();
    } catch (e) {}
  }

  function startTimer(durationInSeconds) {
    let timer = durationInSeconds;
    timerInterval = setInterval(function () {
      let minutes = parseInt(timer / 60, 10), seconds = parseInt(timer % 60, 10);
      document.getElementById("timer-display").textContent = (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
      if (--timer < 0) { clearInterval(timerInterval); submitQuiz(); }
    }, 1000);
  }

  document.getElementById("btn-start-quiz-now").addEventListener("click", function () {
    document.documentElement.requestFullscreen().catch(() => {});
    document.getElementById("start-overlay").classList.add("d-none");
    document.getElementById("quiz-workspace").classList.remove("d-none");
    fetchQuizData().then(() => { if (currentQuiz) startTimer(currentQuiz.timeLimit * 60); });
  });

  function renderQuestion(index) {
    currentIndex = index;
    const qData = currentQuiz.data[index];
    document.getElementById("current-q-num").textContent = index + 1;
    document.getElementById("question-text").innerHTML = qData.question;
    const imagesArea = document.getElementById("question-images-area");
    imagesArea.innerHTML = "";
    (currentQuiz.images || []).filter(img => img.qIndex === qData.originalIndex).forEach(img => imagesArea.innerHTML += `<img src="${img.dataUrl}" class="question-attached-img">`);
    const optionsArea = document.getElementById("options-area");
    optionsArea.innerHTML = "";
    for (const [key, value] of Object.entries(qData.options)) {
      const optDiv = document.createElement("div");
      optDiv.className = `option-box ${userAnswers[index] === key ? "selected" : ""}`;
      optDiv.innerHTML = `<span class="option-prefix">${key}.</span> <span>${value}</span>`;
      optDiv.addEventListener("click", function () {
        document.querySelectorAll(".option-box").forEach(el => el.classList.remove("selected"));
        this.classList.add("selected"); userAnswers[index] = key;
        document.querySelector(`.q-nav-box[data-idx="${index}"]`).classList.add("answered");
      });
      optionsArea.appendChild(optDiv);
    }
    document.querySelectorAll(".q-nav-box").forEach(el => el.classList.remove("active"));
    document.querySelector(`.q-nav-box[data-idx="${currentIndex}"]`).classList.add("active");
  }

  document.getElementById("btn-prev-question").addEventListener("click", () => { if (currentIndex > 0) renderQuestion(currentIndex - 1); });
  document.getElementById("btn-next-question").addEventListener("click", () => { if (currentIndex < currentQuiz.data.length - 1) renderQuestion(currentIndex + 1); });

  function renderQuestionGrid(total) {
    const grid = document.getElementById("question-navigation-grid");
    grid.innerHTML = "";
    for (let i = 0; i < total; i++) {
      const box = document.createElement("div"); box.className = "q-nav-box"; box.dataset.idx = i;
      box.innerHTML = `<span>${i + 1}</span>`;
      box.addEventListener("click", () => renderQuestion(i));
      grid.appendChild(box);
    }
  }

  async function submitQuiz() {
    clearInterval(timerInterval);
    const resultData = { studentId, quizId: currentQuiz.id, quizName: currentQuiz.name, timeSpent: Math.floor((Date.now() - startTime) / 1000), questions: currentQuiz.data, userAnswers, images: currentQuiz.images || [] };
    try { await fetch("/api/quizzes/submit", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(resultData) }); } catch (err) {}
    sessionStorage.setItem("current_quiz_result", JSON.stringify(resultData));
    window.location.href = "/quiz_result";
  }

  document.getElementById("btn-finish-quiz").addEventListener("click", e => { e.preventDefault(); if (confirm("Nộp bài ngay?")) submitQuiz(); });
});
```

## FILE: static/js/quiz_result.js

```javascript
document.addEventListener("DOMContentLoaded", function () {
  const resultDataString = sessionStorage.getItem("current_quiz_result");
  if (!resultDataString) { window.location.href = "/"; return; }
  const resultData = JSON.parse(resultDataString);
  let correctCount = 0;
  resultData.questions.forEach((q, i) => { if (resultData.userAnswers[i] === q.correctAnswer) correctCount++; });
  const totalScore = ((correctCount * 10) / resultData.questions.length).toFixed(2).replace(".", ",");
  let m = Math.floor(resultData.timeSpent / 60), s = resultData.timeSpent % 60;
  document.getElementById("res-quiz-name").textContent = resultData.quizName;
  document.getElementById("res-score").textContent = `${totalScore} / 10`;
  document.getElementById("res-time").textContent = `${m < 10 ? "0" + m : m} phút ${s < 10 ? "0" + s : s} giây`;
  
  const detailsList = document.getElementById("result-details-list");
  resultData.questions.forEach((q, index) => {
    let optionsHtml = '<div class="d-flex flex-column gap-3 mb-4">';
    for (const [key, value] of Object.entries(q.options)) {
      const isCorrect = q.correctAnswer === key, isUserChoice = resultData.userAnswers[index] === key;
      let boxClass = "result-option-box" + (isCorrect ? " correct-answer" : (isUserChoice ? " wrong-answer" : ""));
      optionsHtml += `<div class="${boxClass}"><span class="option-prefix">${key}.</span> <span>${value}</span></div>`;
    }
    optionsHtml += "</div>";
    detailsList.insertAdjacentHTML("beforeend", `
      <div class="card border-0 shadow-sm rounded-4 p-4 mb-3">
        <h5 class="fw-bold m-0 mb-3">Câu ${index + 1}</h5>
        <p class="fs-6 text-dark">${q.question}</p>
        ${optionsHtml}
      </div>`);
  });
});
```
