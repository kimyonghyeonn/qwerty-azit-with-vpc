FRONTEND 가이드 먼저 진행 후 백엔드 가이드에 따라 EC2를 세팅해주시기 바랍니다.

EC2란?
AWS 상에서 빌릴수 있는 컴퓨팅 자원이며 성능, 용량 등 설정에 따라 비용 측정이 다르다.
경험상 EC2는 개발서버 테스트용으로 실행시 제일 가볍고 비용이 적은 t2.micro 를 사용하는것을 추천

아래는 가이드 문서에 따라 EC2 세팅 방법을 적어둔것임

EC2 세팅 가이드

1. AWS 로그인 하여 콘솔에서 EC2 검색
2. 인스턴스 시작 클릭 > 이름 기입 후 OS 이미지는 Amazon Linux (대부분 기본 세팅되어있음)
3. (중요) 인스턴스 유형은 t2.micro 로 설정
4. (중요) 새 키 페어 생성 클릭 => 키페어 이름 입력 후 유형은 RSA, 프라이빗 키 파일 형식은 .pem 으로 지정 (해당 키는 EC2 인스턴스에 접근할수 있는 키이기 때문에 별도로 저장해둔다.)
5. 네트워크 설정 => 방화벽(보안 그룹) 에서 보안 그룹 생성 선택 => SSH, HTTPS, HTTP 트래픽 허용으로 지정한다.

SSH, HTTPS, HTTP 트래픽을 허용하는 이유
SSH의 경우 EC2 로 접속하기 위해서는 SSH 통신(인바운드)가 허용되어야 한다.
또한 웹앱 서비스를 하기 위해서는 HTTPS, HTTP 요청에 대해서 WEB/WAS에서 통신되어야 하기 때문에 허용해둔다.

6. 화면 좌측 네트워크 및 보안 => 보안 그룹 클릭 후 해당 보안 그룹 ID 를 클릭 후 인바운드 규칙 편집 을 클릭한다.

7. 규칙 추가 클릭 =>
   유형 : MYSQL/Aurora
   소스 : 사용자 지정 (자신의 보안 규칙으로 지정, WEB/WAS 대상이 될 EC2의 보안그룹명 입력)
   설명 : ec2-to-rds
   세팅 후 규칙 저장

왜 자기 자신을 보안규칙으로 지정하는가?
동일한 보안규칙을 사용하는 서비스끼리 통신을 허용하기 위함

8. 생성된 EC2 인스턴스를 연결한다. 연결은 ec2 상세화면 우측의 연결 버튼 클릭 => EC2 인스턴스 연결 탭에서 우측 하단 연결 버튼을 클릭하여 연결한다.

이후 터미널에서의 절차는 다음과 같다.

8-1. 기본 패키지 업데이트 : sudo yum update -y
8-2. 각종 패키지 설치
Git : sudo yum -y install git

Nginx :
sudo yum -y install nginx
sudo systemctl enable nginx
sudo systemctl start nginx

Java :
sudo yum -y install java-21-amazon-corretto
java -version

Node.js :
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc
nvm install 18
node -v
npm -v

배포디렉토리 생성 :
sudo mkdir -p /srv/app/backend
sudo mkdir -p /srv/app/frontend
sudo mkdir -p /var/www/frontend
sudo chown -R ec2-user:ec2-user /srv/app
sudo chown -R ec2-user:ec2-user /var/www/frontend

8-3. 백엔드(스프링부트) 빌드 & 실행 0. 소스 받기 전 깃허브와 SSH 연결을 위해 EC2 에서 SSH키를 생성한다.

8-3-1. SSH 키 생성 명령 : ssh-keygen -t ed25519 -C "my-ec2-backend-ssh"
Enter file in which to save the key (/home/ec2-user/.ssh/id_ed25519): => Enter
Enter passphrase (empty for no passphrase): => Enter
Enter same passphrase again: => Enter

키 생성 확인 : ls -al ~/.ssh
정상적인 결과 예시
id_ed25519
id_ed25519.pub

생성결과는 다음과 같다. 이때 개인키를 github actions secrets 에 등록한다.
~/.ssh/id_ed25519 ← 개인키
~/.ssh/id_ed25519.pub ← 공개키

왜 개인키를 Github Actions Secrets 에 등록하는가?
현재 구조는 GitHub Actions 에서 CI/CD 된 결과물을 EC2 로 옮겨넣는 구조임
GitHub -> EC2 일때 EC2 로 접속해야하기 때문에 개인키(열쇠) 가 필요
반면 공개키는 비유하자면 열쇠 목록에 해당함

집 = EC2 / 문 = SSH접속 / 사람 = GitHub Actions
사람이 문을 열고 집에 들어가기 위해선 개인키로 문을 열고 들어가야 함
따라서 Github Actions Secrets 에 이름\_SSH_KEY 형태로 저장한다.
(주의) 개인키는 복사해서 개인이 찾기 쉬운곳에 저장

8-3-2. Github Actions secrets 에 에 개인키 등록
cat id_ed25519 >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
cat ~/.ssh/id_ed25519 입력시 키 형태는 다음과 같아야 한다.
-----BEGIN OPENSSH PRIVATE KEY-----
(내용)
-----END OPENSSH PRIVATE KEY-----

개인키 내용을 복사한다. => cat ~/.ssh/id_ed25519
복사 후 Github Actions Secrets 에 등록한다. (이름\_SSH_KEY 형태로 등록)
