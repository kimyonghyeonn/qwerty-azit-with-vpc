BACKEND CI/CD

backend-deploy.yml 스크립트의 전반적 실행과정을 요약정리한 파일입니다.

main 브랜치에서 FRONTEND 소스의 push 발생시 Github Actions 가 작동한다.
.github/workflows 하위의 backend-deploy.yml 에 따라 CICD 가 실행된다.

backend-deploy.yml 실행 과정

1. ubuntu 기반의 runner 로 소스를 내려받는다 (checkout)
2. JDK21, Gradle 캐시로 환경 세팅
3. backend clean -> build
4. ec2에 접속하여 /srv/app/backend/build/libs 경로가 없다면 생성한다.
5. 3번에서 생성된 Backend/build/libs/\*.jar 파일들을 복사, ec2의 /srv/app/backend/build/libs 경로에 붙여넣기
6. plain.jar, app.jar 가 아닌 실행 가능한 jar 파일을 선택하여 /srv/app/backend/build/libs/ 하위의 app.jar 에 붙여넣는다.
7. ec2의 백엔드 서비스 재시작
8. 재시작 결과를 바로 로그로 확인
