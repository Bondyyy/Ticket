(function () {
    'use strict';

    const STORAGE_KEYS = {
        pending: 'dede_staff_app_pending_scans',
        selectedEvent: 'dede_staff_app_selected_event',
        gate: 'dede_staff_app_gate'
    };

    const state = {
        online: navigator.onLine,
        selectedEvent: '',
        gate: 'Cổng chính',
        deferredInstallPrompt: null,
        cameraStream: null,
        scannerTimer: null,
        facingMode: 'environment',
        scanLocked: false,
        validationInFlight: false,
        lastDecodedText: '',
        lastDecodedAt: 0,
        barcodeDetector: null,
        serverHistory: []
    };

    document.addEventListener('DOMContentLoaded', initStaffApp);

    function initStaffApp() {
        bindTabs();
        bindEventSelection();
        bindGateSelection();
        bindCameraControls();
        bindManualControls();
        bindInstallControls();
        bindNetworkEvents();
        restorePreferences();
        setupBarcodeDetector();
        updateConnectionUi();
        updatePendingUi();
        updateControlState();
        loadHistory();
        registerServiceWorker();

        if (state.online) {
            syncPendingScans();
        }
    }

    function bindTabs() {
        document.querySelectorAll('[data-tab]').forEach(button => {
            button.addEventListener('click', () => activateTab(button.dataset.tab));
        });
    }

    function activateTab(tabName) {
        document.querySelectorAll('[data-tab]').forEach(button => {
            button.classList.toggle('is-active', button.dataset.tab === tabName);
        });
        document.querySelectorAll('[data-tab-panel]').forEach(panel => {
            panel.classList.toggle('is-active', panel.dataset.tabPanel === tabName);
        });
        if (tabName === 'history') {
            loadHistory();
        }
    }

    function bindEventSelection() {
        const select = byId('eventSelect');
        select?.addEventListener('change', () => {
            state.selectedEvent = select.value.trim();
            localStorage.setItem(STORAGE_KEYS.selectedEvent, state.selectedEvent);
            renderEventSummary();
            updateControlState();
        });

        byId('refreshEventsBtn')?.addEventListener('click', refreshEvents);
    }

    function bindGateSelection() {
        document.querySelectorAll('.gate-chip').forEach(button => {
            button.addEventListener('click', () => {
                setGate(button.dataset.gate || 'Cổng chính');
                byId('customGateInput').value = '';
            });
        });
        byId('customGateInput')?.addEventListener('input', event => {
            const value = event.target.value.trim();
            if (value) {
                setGate(value, false);
            }
        });
    }

    function bindCameraControls() {
        byId('startCameraBtn')?.addEventListener('click', startCamera);
        byId('stopCameraBtn')?.addEventListener('click', stopCamera);
        byId('switchCameraBtn')?.addEventListener('click', switchCamera);
    }

    function bindManualControls() {
        byId('manualSubmitBtn')?.addEventListener('click', () => {
            const input = byId('manualCodeInput');
            const code = input?.value.trim();
            if (!code) {
                showClientWarning('CHƯA CÓ MÃ', 'Vui lòng nhập mã an toàn.');
                return;
            }
            handleDecodedCode(code);
        });

        byId('pasteCodeBtn')?.addEventListener('click', async () => {
            try {
                const value = await navigator.clipboard.readText();
                byId('manualCodeInput').value = value || '';
                if (value) {
                    handleDecodedCode(value);
                }
            } catch (error) {
                showClientWarning('KHÔNG THỂ DÁN MÃ', 'Trình duyệt chưa cấp quyền đọc clipboard. Hãy nhập mã thủ công.');
            }
        });

        byId('clearCodeBtn')?.addEventListener('click', () => {
            byId('manualCodeInput').value = '';
            byId('manualCodeInput').focus();
        });
    }

    function bindInstallControls() {
        window.addEventListener('beforeinstallprompt', event => {
            event.preventDefault();
            state.deferredInstallPrompt = event;
            updateInstallUi();
        });

        byId('installAppBtn')?.addEventListener('click', async () => {
            if (!state.deferredInstallPrompt) {
                return;
            }
            state.deferredInstallPrompt.prompt();
            await state.deferredInstallPrompt.userChoice.catch(() => null);
            state.deferredInstallPrompt = null;
            updateInstallUi();
        });

        updateInstallUi();
    }

    function bindNetworkEvents() {
        window.addEventListener('online', () => {
            state.online = true;
            updateConnectionUi();
            syncPendingScans();
            loadHistory();
        });

        window.addEventListener('offline', () => {
            state.online = false;
            updateConnectionUi();
        });

        byId('syncNowBtn')?.addEventListener('click', syncPendingScans);
    }

    function restorePreferences() {
        const savedEvent = localStorage.getItem(STORAGE_KEYS.selectedEvent) || '';
        const savedGate = localStorage.getItem(STORAGE_KEYS.gate) || 'Cổng chính';
        const select = byId('eventSelect');
        if (select && savedEvent && select.querySelector(`option[value="${cssEscape(savedEvent)}"]`)) {
            select.value = savedEvent;
            state.selectedEvent = savedEvent;
        } else if (select) {
            state.selectedEvent = select.value.trim();
        }
        setGate(savedGate);
        renderEventSummary();
    }

    function setGate(value, updatePreset = true) {
        state.gate = value || 'Cổng chính';
        localStorage.setItem(STORAGE_KEYS.gate, state.gate);
        if (updatePreset) {
            document.querySelectorAll('.gate-chip').forEach(button => {
                button.classList.toggle('is-active', button.dataset.gate === state.gate);
            });
        } else {
            document.querySelectorAll('.gate-chip').forEach(button => button.classList.remove('is-active'));
        }
    }

    function setupBarcodeDetector() {
        const badge = byId('cameraSupportBadge');
        if ('BarcodeDetector' in window) {
            try {
                state.barcodeDetector = new BarcodeDetector({ formats: ['qr_code'] });
                if (badge) {
                    badge.textContent = 'QR camera';
                    badge.classList.add('is-ready');
                }
                return;
            } catch (error) {
                state.barcodeDetector = null;
            }
        }
        if (badge) {
            badge.textContent = 'Nhập mã thủ công';
            badge.classList.add('is-warning');
        }
        byId('cameraHint').textContent = 'Trình duyệt này chưa hỗ trợ BarcodeDetector. Vui lòng nhập mã thủ công hoặc dùng trình duyệt Chrome/Edge mới.';
    }

    async function startCamera() {
        if (!ensureCanScan()) {
            return;
        }
        if (!state.barcodeDetector) {
            showClientWarning('CAMERA KHÔNG KHẢ DỤNG', 'Trình duyệt chưa hỗ trợ đọc QR bằng camera. Hãy dùng nhập mã thủ công.');
            return;
        }
        if (!navigator.mediaDevices?.getUserMedia) {
            showClientWarning('CAMERA KHÔNG KHẢ DỤNG', 'Trình duyệt không cho phép mở camera trên kết nối hiện tại.');
            return;
        }

        try {
            await stopCamera(false);
            state.cameraStream = await navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: { ideal: state.facingMode },
                    width: { ideal: 1280 },
                    height: { ideal: 720 }
                },
                audio: false
            });

            const video = byId('cameraVideo');
            video.srcObject = state.cameraStream;
            await video.play();
            byId('cameraPlaceholder').hidden = true;
            document.querySelector('.camera-frame')?.classList.add('is-running');
            byId('startCameraBtn').disabled = true;
            byId('stopCameraBtn').disabled = false;
            scanFrameLoop();
        } catch (error) {
            showClientWarning('KHÔNG MỞ ĐƯỢC CAMERA', cameraErrorMessage(error));
            await stopCamera(false);
        }
    }

    async function stopCamera(showIdle = true) {
        if (state.scannerTimer) {
            window.clearTimeout(state.scannerTimer);
            state.scannerTimer = null;
        }
        if (state.cameraStream) {
            state.cameraStream.getTracks().forEach(track => track.stop());
            state.cameraStream = null;
        }
        const video = byId('cameraVideo');
        if (video) {
            video.pause();
            video.srcObject = null;
        }
        byId('cameraPlaceholder').hidden = false;
        document.querySelector('.camera-frame')?.classList.remove('is-running');
        byId('startCameraBtn').disabled = !hasSelectedEvent() || !isStaffReady();
        byId('stopCameraBtn').disabled = true;
        if (showIdle) {
            byId('cameraHint').textContent = 'Camera đã dừng. Bạn có thể bật lại hoặc nhập mã thủ công.';
        }
    }

    async function switchCamera() {
        state.facingMode = state.facingMode === 'environment' ? 'user' : 'environment';
        if (state.cameraStream) {
            await startCamera();
        }
    }

    async function scanFrameLoop() {
        if (!state.cameraStream || !state.barcodeDetector) {
            return;
        }
        const video = byId('cameraVideo');
        const canvas = byId('scanCanvas');
        if (video.readyState >= HTMLMediaElement.HAVE_CURRENT_DATA && !state.scanLocked) {
            try {
                canvas.width = video.videoWidth || 640;
                canvas.height = video.videoHeight || 480;
                canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
                const codes = await state.barcodeDetector.detect(canvas);
                const rawValue = codes && codes[0] && codes[0].rawValue;
                if (rawValue) {
                    handleDecodedCode(rawValue);
                }
            } catch (error) {
                console.warn('Không đọc được khung hình QR:', error);
            }
        }
        state.scannerTimer = window.setTimeout(scanFrameLoop, 260);
    }

    function handleDecodedCode(rawCode) {
        const code = String(rawCode || '').trim();
        if (!code) {
            return;
        }
        if (!ensureCanScan()) {
            return;
        }

        const now = Date.now();
        if (code === state.lastDecodedText && now - state.lastDecodedAt < 3000) {
            return;
        }
        state.lastDecodedText = code;
        state.lastDecodedAt = now;
        byId('manualCodeInput').value = code;
        state.scanLocked = true;
        validateOrQueue(code).finally(() => {
            window.setTimeout(() => {
                state.scanLocked = false;
            }, 1700);
        });
    }

    async function validateOrQueue(code) {
        if (!state.selectedEvent) {
            showClientWarning('CHƯA CHỌN SỰ KIỆN', 'Vui lòng chọn sự kiện trước khi soát vé.');
            return;
        }
        if (state.validationInFlight) {
            return;
        }

        if (!navigator.onLine || !state.online) {
            queueOfflineScan(code);
            return;
        }

        state.validationInFlight = true;
        setLoadingState(true);
        const startedAt = Date.now();

        try {
            const body = new URLSearchParams();
            body.append('qrPayloadOrCode', code);
            body.append('maSK', state.selectedEvent);
            body.append('congSoat', state.gate || 'Cổng chính');

            const response = await fetch('/api/soat-ve/validate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body
            });
            const data = await response.json().catch(() => ({}));

            if (response.status === 401 || response.status === 403) {
                handleAuthFailure(data);
                return;
            }
            if (!response.ok) {
                throw new Error(data.message || 'Không thể soát vé lúc này.');
            }

            showValidationResult(data, Date.now() - startedAt);
            vibrateForResult(data.status);
            await loadHistory();
        } catch (error) {
            if (!navigator.onLine || error instanceof TypeError) {
                state.online = false;
                updateConnectionUi();
                queueOfflineScan(code);
            } else {
                showClientWarning('KHÔNG THỂ KIỂM TRA', error.message || 'Hệ thống chưa trả về kết quả hợp lệ.');
                vibrateForResult('Vé không tìm thấy');
            }
        } finally {
            state.validationInFlight = false;
            setLoadingState(false);
        }
    }

    function queueOfflineScan(code) {
        const pending = getPendingScans();
        const clientScanId = 'scan-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8);
        pending.push({
            clientScanId,
            deviceId: getDeviceId(),
            qrPayloadOrCode: code,
            maSK: state.selectedEvent,
            congSoat: state.gate || 'Cổng chính',
            thoiGianQuet: String(Date.now())
        });
        setPendingScans(pending);
        updatePendingUi();
        showOfflineQueuedResult(code);
        renderHistory();
    }

    async function syncPendingScans() {
        const pending = getPendingScans();
        if (pending.length === 0) {
            updatePendingUi();
            return;
        }
        if (!navigator.onLine || !state.online) {
            showClientWarning('CHƯA CÓ MẠNG', 'Các lượt quét offline vẫn đang chờ đồng bộ.');
            return;
        }

        const button = byId('syncNowBtn');
        button?.classList.add('is-loading');

        try {
            const response = await fetch('/api/soat-ve/sync', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify(pending)
            });
            const data = await response.json().catch(() => ({}));
            if (response.status === 401 || response.status === 403) {
                handleAuthFailure(data);
                return;
            }
            if (!response.ok || data.success === false) {
                throw new Error(data.message || 'Đồng bộ offline thất bại.');
            }

            localStorage.removeItem(STORAGE_KEYS.pending);
            updatePendingUi();
            showSyncSummary(data);
            await loadHistory();
        } catch (error) {
            console.error('Không thể đồng bộ pending scans:', error);
            showClientWarning('CHƯA ĐỒNG BỘ ĐƯỢC', 'Lượt quét offline vẫn được giữ trên thiết bị để thử lại.');
        } finally {
            button?.classList.remove('is-loading');
        }
    }

    async function loadHistory() {
        const pending = getPendingScans();
        if (!navigator.onLine || !state.online) {
            renderHistory();
            return;
        }
        try {
            const response = await fetch('/api/soat-ve/history', {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const data = await response.json().catch(() => []);
            if (response.status === 401 || response.status === 403) {
                handleAuthFailure(data);
                return;
            }
            if (response.ok && Array.isArray(data)) {
                state.serverHistory = data;
            }
        } catch (error) {
            console.warn('Không tải được lịch sử server:', error);
        } finally {
            if (pending.length > 0) {
                updatePendingUi();
            }
            renderHistory();
        }
    }

    async function refreshEvents() {
        const select = byId('eventSelect');
        if (!select) {
            return;
        }
        try {
            const response = await fetch('/api/staff-app/events', {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const data = await response.json().catch(() => ({}));
            if (response.status === 401 || response.status === 403) {
                handleAuthFailure(data);
                return;
            }
            if (!response.ok || data.success === false || !Array.isArray(data.events)) {
                throw new Error(data.message || 'Không tải được danh sách sự kiện.');
            }

            const current = state.selectedEvent;
            select.innerHTML = '<option value="">Chọn sự kiện</option>' + data.events.map(event => {
                return `<option value="${escapeAttr(event.maSK)}"
                    data-name="${escapeAttr(event.tenSK)}"
                    data-status="${escapeAttr(event.trangThaiSK)}"
                    data-start="${escapeAttr(event.thoiGianBatDau)}"
                    data-end="${escapeAttr(event.thoiGianKetThuc)}"
                    data-venue="${escapeAttr(event.tenDiaDiem)}"
                    data-address="${escapeAttr(event.diaChi)}"
                    data-city="${escapeAttr(event.thanhPho)}">${escapeHtml(event.maSK)} - ${escapeHtml(event.tenSK)}</option>`;
            }).join('');
            if (current && select.querySelector(`option[value="${cssEscape(current)}"]`)) {
                select.value = current;
            }
            state.selectedEvent = select.value.trim();
            renderEventSummary();
            updateControlState();
        } catch (error) {
            showClientWarning('KHÔNG TẢI ĐƯỢC SỰ KIỆN', error.message || 'Vui lòng thử lại.');
        }
    }

    function renderEventSummary() {
        const summary = byId('eventSummary');
        const option = byId('eventSelect')?.selectedOptions?.[0];
        if (!summary || !option || !option.value) {
            summary.className = 'event-summary is-empty';
            summary.innerHTML = '<strong>Chưa chọn sự kiện</strong><span>Vui lòng chọn sự kiện trước khi bật camera hoặc nhập mã.</span>';
            return;
        }

        const name = option.dataset.name || option.textContent || option.value;
        const status = option.dataset.status || 'Chưa rõ';
        const venue = [option.dataset.venue, option.dataset.city].filter(Boolean).join(', ');
        const time = formatRange(option.dataset.start, option.dataset.end);
        summary.className = 'event-summary';
        summary.innerHTML = `
            <strong>${escapeHtml(name)}</strong>
            <span>${escapeHtml(time)}</span>
            <span>${escapeHtml(venue || 'Chưa có địa điểm')}</span>
            <em>${escapeHtml(status)}</em>`;
    }

    function renderHistory() {
        const list = byId('historyList');
        if (!list) {
            return;
        }
        const pendingRows = getPendingScans().map(item => ({
            time: Number(item.thoiGianQuet) || Date.now(),
            maVe: extractShortCode(item.qrPayloadOrCode),
            status: 'Chờ đồng bộ',
            gate: item.congSoat || 'Cổng chính',
            source: 'Chờ đồng bộ'
        }));
        const serverRows = state.serverHistory.map(item => ({
            time: item.thoiGianQuet,
            maVe: item.maVe || 'N/A',
            status: item.ketQuaQuet || 'Vé không tìm thấy',
            gate: item.congSoat || 'Cổng chính',
            source: item.nguonDuLieu || 'Online'
        }));
        const rows = pendingRows.concat(serverRows).slice(0, 10);

        if (rows.length === 0) {
            list.innerHTML = '<div class="empty-state">Chưa có lịch sử quét.</div>';
            return;
        }

        list.innerHTML = rows.map(row => `
            <div class="history-item">
                <div>
                    <strong>${escapeHtml(row.maVe)}</strong>
                    <span>${escapeHtml(row.gate)} · ${escapeHtml(formatDateTime(row.time))}</span>
                </div>
                <div class="history-badges">
                    <span class="scan-status ${statusClass(row.status)}">${escapeHtml(row.status)}</span>
                    <span class="source-status">${escapeHtml(row.source)}</span>
                </div>
            </div>
        `).join('');
    }

    function showValidationResult(data, fallbackDurationMs) {
        const normalized = normalizeValidationResult(data, fallbackDurationMs);
        const card = byId('resultCard');
        card.className = 'result-card ' + resultClass(normalized.status);
        byId('resultSymbol').textContent = resultSymbol(normalized.status);
        byId('resultTitle').textContent = resultTitle(normalized.status);
        byId('resultMessage').textContent = normalized.message;
        fillMeta(normalized);
    }

    function normalizeValidationResult(data, fallbackDurationMs) {
        const status = data?.status || 'Vé không tìm thấy';
        return {
            success: Boolean(data?.success),
            status,
            message: messageForStatus(status, data?.message),
            maVe: data?.maVe || '-',
            seatName: data?.seatName || '-',
            zoneName: data?.zoneName || '-',
            ticketOwner: data?.ticketOwner || '-',
            scanTime: data?.scanTime || data?.thoiGianQuet || Date.now(),
            durationMs: Number.isFinite(data?.durationMs) ? data.durationMs : fallbackDurationMs,
            nguonDuLieu: data?.nguonDuLieu || 'Online'
        };
    }

    function showOfflineQueuedResult(code) {
        const card = byId('resultCard');
        card.className = 'result-card result-offline';
        byId('resultSymbol').textContent = '↥';
        byId('resultTitle').textContent = 'ĐÃ LƯU OFFLINE - CHỜ ĐỒNG BỘ';
        byId('resultMessage').textContent = 'Lượt quét đã lưu trên thiết bị. Kết quả thật sẽ có sau khi đồng bộ server.';
        fillMeta({
            maVe: extractShortCode(code),
            seatName: '-',
            zoneName: 'Chờ đồng bộ',
            ticketOwner: '-',
            scanTime: Date.now(),
            durationMs: 0
        });
        vibrate([80, 40, 80]);
    }

    function showClientWarning(title, message) {
        const card = byId('resultCard');
        card.className = title === 'CHƯA CHỌN SỰ KIỆN' ? 'result-card result-warning' : 'result-card result-danger';
        byId('resultSymbol').textContent = title === 'CHƯA CHỌN SỰ KIỆN' ? '!' : '×';
        byId('resultTitle').textContent = title;
        byId('resultMessage').textContent = message;
        byId('resultMeta').hidden = true;
        vibrate([160]);
    }

    function showSyncSummary(data) {
        const count = data.syncedCount || data.results?.length || 0;
        const successCount = data.successCount || 0;
        const card = byId('resultCard');
        card.className = 'result-card result-offline';
        byId('resultSymbol').textContent = '↥';
        byId('resultTitle').textContent = 'ĐÃ ĐỒNG BỘ OFFLINE';
        byId('resultMessage').textContent = `Server đã xử lý ${count} lượt quét, trong đó ${successCount} lượt hợp lệ.`;
        byId('resultMeta').hidden = true;
    }

    function fillMeta(data) {
        byId('metaTicket').textContent = data.maVe || '-';
        byId('metaSeat').textContent = data.seatName || '-';
        byId('metaZone').textContent = data.zoneName || '-';
        byId('metaOwner').textContent = data.ticketOwner || '-';
        byId('metaTime').textContent = formatDateTime(data.scanTime);
        byId('metaDuration').textContent = `${data.durationMs || 0} ms`;
        byId('resultMeta').hidden = false;
    }

    function ensureCanScan() {
        if (!isStaffReady()) {
            showClientWarning('KHÔNG THỂ SOÁT VÉ', 'Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên, vui lòng liên hệ quản trị viên.');
            return false;
        }
        if (!hasSelectedEvent()) {
            showClientWarning('CHƯA CHỌN SỰ KIỆN', 'Vui lòng chọn sự kiện trước khi soát vé.');
            return false;
        }
        return true;
    }

    function updateControlState() {
        const ready = isStaffReady();
        const hasEvent = hasSelectedEvent();
        byId('eventRequiredNotice').hidden = hasEvent;
        byId('startCameraBtn').disabled = !ready || !hasEvent || Boolean(state.cameraStream);
        byId('manualCodeInput').disabled = !ready || !hasEvent;
        byId('manualSubmitBtn').disabled = !ready || !hasEvent || state.validationInFlight;
        byId('pasteCodeBtn').disabled = !ready || !hasEvent;
        byId('clearCodeBtn').disabled = !ready || !hasEvent;
        if (!hasEvent && state.cameraStream) {
            stopCamera(false);
        }
    }

    function updateConnectionUi() {
        state.online = navigator.onLine;
        const badge = byId('connectionBadge');
        const text = byId('connectionText');
        badge?.classList.toggle('is-online', state.online);
        badge?.classList.toggle('is-offline', !state.online);
        if (text) {
            text.textContent = state.online ? 'Online' : 'Offline';
        }
        byId('offlineBanner').hidden = state.online;
    }

    function updatePendingUi() {
        const pending = getPendingScans();
        const count = pending.length;
        byId('pendingCount').textContent = String(count);
        byId('syncNowBtn').disabled = count === 0;
        const navDot = byId('navPendingDot');
        if (navDot) {
            navDot.hidden = count === 0;
            navDot.textContent = String(count);
        }
    }

    function updateInstallUi() {
        const standalone = window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone;
        const button = byId('installAppBtn');
        if (!button) {
            return;
        }
        button.hidden = standalone || !state.deferredInstallPrompt;
        if (standalone) {
            byId('installInstructions').classList.add('is-installed');
            byId('installInstructions').innerHTML = '<strong>Đã chạy như app</strong><span>Dề Dê Check-in đang mở ở chế độ standalone.</span>';
        }
    }

    function setLoadingState(isLoading) {
        byId('manualSubmitBtn').classList.toggle('is-loading', isLoading);
        byId('manualSubmitBtn').textContent = isLoading ? 'Đang soát vé' : 'Soát vé';
        updateControlState();
    }

    function handleAuthFailure(data) {
        const overlay = byId('sessionOverlay');
        if (overlay) {
            overlay.hidden = false;
        }
        if (data?.message) {
            showClientWarning('PHIÊN KHÔNG HỢP LỆ', data.message);
        }
    }

    function getPendingScans() {
        try {
            const parsed = JSON.parse(localStorage.getItem(STORAGE_KEYS.pending) || '[]');
            return Array.isArray(parsed) ? parsed : [];
        } catch (error) {
            return [];
        }
    }

    function setPendingScans(items) {
        const deduped = [];
        const seen = new Set();
        items.forEach(item => {
            const key = item.clientScanId || `${item.qrPayloadOrCode}|${item.maSK}|${item.thoiGianQuet}`;
            if (!seen.has(key)) {
                seen.add(key);
                deduped.push(item);
            }
        });
        localStorage.setItem(STORAGE_KEYS.pending, JSON.stringify(deduped));
    }

    function getDeviceId() {
        const key = 'dede_staff_app_device_id';
        let id = localStorage.getItem(key);
        if (!id) {
            id = 'staff-device-' + Math.random().toString(36).slice(2) + Date.now().toString(36);
            localStorage.setItem(key, id);
        }
        return id;
    }

    function isStaffReady() {
        return document.querySelector('.app-shell')?.dataset.staffReady === 'true';
    }

    function hasSelectedEvent() {
        const select = byId('eventSelect');
        state.selectedEvent = select?.value?.trim() || '';
        return Boolean(state.selectedEvent);
    }

    function registerServiceWorker() {
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.register('/service-worker.js').catch(error => {
                console.warn('Không đăng ký được service worker:', error);
            });
        }
    }

    function resultClass(status) {
        if (status === 'Hợp lệ') return 'result-success';
        if (status === 'Vé đã sử dụng') return 'result-used';
        if (status === 'Sai sự kiện') return 'result-wrong-event';
        return 'result-danger';
    }

    function resultSymbol(status) {
        if (status === 'Hợp lệ') return '✓';
        if (status === 'Vé đã sử dụng') return '!';
        if (status === 'Sai sự kiện') return '!';
        return '×';
    }

    function resultTitle(status) {
        if (status === 'Hợp lệ') return 'HỢP LỆ - CHO VÀO';
        if (status === 'Vé đã sử dụng') return 'VÉ ĐÃ SỬ DỤNG';
        if (status === 'Sai sự kiện') return 'SAI SỰ KIỆN';
        return 'KHÔNG HỢP LỆ';
    }

    function messageForStatus(status, apiMessage) {
        if (apiMessage) return apiMessage;
        if (status === 'Hợp lệ') return 'Vé hợp lệ. Hệ thống đã chuyển vé sang trạng thái Đã sử dụng.';
        if (status === 'Vé đã sử dụng') return 'Vé này đã được soát trước đó.';
        if (status === 'Sai sự kiện') return 'Vé tồn tại nhưng không thuộc sự kiện đang chọn.';
        if (status === 'Vé giả') return 'Vé không được phép sử dụng tại cổng.';
        return 'Mã an toàn không tồn tại trên hệ thống.';
    }

    function statusClass(status) {
        if (status === 'Hợp lệ') return 'is-valid';
        if (status === 'Chờ đồng bộ') return 'is-pending';
        if (status === 'Vé đã sử dụng') return 'is-used';
        return 'is-invalid';
    }

    function cameraErrorMessage(error) {
        if (error?.name === 'NotAllowedError') return 'Bạn chưa cấp quyền camera. Hãy cho phép camera trong trình duyệt.';
        if (error?.name === 'NotFoundError') return 'Không tìm thấy camera trên thiết bị.';
        if (error?.name === 'NotReadableError') return 'Camera đang được ứng dụng khác sử dụng.';
        if (error?.name === 'SecurityError') return 'Trình duyệt chặn camera vì trang không chạy trên HTTPS hoặc localhost.';
        return 'Không thể mở camera. Hãy thử lại hoặc nhập mã thủ công.';
    }

    function formatRange(start, end) {
        const startText = formatDateTime(start);
        const endText = formatDateTime(end);
        if (startText === '-' && endText === '-') return 'Chưa có thời gian';
        if (endText === '-') return startText;
        return `${startText} - ${endText}`;
    }

    function formatDateTime(value) {
        if (!value) return '-';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '-';
        return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }

    function extractShortCode(value) {
        const text = String(value || '');
        if (text.startsWith('DDE_TICKET|')) {
            const part = text.split('|').find(item => item.toLowerCase().startsWith('code='));
            if (part) {
                return part.slice(5);
            }
        }
        if (text.startsWith('TICKET|')) {
            const part = text.split('|').find(item => {
                const key = item.split('=')[0]?.trim().toLowerCase();
                return key === 'code' || key === 'maqr';
            });
            if (part) {
                return part.split('=').slice(1).join('=');
            }
        }
        return text.length > 22 ? text.slice(0, 22) + '...' : text || 'N/A';
    }

    function vibrateForResult(status) {
        if (status === 'Hợp lệ') {
            vibrate(80);
        } else {
            vibrate([160, 50, 160]);
        }
    }

    function vibrate(pattern) {
        if (navigator.vibrate) {
            navigator.vibrate(pattern);
        }
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    function escapeAttr(value) {
        return escapeHtml(value).replaceAll('`', '&#096;');
    }

    function cssEscape(value) {
        if (window.CSS?.escape) {
            return CSS.escape(value);
        }
        return String(value).replaceAll('"', '\\"');
    }

    function byId(id) {
        return document.getElementById(id);
    }
})();
