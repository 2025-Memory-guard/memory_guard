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
