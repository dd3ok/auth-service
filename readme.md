# 🔐 OAuth 2.0 인증 서버

> JWT + Redis 기반 분산 인증 시스템 | Hexagonal Architecture

## 🚀 주요 기능

### **핵심 기능**
- 🔗 **멀티 소셜 로그인**: Google, Kakao, Naver OAuth 2.0
- 🔑 **JWT 토큰 관리**: Access/Refresh Token 실시간 모니터링
- 🗄️ **Redis 캐시**: 분산 환경 토큰 동기화
- 📊 **실시간 대시보드**: JWT/Redis 상태 시각화

### **기술 스택**
- **Backend**: Kotlin + Spring Boot 3.x + Spring Security
- **Database**: MySQL 8.x + Redis 7.x
- **Architecture**: Hexagonal Architecture

## ⚡ 빠른 시작

### **1. OAuth 클라이언트 ID/Secret 발급**

#### **🟢 Google**
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 → API 및 서비스 → 사용자 인증 정보
3. OAuth 클라이언트 ID 생성 (웹 애플리케이션)
4. 승인된 리디렉션 URI: `http://localhost:8080/auth/callback/google`

#### **🟡 Kakao**
1. [카카오 개발자센터](https://developers.kakao.com/) 접속
2. 애플리케이션 추가 → 플랫폼 설정 → Web
3. 카카오 로그인 활성화 → Redirect URI: `http://localhost:8080/auth/callback/kakao`
4. 동의항목: 프로필 정보, 카카오계정(이메일) 설정

#### **🟢 Naver**
1. [네이버 개발자센터](https://developers.naver.com/) 접속
2. 애플리케이션 등록 → API 설정
3. 서비스 URL: `http://localhost:8080`
4. Callback URL: `http://localhost:8080/auth/callback/naver`

### **2. 환경 설정 및 실행**

```bash
# 1. 저장소 클론
git clone 
cd oauth-auth-service

# 2. 환경 변수 설정 (.env 또는 IDE 설정)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id  
KAKAO_CLIENT_SECRET=your_kakao_client_secret
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret

# 3. 인프라 실행
docker-compose up -d

# 4. 애플리케이션 실행
./gradlew bootRun
```

### **3. 접속**
- **로그인**: http://localhost:8080/login
- **대시보드**: http://localhost:8080/dashboard

## 🎯 사용 시나리오

### **기본 인증 흐름**
```
1. 소셜 로그인 (Google/Kakao/Naver)
   ↓
2. JWT 토큰 발급 (Access 30분 + Refresh 14일)
   ↓
3. Redis에 Refresh Token 저장 
   ↓
4. 대시보드에서 실시간 모니터링
```

### **분산 토큰 관리 시연**
```
🔄 토큰 갱신 테스트:
1. Access Token 30분 후 자동 만료
2. Refresh Token으로 자동 갱신
3. Redis에서 새 토큰으로 교체

🗑️ 토큰 무효화 테스트:
1. Redis에서 Refresh Token 삭제
2. 갱신 시도 → 실패
3. 강제 로그아웃 → 보안성 검증
```

## 📚 핵심 API

| Endpoint | Method | 기능 |
|----------|--------|------|
| `/api/v1/auth/oauth/{provider}` | POST | 소셜 로그인 |
| `/api/v1/auth/refresh` | POST | 토큰 갱신 |
| `/api/v1/debug/redis/refresh-tokens` | GET/DELETE | Redis 토큰 관리 |


### **JWT + Redis 분산 설계**
- **Access Token**: 브라우저(localStorage) - 단기인증
- **Refresh Token**: Redis 서버 - 장기저장/무효화
- **상태 동기화**: 다중 서버 환경 완벽 지원

### **보안 강화**
- **토큰 무효화**: 서버 주도 즉시 차단
- **실시간 모니터링**: 토큰 상태 시각화

---