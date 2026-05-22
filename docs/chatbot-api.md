# 챗봇(상담) API 명세서

코치 챗봇과 1:1 상담을 진행하는 기능의 API 명세입니다. 이 문서만으로 프론트엔드 구현이 가능하도록 작성되었습니다.

## 1. 개요

| 항목 | 내용 |
|---|---|
| Base URL | `https://woochelin.site` (로컬: `http://localhost:8080`) |
| 공통 Content-Type | `application/json` (요청·응답 모두) |
| 인증 | 없음 |
| 응답 인코딩 | UTF-8 |

### 엔드포인트 목록

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/v1/chatbot/chat` | 챗봇에게 상담 메시지 전송 |
| `GET` | `/api/v1/chatbot/trending` | 이번 주 인기 코치 목록 |

---

## 2. POST `/api/v1/chatbot/chat` — 상담 메시지 전송

선택한 코치(검프/류시) 챗봇에게 메시지를 보내고 답변을 받습니다. `sessionId`로 대화를 식별하며, 같은 `sessionId`로 이어 보내면 이전 대화를 기억한 채 답변합니다.

### 요청 (Request Body)

```json
{
  "sessionId": "3f9a1c2e-8b7d-4e6f-a1b2-c3d4e5f60718",
  "botId": "gump",
  "crewName": "체체",
  "message": "요즘 코딩이 너무 어려워서 자신감이 떨어져요."
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `sessionId` | string | ✅ | 대화 세션 식별자. **프론트엔드가 UUID로 생성**한다. 같은 값 = 같은 대화(기억 유지), 새 값 = 새 대화. 공백 불가. |
| `botId` | string | ✅ | 상담할 코치. `gump`(검프) 또는 `ryusi`(류시). 공백 불가. |
| `crewName` | string | ✅ | 상담받는 크루(수강생) 이름. 코치가 대화 중 이 이름을 부른다. 공백 불가. |
| `message` | string | ✅ | 사용자가 보낸 메시지. 공백 불가. |

> **세션 고정 규칙**: `botId`와 `crewName`은 해당 `sessionId`의 **첫 요청에서 고정**된다. 같은 `sessionId`로 보내는 이후 요청의 `botId`/`crewName` 값은 무시되고 첫 요청 값이 유지된다. 코치나 크루 이름을 바꾸려면 새 `sessionId`(새 UUID)로 시작해야 한다.

### 응답 — `200 OK`

```json
{
  "reply": "체체, 그 마음 충분히 이해해요. 어떤 부분이 가장 어렵게 느껴지나요?"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `reply` | string | 코치 챗봇의 답변. 그대로 화면에 출력하면 된다. |

### 동작 규칙

- **대화 기억**: 세션당 최근 **20개 메시지(질문 10 + 답변 10턴)** 까지 기억한다. 그보다 오래된 대화는 자동으로 잊는다.
- **저장 위치**: 대화는 서버 메모리에만 보관된다. **서버 재시작 시 모든 대화가 사라진다.** 동시 보관 세션 수는 최대 500개이며, 초과 시 가장 오래 쓰이지 않은 세션부터 제거된다.
- **코치별 성격**: `gump`(검프), `ryusi`(류시)는 각자 고유한 페르소나로 답변한다.

---

## 3. GET `/api/v1/chatbot/trending` — 이번 주 인기 코치

최근 7일간 상세 조회가 많았던 코치를 최대 10명까지 인기순으로 반환한다. 챗봇 추천 목록 등에 활용할 수 있다.

### 요청

쿼리 파라미터·바디 없음.

### 응답 — `200 OK`

```json
[
  {
    "targetType": "COACH",
    "targetId": 1,
    "name": "검프",
    "part": "BACKEND",
    "profileImageUrl": "/coach/BE_코치_검프.png",
    "viewCount": 42
  },
  {
    "targetType": "COACH",
    "targetId": 7,
    "name": "류시",
    "part": "SOFT_SKILL",
    "profileImageUrl": "/coach/SS_코치_류시.png",
    "viewCount": 31
  }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `targetType` | string | 항상 `"COACH"`. |
| `targetId` | integer | 코치 ID. |
| `name` | string | 코치 이름. |
| `part` | string | 소속 파트. `BACKEND` \| `FRONTEND` \| `ANDROID` \| `COMMON` \| `SOFT_SKILL`. |
| `profileImageUrl` | string | 프로필 이미지 경로. Base URL에 이어 붙여 사용 (`https://woochelin.site/coach/...`). |
| `viewCount` | integer | 최근 7일 조회 수. |

> 최근 7일간 조회 로그가 없으면 빈 배열 `[]`을 반환한다.

---

## 4. 에러 응답

모든 에러는 아래 형태로 반환된다.

```json
{
  "message": "에러 사유 설명"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `message` | string | 사람이 읽을 수 있는 에러 사유. 화면에 그대로 노출 가능. |

### 상태 코드별 발생 케이스 (`POST /chat` 기준)

| 상태 코드 | 발생 상황 | `message` 예시 |
|---|---|---|
| `400 Bad Request` | 필수 필드가 비어 있거나 누락 | `"message: 공백일 수 없습니다"` (형식: `<필드명>: <사유>`) |
| `404 Not Found` | 존재하지 않는 `botId` | `"챗봇 코치를 찾을 수 없습니다."` |
| `503 Service Unavailable` | Gemini API 키 미설정 | `"Gemini API 키가 설정되어 있지 않습니다. gemini.api-key를 설정해 주세요."` |
| `503 Service Unavailable` | Gemini 호출 실패 | `"Gemini API 호출에 실패했습니다."` |
| `503 Service Unavailable` | Gemini가 빈 응답 반환 | `"Gemini 응답이 비어 있습니다."` |

> `503`은 외부 AI 서버 쪽 문제다. 프론트엔드에서는 "잠시 후 다시 시도해 주세요" 안내와 재시도 버튼을 권장한다.

---

## 5. 프론트엔드 구현 가이드

### 상담 흐름

1. 사용자가 코치(검프/류시)를 선택 → `botId` 결정 (`gump` / `ryusi`)
2. 사용자가 크루 이름 입력 → `crewName`
3. 상담 시작 시 **UUID를 한 번 생성**해 `sessionId`로 보관 (`crypto.randomUUID()`)
4. 사용자가 메시지를 보낼 때마다 `POST /api/v1/chatbot/chat` 호출 — `sessionId`·`botId`·`crewName`은 동일하게, `message`만 갱신
5. 응답의 `reply`를 대화창에 출력
6. "새 상담 시작" 시 새 UUID를 생성해 `sessionId` 교체 (이전 대화 기억 초기화)

### 호출 예시 (JavaScript)

```js
// 상담 세션 시작 시 1회 생성해 보관
const sessionId = crypto.randomUUID();
const botId = "gump";      // 검프, 류시는 "ryusi"
const crewName = "체체";

async function sendMessage(message) {
  const res = await fetch("/api/v1/chatbot/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ sessionId, botId, crewName, message }),
  });

  if (!res.ok) {
    const { message } = await res.json();   // { "message": "..." }
    throw new Error(message);
  }

  const { reply } = await res.json();        // { "reply": "..." }
  return reply;
}
```

### 주의사항

- `sessionId`는 상담 한 건 동안 **절대 바뀌면 안 된다.** 매 요청 새 UUID를 만들면 매번 기억 없는 새 대화가 된다.
- 서버가 재시작되면 진행 중이던 대화 기억이 사라진다. 이 경우 같은 `sessionId`로 보내도 이전 맥락 없이 응답하므로, 장시간 대화에서는 끊김 가능성을 감안한다.
- `botId`는 `gump` / `ryusi`만 사용한다.

---

## 6. 참고: 데이터 타입 표기

| 표기 | JSON 실제 타입 |
|---|---|
| string | 문자열 |
| integer | 정수 (number) |

문서 기준 구현: 세션 기반 챗봇 상담 기능 (인메모리 대화 메모리).
