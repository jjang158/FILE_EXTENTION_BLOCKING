# File Extension Blocking Service

## 프로젝트 개요
파일 확장자 차단 정책을 관리하는 웹 서비스입니다.
고정된 확장자(Fixed)와 사용자가 직접 추가하는 커스텀 확장자(Custom)를 관리할 수 있습니다.
또한, 도메인별(Namespace) 정책 분리가 가능한 아키텍처로 설계되었습니다.

## 🛠 기술 스택
- **Backend**: Java 17, Spring Boot 3.2, JPA (Hibernate)
- **Frontend**: HTML5, CSS3, Vanilla JS
- **Database**: PostgreSQL 15
- **Infrastructure**: Docker Compose, AWS EC2 (권장 환경)

## 🏗 설계 및 구현 상세

### 1. 데이터베이스 모델링 (ERD)
본 프로젝트는 확장자 차단 정책이 "서비스 전체"가 아닌 "기능 단위(예: 메신저, 게시판)"로 다를 수 있음을 고려하여 설계했습니다.

- **ExtensionPolicy**: 정책의 네임스페이스(namespace)를 관리합니다. (예: `default`, `messenger`)
- **ExtensionRule**: 각 정책 하위의 실제 차단 규칙을 저장합니다.
    - `policy_id` + `extension` 복합 유니크 키를 통해 중복을 방지합니다.
    - `type` (FIXED/CUSTOM) 컬럼으로 고정 규칙과 커스텀 규칙을 명확히 구분합니다.

### 2. 중복 처리 및 정책
- **DB Level**: `Unique Constraint(policy_id, extension)`로 물리적 중복 차단.
- **Application Level**:
    - **고정 확장자 우선**: `FIXED` 타입으로 정의된 확장자는 `CUSTOM`으로 추가할 수 없습니다. (예: `exe`가 고정 목록에 있다면, 사용자가 `exe`를 커스텀으로 추가 시도 시 에러 발생)
    - **소문자 정규화**: 모든 입력은 저장 전 소문자로 변환됩니다.

### 3. 보안 고려사항
- **Validaiton**:
    - **Regex**: `^[a-z0-9]+$` 정규식을 사용하여 특수문자나 스크립트 주입 공격 방지.
    - **Length**: 최대 20자 제한.
    - **Server/Client Double Check**: 프론트엔드와 백엔드 양쪽에서 검증 수행.
- **Docker Security**:
    - 백엔드 컨테이너는 `non-root` 유저로 실행되도록 설정하여, 컨테이너 탈취 시 호스트 시스템 위협 최소화.

### 4. 추가 고민 사항 (Scalability)
- **동시성(Concurrency)**: 커스텀 확장자 200개 제한은 동시에 여러 요청이 올 경우 초과될 위험이 있습니다. 이를 방지하기 위해 Service 계층에 트랜잭션 처리를 적용했습니다. (더 엄격한 제어가 필요하면 DB Lock 고려 가능)
- **API 확장성**: `DELETE` API는 확장자 문자열이 아닌 ID 기반으로 설계하여, 향후 확장자명이 변경되거나 특수 케이스가 생겨도 안전하게 식별 가능하도록 했습니다.

## 🚀 실행 방법 (Docker Compose)

### 1. 사전 준비
Docker 및 Docker Compose가 설치되어 있어야 합니다.

### 2. 실행
프로젝트 루트 경로에서 아래 명령어 실행:
```bash
docker-compose up -d --build
```

### 3. 접속
- **Web UI**: http://localhost
- **API**: http://localhost/api

### 4. 종료
```bash
docker-compose down -v
```
( `-v` 옵션은 DB 데이터를 초기화하려 할 때만 사용하세요. 영구 저장을 원하면 제외)

## 📂 디렉토리 구조
```
.
├── backend/            # Spring Boot Project
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/           # Static Web Files
│   ├── index.html
│   ├── style.css
│   └── app.js
├── database/           # DB Init scripts
├── docker-compose.yml  # Container Orchestration
└── nginx.conf          # Web Server Config
```
