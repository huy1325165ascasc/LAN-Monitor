let stompClient = null;
let selectedClientId = null;
let currentSubscriptions = {};

// 1. Kết nối STOMP WebSocket
function connectWebSocket() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // Tắt bớt log debug STOMP trong console

  stompClient.connect(
    {},
    function () {
      const statusEl = document.getElementById("socket-status");
      statusEl.innerText = "WebSocket Đã Kết Nối";
      statusEl.className =
        "px-3 py-1 text-xs rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20";

      // Lắng nghe cập nhật danh sách máy con
      stompClient.subscribe("/topic/clients", function (message) {
        const clients = JSON.parse(message.body);
        renderClientList(clients);
      });

      // Lắng nghe cảnh báo gian lận (ĐÃ ĐƯỢC ĐƯA VÀO ĐÚNG VỊ TRÍ)
      stompClient.subscribe("/topic/alerts", function (message) {
        const alertData = JSON.parse(message.body);
        addSuspiciousAlert(alertData.pcName, alertData.tabName);

        const clientCard = document.querySelector(
          `.client-item[onclick*="${alertData.clientId}"]`,
        );
        if (clientCard) {
          clientCard.classList.remove("border-slate-700");
          clientCard.classList.add("border-rose-500", "animate-pulse");
          setTimeout(() => {
            clientCard.classList.remove("border-rose-500", "animate-pulse");
            clientCard.classList.add("border-slate-700");
          }, 5000);
        }
      });

      // Tải danh sách khởi đầu qua API
      fetchInitialClients();
    },
    function () {
      const statusEl = document.getElementById("socket-status");
      statusEl.innerText = "Mất kết nối WebSocket!";
      statusEl.className =
        "px-3 py-1 text-xs rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20";
      setTimeout(connectWebSocket, 4000); // Thử kết nối lại sau 4s
    },
  );
}

// 2. Tải danh sách Client qua REST API
async function fetchInitialClients() {
  try {
    const res = await fetch("/api/clients");
    const clients = await res.json();
    renderClientList(clients);
  } catch (e) {
    console.error("Lỗi khi tải danh sách client:", e);
  }
}

// 3. Render danh sách Client sang HTML
function renderClientList(clients) {
  const container = document.getElementById("client-grid");
  const countTag = document.getElementById("client-count");

  countTag.innerText = `${clients.length} online`;

  if (clients.length === 0) {
    container.innerHTML = `<div class="col-span-full text-center py-10 text-slate-500 text-sm">Chưa có máy nào kết nối...</div>`;
    selectedClientId = null;
    updateSelectedTag();
    return;
  }

  container.innerHTML = "";
  clients.forEach((c) => {
    const isActive = c.id === selectedClientId;

    const item = document.createElement("div");
    item.className = `client-item flex flex-col justify-between p-4 rounded-xl border ${isActive ? "border-indigo-500 bg-indigo-500/10" : "border-slate-700 bg-slate-900/60 hover:border-slate-500"} transition-all`;

    // ĐÃ XÓA TRẠNG THÁI, THỜI GIAN VÀ NÚT KHÓA BÀI
    item.innerHTML = `
            <div class="flex items-center justify-between mb-4 border-b border-slate-700/50 pb-3">
                <div class="flex items-center space-x-2 cursor-pointer" onclick="selectClient('${c.id}')">
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
                    <span class="font-bold text-sm text-slate-200">${c.pcName || "Máy Trạm"}</span>
                </div>
                <span class="text-xs font-mono text-slate-400">${c.ipAddress}</span>
            </div>
            
            <div class="mt-auto">
                <button onclick="openLiveStream('${c.id}', '${c.pcName || "Máy Trạm"}')" class="w-full bg-indigo-600/80 hover:bg-indigo-500 text-white text-sm py-2 rounded-md transition flex items-center justify-center gap-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>
                    Xem Live Stream
                </button>
            </div>
        `;
    container.appendChild(item);
  });

  if (!selectedClientId && clients.length > 0) {
    selectClient(clients[0].id);
  }
}

// --- THÊM HÀM NÀY VÀO CUỐI FILE ---
async function openImagesFolder() {
  try {
    const res = await fetch("/api/clients/open-images-folder", {
      method: "POST",
    });
    if (res.ok) {
      if (window.notify)
        window.notify("Đang mở thư mục ảnh trên máy chủ...", "success");
    } else {
      alert("Lỗi: Không thể tự động mở thư mục từ trình duyệt.");
    }
  } catch (e) {
    console.error("Lỗi khi gọi API mở thư mục:", e);
  }
}

// 4. Chọn một máy
function selectClient(clientId) {
  selectedClientId = clientId;
  updateSelectedTag();

  document.querySelectorAll(".client-item").forEach((el) => {
    if (el.innerHTML.includes(clientId)) {
      el.classList.add("border-indigo-500", "bg-indigo-500/10");
      el.classList.remove("border-slate-700", "bg-slate-900/60");
    } else {
      el.classList.remove("border-indigo-500", "bg-indigo-500/10");
      el.classList.add("border-slate-700", "bg-slate-900/60");
    }
  });
}

function updateSelectedTag() {
  const tag = document.getElementById("selected-client-tag");
  tag.innerText = selectedClientId
    ? `Đang điều khiển: ${selectedClientId}`
    : "Chưa chọn máy trạm";
}

// Hàm gửi cảnh báo khả nghi
function addSuspiciousAlert(machineName, tabName) {
  const feedContainer = document.getElementById("alert-feed");
  const now = new Date();
  const timeString = now.toLocaleTimeString("vi-VN", { hour12: false });

  const alertBox = document.createElement("div");
  alertBox.className =
    "bg-slate-900 border-l-4 border-rose-500 rounded-r-lg p-3 shadow-md animate-[pulse_0.5s_ease-in-out]";

  alertBox.innerHTML = `
        <div class="flex justify-between items-start mb-1">
            <span class="text-[11px] font-mono text-slate-400">${timeString}</span>
            <button class="text-slate-500 hover:text-indigo-400 transition-colors">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
            </button>
        </div>
        <div>
            <span class="font-bold text-sm text-slate-200">${machineName}</span>
            <p class="text-xs text-rose-400 mt-0.5 line-clamp-2">Đang mở: ${tabName}</p>
        </div>
    `;
  feedContainer.prepend(alertBox);
}

// ================= HỆ THỐNG LIVESTREAM 4 FPS =================
let liveStreamInterval = null;
let activeLiveStreamClient = null;

function openLiveStream(clientId, pcName) {
  activeLiveStreamClient = clientId;
  document.getElementById("livestream-title").innerText = pcName;

  const modal = document.getElementById("livestream-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");

  const topic = `/topic/screenshot/${clientId}`;
  if (!currentSubscriptions[topic] && stompClient) {
    currentSubscriptions[topic] = stompClient.subscribe(topic, function (msg) {
      if (activeLiveStreamClient === clientId) {
        document.getElementById("livestream-img").src =
          "data:image/jpeg;base64," + msg.body;
      }
    });
  }

  if (liveStreamInterval) clearInterval(liveStreamInterval);
  liveStreamInterval = setInterval(() => {
    fetch(`/api/clients/${encodeURIComponent(clientId)}/capture`, {
      method: "POST",
    }).catch((e) => console.log("Lỗi Livestream: ", e));
  }, 250);
}

function closeLiveStream() {
  if (liveStreamInterval) {
    clearInterval(liveStreamInterval);
    liveStreamInterval = null;
  }
  activeLiveStreamClient = null;
  const modal = document.getElementById("livestream-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
  document.getElementById("livestream-img").src = "";
}

// Hàm yêu cầu Server lưu lại 1 khung hình đang Livestream
async function captureLivestreamFrame() {
  if (!activeLiveStreamClient) return;
  try {
    const res = await fetch(
      `/api/clients/${encodeURIComponent(activeLiveStreamClient)}/save-frame`,
      {
        method: "POST",
      },
    );
    if (res.ok) {
      // Hiển thị thông báo nhỏ trên màn hình
      alert(
        "📸 Đã chụp và lưu thành công tấm ảnh minh chứng vào thư mục 'images' trên Server!",
      );
    } else {
      alert("Lỗi: Không thể chụp ảnh lúc này.");
    }
  } catch (e) {
    console.error("Lỗi khi gọi API chụp ảnh:", e);
  }
}

// ================= HỆ THỐNG CẤU HÌNH WHITELIST =================
async function openWhitelistModal() {
  document.getElementById("whitelist-modal").classList.remove("hidden");
  document.getElementById("whitelist-modal").classList.add("flex");

  // Tải cấu hình hiện tại từ Server
  try {
    const res = await fetch("/api/settings/whitelist");
    const data = await res.json();
    document.getElementById("whitelist-input").value = data.join(", ");
  } catch (e) {
    console.error("Lỗi tải Whitelist:", e);
  }
}

function closeWhitelistModal() {
  document.getElementById("whitelist-modal").classList.add("hidden");
  document.getElementById("whitelist-modal").classList.remove("flex");
}

async function saveWhitelist() {
  const inputStr = document.getElementById("whitelist-input").value;
  // Cắt chuỗi bằng dấu phẩy, loại bỏ khoảng trắng thừa
  const whitelistArray = inputStr
    .split(",")
    .map((item) => item.trim())
    .filter((item) => item.length > 0);

  if (whitelistArray.length === 0) {
    alert("Vui lòng nhập ít nhất một từ khóa hợp lệ!");
    return;
  }

  try {
    const res = await fetch("/api/settings/whitelist", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ whitelist: whitelistArray }),
    });

    if (res.ok) {
      alert(
        "Đã lưu cấu hình và cập nhật luật mới xuống tất cả máy tính đang thi!",
      );
      closeWhitelistModal();
    }
  } catch (e) {
    alert("Lỗi khi lưu cấu hình: " + e.message);
  }
}

// Khởi chạy khi load trang
document.addEventListener("DOMContentLoaded", connectWebSocket);
