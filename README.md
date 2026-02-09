# 🚀 Point-Mall Core API
실무에서 경험한 포인트몰 시스템의 핵심 도메인을 재구성한 프로젝트입니다. 

# Tech Stack
- Language: Java 17
- Framework: Spring Boot 3.5.x
- Persistence: Spring Data JPA, Querydsl
- Database: MySQL
- Build Tool: Gradle
- Test:

# Architecture
ERD
<img width="1260" height="1148" alt="Untitled" src="https://github.com/user-attachments/assets/3cfb4704-08a5-4662-9e53-1f233fb9cfeb" />

# Troubleshooting
1. 동시성 이슈 제어
  - 문제: 상품 주문 시 여러 사용자가 동시에 주문을 했을 때 동시에 재고를 차감하거나 포인트를 사용하는 경우 데이터 정합성이 깨지는 문제 발생
  - 해결:  
