## DB ERD
https://www.erdcloud.com/d/SJiLvMtFndTPs4hyX


## 공유 할 부분 

### **1.  API 에러 핸들링 컨벤션**

 `GlobalExceptionHandler`가 설정되어 있어, 컨트롤러에서 발생하는 대부분의 예외를 일관된 형식으로 처리합니다.

-   **이렇게 해주세요 (Do) 👍**
    -   컨트롤러에서는 `try-catch` 문을 사용하지 마세요.
    -   서비스(Service) 계층에서 비즈니스 로직 검증에 실패했을 경우, 상황에 맞는 예외를 `throw` 해주세요. 핸들러가 알아서 잡아 HTTP 상태 코드와 표준 에러 메시지를 생성합니다.

    **[Before 👎]**
    ```java
    // UserController.java
    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto) {
        try {
            TokenDto tokenInfo = userService.login(loginDto.getUserId(), loginDto.getPassword());
            // ... 생략 ...
            return ResponseEntity.ok(loginResponseDto);
        } catch (AuthenticationException e) {
            ErrorResponse response = new ErrorResponse("AUTH_FAILED", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
    ```
    **[After 👍]**
    ```java
    // UserController.java
    @PostMapping("/user/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        // userService.login 내부에서 예외가 발생하면 GlobalExceptionHandler가 처리합니다.
        TokenDto tokenInfo = userService.login(loginDto.getUserId(), loginDto.getPassword());
        // ... 생략 ...
        return ResponseEntity.ok(loginResponseDto);
    }
    ```

-   **새로운 예외 추가가 필요하다면?**
    -   만약 처리해야 할 새로운 종류의 예외가 있다면 `GlobalExceptionHandler.java` 파일에 `@ExceptionHandler`를 추가하여 등록해주세요.

### ** JWT 인증/인가 테스트 방법**

로컬에서 개발 및 테스트의 편의를 위해 현재는 모든 API 요청이 허용된 상태입니다. 실제 JWT 인증/인가 로직을 테스트하려면 간단한 주석 해제가 필요합니다.

-   **파일 위치**: `src/main/java/com/example/memory_guard/global/config/SecurityConfig.java`
-   **활성화 방법**: 아래 코드 블록의 주석을 제거하고, 그 아래의 `requestMatchers("/**").permitAll()` 부분을 주석 처리하거나 삭제해 주세요.

    ```java
    // SecurityConfig.java
    
    // 이 부분의 주석을 해제하세요!
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/user/login", "/guard/login", "/token/reissue").permitAll()
        .requestMatchers("/api/ward/**").hasRole("USER") // 피보호자 API는 ROLE_USER 필요
        .requestMatchers("/api/guard/**").hasRole("GUARD") // 보호자 API는 ROLE_GUARD 필요
        .anyRequest().authenticated()
    )
    /*
    // 이 부분은 주석 처리 또는 삭제하세요!
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/**").permitAll()
        .anyRequest().authenticated()
    )
    */
    ```
-   **참고**: 활성화 후 API를 테스트할 때는 Postman 등의 클라이언트에서 **`Authorization` 헤더에 `Bearer {AccessToken}`**을 포함하여 요청해야 합니다.

### ** 초기 데이터 안내**

-   **피보호자 (ROLE_USER) 계정**
    -   **ID**: `user1`
    -   **Password**: `user1`

-   **보호자 (ROLE_GUARD) 계정**
    -   **ID**: `guard1`
    -   **Password**: `guard1`
    -   **참고**: `guard1` 계정은 `user1` 계정과 자동으로 연결(피보호자-보호자 관계)되어 있습니다.

---

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
