FRONTEND CI/CD

frontend-deploy.yml 스크립트의 전반적 실행과정을 요약정리한 파일입니다.

main 브랜치에서 FRONTEND 소스의 push 발생시 Github Actions 가 작동한다.
.github/workflows 하위의 frontend-deploy.yml 에 따라 CICD 가 실행된다.

frontend-deploy.yml 실행 과정

1. ubuntu 기반의 runner 로 소스를 내려받는다 (checkout)
2. 노드 설치 후 빌드 실행
3. ec2에 접속하여 /var/www/frontend 경로가 없다면 생성하고, 있다면 /var/www/frontend 하위의 내용 삭제
4. /var/www/frontend 에 빌드한 dist 폴더의 내용을 붙여넣기 한다.
