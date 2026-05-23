# 챗봇 API 프론트엔드 연동 작업 명세

프론트엔드에서 챗봇(상담) 기능을 API와 연결하기 위해 해야 할 작업을 정리한 문서입니다.
API 필드별 상세 스펙은 [`chatbot-api.md`](./chatbot-api.md)를 참고하세요.

---

## 0. 백엔드 전제조건 (연동 전 확인)

### 0-1. CORS — ⚠️ 현재 백엔드에 CORS 설정이 없음

호출 방식에 따라 처리가 달라집니다.

| 상황 | CORS 필요? | 해결 방법 |
|---|---|---|
| **로컬 개발** (프론트 `localhost:5173` → 백엔드 `localhost:8080`) | 필요 | **Vite proxy 설정** (프론트만 작업, 4-2 참고) |
| **배포** (프론트 빌드물을 백엔드 `static/`에 넣어 같은 도메인 서빙) | 불필요 | 작업 없음 |
| 프론트를 별도 도메인(Vercel 등)에 배포 | 필요 | 백엔드에 CORS 설정 추가 필요 |

> 우리 배포 계획(단일 EC2 + 프론트 빌드물을 `static/`에 통합)에서는 **운영 시 CORS가 불필요**합니다.
> 로컬 개발 중에만 Vite proxy로 우회하면 되고, 백엔드 코드는 건드릴 필요가 없습니다.

### 0-2. Gemini API 키

서버에 `GEMINI_API_KEY` 환경변수가 없으면 `/chat` 호출이 **503**을 반환합니다.
백엔드 담당자가 배포 환경에 키를 설정해야 실제 응답이 옵니다. (프론트는 503 처리만 준비)

---

## 1. 사용할 API

| 메서드 | 경로 | 용도 |
|---|---|---|
| `POST` | `/api/v1/chatbot/chat` | 상담 메시지 전송 / 응답 수신 |
| `GET` | `/api/v1/chatbot/trending` | (선택) 인기 코치 목록 |

**요청** (`POST /chat`) — 4개 필드 모두 필수, 공백 불가:
```json
{ "sessionId": "<UUID>", "botId": "gump", "crewName": "체체", "message": "..." }
```
**응답** (`200`):
```json
{ "reply": "코치의 답변" }
```

---

## 2. 프론트엔드 작업 체크리스트

### 2-1. API 호출 모듈
- [ ] Base URL을 환경변수로 분리 (`.env`의 `VITE_API_BASE_URL` 등). 로컬은 빈 값 + proxy, 운영은 빈 값(같은 오리진).
- [ ] `chat(payload)` 함수 — `POST /api/v1/chatbot/chat`
- [ ] `getTrending()` 함수 — `GET /api/v1/chatbot/trending` (인기 코치 노출 시)

### 2-2. 세션 / 상태 관리
- [ ] 상담 시작 시 **`crypto.randomUUID()`로 `sessionId`를 한 번만 생성**해 보관
- [ ] `sessionId`, `botId`(`gump`/`ryusi`), `crewName`을 상담 한 건 동안 고정 유지
- [ ] 화면에 표시할 **대화 목록** state 보관 (`[{ role: 'user'|'bot', text }]`)
- [ ] "새 상담 시작" 시 새 UUID 발급 + 대화 목록 초기화
- [ ] (선택) `sessionId`를 `sessionStorage`에 저장 → 새로고침 후에도 같은 세션 이어가기

### 2-3. 화면 구성
- [ ] 코치 선택 UI — 검프 / 류시 중 택1 → `botId` 결정 (`검프→gump`, `류시→ryusi`)
- [ ] 크루 이름 입력 UI → `crewName`
- [ ] 채팅 화면 — 대화 목록 표시 + 메시지 입력창 + 전송 버튼
- [ ] 전송 중 로딩 표시 (Gemini 응답은 수 초 소요)

### 2-4. 요청·응답 처리 규칙 (중요)
- [ ] 메시지 전송 시 `sessionId`·`botId`·`crewName`은 **동일하게**, `message`만 바꿔 전송
- [ ] ⚠️ **백엔드 응답은 `{ reply }`뿐 — 사용자가 보낸 메시지는 응답에 없음.**
      화면 대화 목록은 프론트가 관리한다:
      1. 사용자가 전송 → 프론트가 `{role:'user'}` 항목을 목록에 **직접 추가**
      2. 응답 도착 → `{role:'bot', text: reply}` 항목 추가
- [ ] `trending`의 `profileImageUrl`은 상대경로(`/coach/...`)이므로 이미지 표시 시 Base URL을 붙인다

### 2-5. 에러 / 로딩 처리
- [ ] `400` — 필수값 누락. (정상 흐름이면 발생 안 함. 입력 검증으로 예방)
- [ ] `404` — 잘못된 `botId`. 코치 선택을 `gump`/`ryusi`로 제한하면 발생 안 함
- [ ] `503` — Gemini 외부 API 문제. "잠시 후 다시 시도해 주세요" 안내 + 재시도 버튼
- [ ] 모든 에러 응답 본문은 `{ "message": "..." }` — 그대로 노출 가능
- [ ] 전송~응답 사이 입력창 비활성화 + 로딩 인디케이터

---

## 3. 반드시 지킬 규칙 (요약)

1. **`sessionId`는 상담 한 건 동안 절대 바꾸지 않는다.** 매 요청 새 UUID를 만들면 매번 기억 없는 새 대화가 된다.
2. `botId`·`crewName`은 세션 첫 요청에 고정된다 — 도중에 코치/이름을 바꾸려면 새 `sessionId`로 시작.
3. `botId`는 `gump`(검프) / `ryusi`(류시)만 사용.
4. 대화 목록 UI는 프론트 책임 — 백엔드는 `reply`만 돌려준다.
5. 서버 재시작 시 백엔드 세션 기억이 사라질 수 있으니, 장시간 대화의 맥락 끊김 가능성을 감안.

---

## 4. 샘플 코드

### 4-1. API 모듈 (`api/chatbot.js`)

```js
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export async function chat({ sessionId, botId, crewName, message }) {
  const res = await fetch(`${BASE_URL}/api/v1/chatbot/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ sessionId, botId, crewName, message }),
  });
  if (!res.ok) {
    const { message } = await res.json();        // { "message": "..." }
    const error = new Error(message);
    error.status = res.status;                   // 400 | 404 | 503
    throw error;
  }
  const { reply } = await res.json();            // { "reply": "..." }
  return reply;
}

export async function getTrending() {
  const res = await fetch(`${BASE_URL}/api/v1/chatbot/trending`);
  if (!res.ok) throw new Error("인기 코치 조회 실패");
  return res.json();
}
```

### 4-2. Vite proxy (로컬 개발용 — `vite.config.js`)

```js
export default defineConfig({
  server: {
    proxy: {
      "/api": "http://localhost:8080",   // /api 요청을 백엔드로 프록시 → CORS 우회
    },
  },
});
```

### 4-3. 상담 흐름 (의사코드)

```js
// 1) 상담 시작 — 코치 선택, 크루 이름 입력 후
const sessionId = crypto.randomUUID();   // 한 번만 생성
const botId = "gump";                    // 검프, 류시는 "ryusi"
const crewName = "체체";
let messages = [];                       // 화면 대화 목록

// 2) 메시지 전송
async function send(text) {
  messages = [...messages, { role: "user", text }];   // 내 메시지 직접 추가
  setLoading(true);
  try {
    const reply = await chat({ sessionId, botId, crewName, message: text });
    messages = [...messages, { role: "bot", text: reply }];
  } catch (e) {
    if (e.status === 503) showRetryToast(e.message);
    else showError(e.message);
  } finally {
    setLoading(false);
  }
}

// 3) 새 상담 — sessionId 재발급 + messages 초기화
```

---

## 5. 작업 순서 권장

1. (백엔드) 배포 환경에 `GEMINI_API_KEY` 설정 — 없으면 503만 반환
2. (프론트) Vite proxy 설정 — 로컬에서 API 호출 가능하게
3. (프론트) API 모듈 작성 → 코치 선택·이름 입력 화면 → 채팅 화면 순으로 구현
4. (프론트) 에러/로딩 처리 보강
5. (배포) 프론트 빌드물을 백엔드 `src/main/resources/static/`에 넣어 통합 — 이때부터 CORS 불필요
