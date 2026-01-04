/**
 * 시나리오 상수 정의
 * 각 기능별 확장자 차단 정책을 구분하기 위한 설정
 */
const SCENARIOS = {
    chat: {
        namespace: 'chat',              // API에서 사용할 네임스페이스
        displayName: '채팅 기능',        // 사용자에게 표시될 이름
        icon: '💬',                      // 아이콘
        description: '채팅에서 전송 가능한 파일 확장자를 관리합니다',
        color: '#4CAF50'                 // 테마 색상
    },
    work: {
        namespace: 'work',
        displayName: '업무 공유',
        icon: '📁',
        description: '업무 공유 시 업로드 가능한 파일 확장자를 관리합니다',
        color: '#2196F3'
    }
};

/**
 * 기본 시나리오
 * 처음 접속 시 또는 선택된 시나리오가 없을 때 사용
 */
const DEFAULT_SCENARIO = 'chat';
