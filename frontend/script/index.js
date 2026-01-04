// API 기본 경로
const API_BASE = '/api';

// 동적 네임스페이스 지원 - 선택된 시나리오를 localStorage에서 불러오거나 기본값 사용
let currentNamespace = localStorage.getItem('selectedScenario') || DEFAULT_SCENARIO;

// DOM 요소 참조
const fixedContainer = document.getElementById('fixed-extensions');     // 고정 확장자 컨테이너
const customContainer = document.getElementById('custom-extensions');   // 커스텀 확장자 컨테이너
const customInput = document.getElementById('custom-input');             // 확장자 입력 필드
const addBtn = document.getElementById('add-btn');                       // 추가 버튼
const counter = document.getElementById('counter');                      // 커스텀 확장자 개수 표시
const scenarioSelect = document.getElementById('scenario-select');       // 시나리오 선택 셀렉트 박스
const scenarioDescription = document.getElementById('scenario-description'); // 시나리오 설명

// 상수 정의
const MAX_CUSTOM = 200;                 // 최대 커스텀 확장자 개수
const VALID_REGEX = /^[a-z0-9]+$/;      // 유효한 확장자 형식 (영문 소문자, 숫자만)

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener('DOMContentLoaded', () => {
    setupScenarioSelector();    // 시나리오 선택기 설정
    updateScenarioDisplay();    // 시나리오 설명 업데이트
    fetchData();                // 정책 데이터 로드
});

/**
 * 시나리오 선택기 초기 설정 및 이벤트 리스너 등록
 */
function setupScenarioSelector() {
    // 현재 선택된 시나리오로 셀렉트 박스 값 설정
    scenarioSelect.value = currentNamespace;

    // 시나리오 변경 이벤트 처리
    scenarioSelect.addEventListener('change', (e) => {
        currentNamespace = e.target.value;
        localStorage.setItem('selectedScenario', currentNamespace);
        updateScenarioDisplay();
        fetchData(); // 새 시나리오의 정책 다시 로드
    });
}

/**
 * 선택된 시나리오의 설명 표시 업데이트
 */
function updateScenarioDisplay() {
    const scenario = SCENARIOS[currentNamespace];
    if (scenario) {
        scenarioDescription.textContent = scenario.description;
        scenarioSelect.value = currentNamespace;
    }
}

/**
 * 서버에서 정책 데이터를 가져와서 UI에 표시
 */
async function fetchData() {
    try {
        const res = await fetch(`${API_BASE}/policies/${currentNamespace}`);
        if (!res.ok) throw new Error('Failed to load data');
        const data = await res.json();

        renderFixed(data.fixed);    // 고정 확장자 렌더링
        renderCustom(data.custom);  // 커스텀 확장자 렌더링
    } catch (err) {
        console.error(err);
        alert('데이터를 불러오는데 실패했습니다.');
    }
}

/**
 * 고정 확장자 목록을 체크박스 형태로 화면에 표시
 * @param {Array} list - 고정 확장자 목록 (name, checked 속성 포함)
 */
function renderFixed(list) {
    fixedContainer.innerHTML = '';
    list.forEach(item => {
        const label = document.createElement('label');
        label.className = 'checkbox-label';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = item.checked;
        checkbox.addEventListener('change', () => toggleFixed(item.name));

        label.appendChild(checkbox);
        label.appendChild(document.createTextNode(` ${item.name}`));
        fixedContainer.appendChild(label);
    });
}

/**
 * 커스텀 확장자 목록을 태그 형태로 화면에 표시
 * @param {Array} list - 커스텀 확장자 목록 (id, name 속성 포함)
 */
function renderCustom(list) {
    customContainer.innerHTML = '';
    counter.textContent = list.length;

    list.forEach(item => {
        const tag = document.createElement('div');
        tag.className = 'tag';
        tag.innerHTML = `
            ${item.name}
            <span class="close" onclick="deleteCustom(${item.id})">&times;</span>
        `;
        customContainer.appendChild(tag);
    });
}

/**
 * 고정 확장자의 차단 상태를 토글 (체크/체크 해제)
 * @param {string} name - 토글할 확장자 이름
 */
async function toggleFixed(name) {
    try {
        const res = await fetch(`${API_BASE}/policies/${currentNamespace}/fixed/${name}/toggle`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error('Update failed');

        // fetchData() 호출 제거 - 체크박스 상태는 자연스럽게 변경
        // 목록 re-render 방지로 순서 유지
    } catch (err) {
        alert('업데이트 실패: ' + err.message);
        // 오류 시에만 재로드하여 올바른 상태 복구
        fetchData();
    }
}

/**
 * 커스텀 확장자를 추가
 * 클라이언트 측 검증 후 서버에 요청
 */
async function addCustom() {
    const val = customInput.value.trim().toLowerCase();

    // 클라이언트 측 유효성 검증
    if (!val) return;
    if (val.length > 20) {
        alert('확장자는 20자 이내여야 합니다.');
        return;
    }
    if (!VALID_REGEX.test(val)) {
        alert('영문 소문자와 숫자만 입력 가능합니다.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/policies/${currentNamespace}/custom`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ extension: val })
        });

        if (!res.ok) {
            const errorText = await res.text();
            throw new Error(errorText || 'Add failed');
        }

        customInput.value = '';  // 입력 필드 초기화
        fetchData();              // 목록 새로고침
    } catch (err) {
        alert(err.message);
    }
}

// Delete is global ID based
window.deleteCustom = /**
 * 커스텀 확장자를 삭제
 * @param {number} id - 삭제할 확장자의 ID
 */
    async function deleteCustom(id) {
        if (!confirm('이 확장자를 삭제하시겠습니까?')) return;

        try {
            const res = await fetch(`${API_BASE}/extensions/${id}`, {
                method: 'DELETE'
            });
            if (!res.ok) throw new Error('Delete failed');
            fetchData();  // 목록 새로고침
        } catch (err) {
            alert('삭제 실패: ' + err.message);
        }
    };

// 이벤트 리스너 등록
addBtn.addEventListener('click', addCustom);
customInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') addCustom();  // Enter 키로도 추가 가능
});
