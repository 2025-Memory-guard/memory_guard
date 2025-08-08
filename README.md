## DB ERD**
https://www.erdcloud.com/d/SJiLvMtFndTPs4hyX

## **Memory Guard API 명세서**

#### ** 피보호자(사용자) 회원가입**

-   **Description**: 새로운 피보호자(사용자) 계정을 생성합니다.
-   **URL**: `/ward/signup`
-   **Method**: `POST`
-   **Auth**: `필요 없음`
-   **Request Body**: `application/json`
    ```json
    {
      "userId": "string (필수)",
      "username": "string (필수, 고유해야 함)",
      "password": "string (필수)"
    }
    ```
-   **Success Response**: `200 OK`
    -   **Body**: `"회원가입이 완료되었습니다."` (string)
-   **Error Response**:
    -   `400 Bad Request`: 이미 존재하는 `userId` 또는 `username`일 경우 (`"code": "INVALID_REQUEST"`)

#### ** 보호자 회원가입**

-   **Description**: 새로운 보호자 계정을 생성하고, 기존 피보호자와 연결합니다.
-   **URL**: `/guard/signup`
-   **Method**: `POST`
-   **Auth**: `필요 없음`
-   **Request Body**: `application/json`
    ```json
    {
      "userId": "string (필수)",
      "username": "string (필수, 고유해야 함)",
      "password": "string (필수)",
      "wardUserId": "string (필수, 연결할 피보호자의 userId)"
    }
    ```
-   **Success Response**: `200 OK`
    -   **Body**: `"보호자 회원가입이 완료되었습니다."` (string)
-   **Error Response**:
    -   `400 Bad Request`: `userId` 또는 `username`이 중복되거나, `wardUserId`가 존재하지 않거나, 해당 `wardUserId`를 가진 사용자가 피보호자 권한(`ROLE_USER`)이 없을 경우 (`"code": "INVALID_REQUEST"`)
    -   `500 Internal Server Error`: 서버에 `ROLE_GUARD`가 정의되지 않은 경우 (`"code": "INVALID_STATE"`)

#### ** 로그인**

-   **Description**: `userId`와 `password`로 로그인하여 Access Token과 Refresh Token을 발급받습니다.
-   **URL**: `/user/login`
-   **Method**: `POST`
-   **Auth**: `필요 없음`
-   **Request Body**: `application/json`
    ```json
    {
      "userId": "string (필수)",
      "password": "string (필수)"
    }
    ```
-   **Success Response**: `200 OK`
    -   **Body**: `application/json`
        ```json
        {
          "grantType": "Bearer",
          "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
          "userId": "string",
          "roles": ["ROLE_USER"] // 또는 ["ROLE_GUARD"]
        }
        ```
    -   **Cookie**:
        -   `Set-Cookie`: `refreshToken=eyJhbGciOiJIUzI1NiJ9...; Path=/; Max-Age=...; HttpOnly; SameSite=Lax`
-   **Error Response**:
    -   `401 Unauthorized`: `userId`가 존재하지 않거나 `password`가 틀렸을 경우 (`"code": "AUTHENTICATION_FAILED"`)

#### ** Access Token 재발급**

-   **Description**: 유효한 Refresh Token을 사용하여 새로운 Access Token을 재발급받습니다.
-   **URL**: `/token/reissue`
-   **Method**: `POST`
-   **Auth**: `필요 없음`
-   **Request**:
    -   **Cookie**: `refreshToken=...` (로그인 시 발급받은 쿠키)
-   **Success Response**: `200 OK`
    -   **Body**: `application/json` (재발급된 Access Token 정보)
        ```json
        {
          "grantType": "Bearer",
          "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
        }
        ```
-   **Error Response**:
    -   `401 Unauthorized`: Refresh Token이 쿠키에 없거나 유효하지 않을 경우. 유효하지 않은 토큰일 경우 `refreshToken` 쿠키가 삭제됩니다.

---

### ** 음성 및 일기 API**

**※ 모든 API는 `ROLE_USER` 권한이 있는 사용자의 Access Token이 필요합니다.**

#### ** 음성 파일 업로드 및 분석**

-   **Description**: 사용자가 녹음한 음성 파일을 업로드합니다. 서버는 파일을 저장하고, 치매 위험도 분석 및 음성 일기 생성을 비동기적으로 수행할 수 있습니다.
-   **URL**: `/api/ward/audio/evaluation`
-   **Method**: `POST`
-   **Auth**: `Bearer Token (ROLE_USER)`
-   **Request**: `multipart/form-data`
    -   **Part**:
        -   `key`: `audioFile`
        -   `value`: (음성 파일)
-   **Success Response**: `200 OK`
    -   **Body**: `"오디오 파일이 성공적으로 저장되었습니다."` (string)
-   **Error Response**:
    -   `400 Bad Request`: 오디오 파일이 비어있는 경우 (`"code": "INVALID_ARGUMENT"`)
    -   `500 Internal Server Error`: 파일 저장 중 I/O 오류 발생 시 (`"code": "FILE_IO_ERROR"`)

#### ** 음성 파일 및 일기 조회**

-   **Description**: 특정 `audioId`에 해당하는 음성 파일과, 그 음성으로 생성된 일기 내용을 함께 조회합니다.
-   **URL**: `/api/ward/audio/play/{audioId}`
-   **Method**: `GET`
-   **Auth**: `Bearer Token (ROLE_USER)`
-   **Path Variable**:
    -   `audioId`: `long` (조회할 오디오의 ID)
-   **Success Response**: `200 OK`
    -   **Content-Type**: `multipart/form-data`
    -   **Body**: 두 개의 파트로 구성된 multipart 응답
        1.  **diary (json)**:
            -   `Content-Type`: `application/json`
            -   **Body**:
                ```json
                {
                  "title": "string (일기 제목)",
                  "body": "string (일기 본문)",
                  "authorName": "string (작성자 이름)",
                  "writtenAt": "date (작성일, yyyy-MM-dd)"
                }
                ```
        2.  **audio (file)**:
            -   `Content-Type`: `audio/wav` (또는 업로드된 파일의 타입)
            -   **Body**: (오디오 파일의 바이너리 데이터)
-   **Error Response**:
    -   `400 Bad Request`: 해당 `audioId`의 일기를 찾을 수 없을 경우 (`"code": "INVALID_ARGUMENT"`)
    -   `500 Internal Server Error`: 해당 `audioId`의 오디오 파일을 찾을 수 없거나 I/O 오류 발생 시 (`"code": "FILE_IO_ERROR"`)
