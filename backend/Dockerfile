# 1. Base Image 설정 (Java 버전에 맞게 수정)
FROM amazoncorretto:17-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 호스트에서 빌드된 JAR 파일을 컨테이너 내부로 복사
# 프로젝트 빌드 도구(Gradle/Maven)에 따라 JAR 파일 생성 경로가 다를 수 있음
COPY build/libs/app.jar app.jar

# 4. 포트 노출
EXPOSE 8080

# 5. 실행 명령어 지정 및 local 프로파일 강제 활성화
ENTRYPOINT ["java", "-Dspring.profiles.active=local-test", "-jar", "app.jar"]