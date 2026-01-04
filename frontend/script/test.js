// API 기본 경로
const API_BASE = '/api';
// NAMESPACE는 각 HTML 파일에서 이 스크립트 로드 전에 정의됨 (chat 또는 work)

// DOM 요소 참조
const uploadZone = document.getElementById('upload-zone');                   // 파일 업로드 영역
const fileInput = document.getElementById('file-input');                     // 파일 입력 요소
const resultsContainer = document.getElementById('results-container');       // 결과 표시 컨테이너
const blockedExtensionsEl = document.getElementById('blocked-extensions');   // 차단된 확장자 목록

/**
 * 페이지 로드 시 초기화
 * 차단된 확장자 목록을 로드하고 3초마다 자동 갱신
 */
document.addEventListener('DOMContentLoaded', () => {
    loadBlockedExtensions();

    // 3초마다 자동으로 차단 목록 갱신
    setInterval(loadBlockedExtensions, 3000);
});

/**
 * 서버에서 현재 차단된 확장자 목록을 불러와서 화면에 표시
 */
async function loadBlockedExtensions() {
    try {
        const res = await fetch(`${API_BASE}/policies/${NAMESPACE}`);
        if (!res.ok) throw new Error('Failed to load policy');
        const data = await res.json();

        // 고정 확장자와 커스텀 확장자 모두에서 차단된 것들 추출
        const blockedFixed = data.fixed.filter(f => f.checked).map(f => f.name);
        const blockedCustom = data.custom.filter(c => c.active).map(c => c.name);
        const blockedExts = [...blockedFixed, ...blockedCustom];

        if (blockedExts.length === 0) {
            blockedExtensionsEl.innerHTML = '<p style="color: #666;">차단된 확장자가 없습니다</p>';
        } else {
            blockedExtensionsEl.innerHTML = blockedExts
                .map(ext => `<span class="ext-tag">.${ext}</span>`)
                .join('');
        }
    } catch (err) {
        console.error(err);
        blockedExtensionsEl.innerHTML = '<p style="color: #dc3545;">정책을 불러오는데 실패했습니다</p>';
    }
}

// 파일 입력 처리
fileInput.addEventListener('change', (e) => {
    handleFiles(e.target.files);
});

// 업로드 영역 클릭 시 파일 선택기 열기
uploadZone.addEventListener('click', () => {
    fileInput.click();
});

// 드래그 앤 드롭 이벤트
uploadZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadZone.classList.add('dragover');  // 드래그 오버 시 스타일 추가
});

uploadZone.addEventListener('dragleave', () => {
    uploadZone.classList.remove('dragover');  // 드래그 아웃 시 스타일 제거
});

uploadZone.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadZone.classList.remove('dragover');
    handleFiles(e.dataTransfer.files);  // 드롭된 파일 처리
});

/**
 * 선택된 파일들을 처리하여 검증
 * @param {FileList} files - 검증할 파일 목록
 */
async function handleFiles(files) {
    if (files.length === 0) return;

    resultsContainer.innerHTML = '';  // 이전 결과 초기화

    for (const file of files) {
        await validateFile(file);  // 각 파일을 순차적으로 검증
    }
}

/**
 * 개별 파일을 서버에 요청하여 허용/차단 여부 확인
 * @param {File} file - 검증할 파일
 */
async function validateFile(file) {
    // 결과 표시 요소 생성
    const resultDiv = document.createElement('div');
    resultDiv.className = 'result-item';

    const filenameDiv = document.createElement('div');
    filenameDiv.className = 'result-filename';
    filenameDiv.textContent = file.name;

    const statusDiv = document.createElement('div');
    statusDiv.className = 'result-extension';
    statusDiv.textContent = '검사 중...';

    resultDiv.appendChild(filenameDiv);
    resultDiv.appendChild(statusDiv);
    resultsContainer.appendChild(resultDiv);

    try {
        // 서버에 파일 검증 요청
        const res = await fetch(`${API_BASE}/validate/file`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                filename: file.name,
                namespace: NAMESPACE
            })
        });

        if (!res.ok) throw new Error('Validation request failed');
        const data = await res.json();

        // 결과에 따라 UI 업데이트
        if (data.allowed) {
            resultDiv.classList.add('allowed');
            filenameDiv.innerHTML = `${file.name} <span class="status-badge allowed">허용</span>`;
            statusDiv.innerHTML = `확장자: .${data.extension || '(없음)'} - ${data.reason}`;
        } else {
            resultDiv.classList.add('blocked');
            filenameDiv.innerHTML = `${file.name} <span class="status-badge blocked">차단됨</span>`;
            statusDiv.innerHTML = `확장자: .${data.extension} - ${data.reason}`;
        }
    } catch (err) {
        console.error(err);
        resultDiv.classList.add('blocked');
        filenameDiv.innerHTML = `${file.name} <span class="status-badge blocked">오류</span>`;
        statusDiv.textContent = '검사 중 오류가 발생했습니다';
    }
}
