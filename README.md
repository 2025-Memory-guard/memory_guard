# 📖 인증 API (Authentication API)

JWT(Access Token, Refresh Token)를 기반

## 1. 로그인 (Login)

사용자(보호자, 피보호자 공통)의 아이디와 비밀번호로 로그인하여 인증 토큰을 발급받습니다.

-   **URL**: `/user/login`
-   **Method**: `POST`
-   **Description**: 로그인 성공 시, `Access Token`을 포함한 정보를 Body로 반환하고, `Refresh Token`은 `HttpOnly` 쿠키로 설정하여 응답합니다.
-   **Request Body**:
    ```json
    {
      "userId": "string",
      "password": "string"
    }
    ```
-   **Response Body (200 OK)**:
    ```json
    {
      "grantType": "Bearer",
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": "string",
      "roles": ["ROLE_USER"]
    }
    ```
-   **Response Cookie (200 OK)**:
    - `Set-Cookie`: `refreshToken=eyJhbGciOiJIUzI1NiJ9...; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax`

## 2. 토큰 재발급 (Reissue Token)

만료된 Access Token을 Refresh Token을 이용해 재발급받습니다.

-   **URL**: `/token/reissue`
-   **Method**: `POST`
-   **Description**: 요청 시 Cookie에 담긴 유효한 `Refresh Token`을 확인하여 새로운 `Access Token`을 발급합니다.
-   **Request**:
    - **Cookie**: `refreshToken=eyJhbGciOiJIUzI1NiJ9...` (로그인 시 발급받은 쿠키)
-   **Response Body (200 OK)**:
    - 새로운 Access Token 정보를 반환합니다.
    ```json
    {
      "grantType": "Bearer",
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": null,
      "roles": null
    }
    ```
-   **Response (401 Unauthorized)**:
    - Refresh Token이 없거나 유효하지 않을 경우 발생합니다.
    - **Body**: `"Refresh Token이 없습니다."` 또는 `"유효하지 않은 Refresh Token 입니다. 다시 로그인해주세요."`





