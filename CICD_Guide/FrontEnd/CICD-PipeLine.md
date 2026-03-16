FrontEnd 배포 자동화 구성 정리

FrontEnd CI/CD 파이프라인 구성 과정은 다음과 같다.

개발자 push
↓
GitHub Actions
↓
빌드 산출물 생성
↓
S3 업로드
↓
CodeDeploy 배포 생성
↓
EC2의 CodeDeploy Agent가 배포 수행
↓
Nginx 재기동

각 역할은 다음과 같다.

1. GitHub Actions: 빌드, 압축, 업로드, 배포 트리거
2. S3: 배포본(zip) 저장소
3. CodeDeploy: 어떤 EC2에 어떤 순서로 배포할지 제어
4. EC2 + CodeDeploy Agent: 실제 파일 배치, 스크립트 실행, 서비스 재시작

배포패키지 구성
frontend-deploy.zip
│
├ dist/ ← Vue build 결과
│ ├ index.html
│ ├ assets/
│ └ favicon.ico
│
├ appspec.yml
│
└ scripts/
└ deploy.sh

CI/CD 준비 과정

- 준비 전 IAM 사용자는 반드시 생성되어 있어야함

1. Github Actions 를 위한 AWS 접속정보 저장
   GitHub Actions 에서는 AWS 에 접근하기 위한 secrets 를 저장하고 있어야 한다. FrontEnd 에서는 AWS 접속정보인 AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 를 레포지토리 > Settings > 우측 배너의 Secrets and variables > Actions 에 저장한다.
   AWS_ACCESS_KEY_ID : IAM 계정 ID
   AWS_SECRET_ACCESS_KEY : IAM 계정 PW

2. S3 생성
   버킷 이름은 소문자 작성 / 이때 반드시 EC2, CodeDeploy 와 동일한 리전으로 구성
   버킷 생성 후 폴더 구조는 frontend/backend 이렇게 구성한다.
   구성 예시 :
   qwerty-azit-codedeploy-artifacts
   │
   ├ frontend
   │ ├ frontend-deploy-a34f2.zip
   │ ├ frontend-deploy-b28d1.zip
   │ └ frontend-deploy-c9123.zip
   │
   └ backend
   ├ backend-deploy-a93ab.zip
   ├ backend-deploy-b92dd.zip
   └ backend-deploy-c3321.zip

3. IAM 사용자 / EC2 권한 부여 및 CodeDeploy Agent 설치
   3-1. IAM 사용자
   배포를 위한 IAM 사용자 계정에는 권한이 부여되어있어야 한다. 개발을 위해서는 AdministratorAccess 권한이 사용되었지만 실제 운영에서는 필수 권한인 S3, CodeDeploy 와 관련된 권한만 부여한다.

3-2. EC2
현재 구성되어있는 WEB, WAS 서버에는 IAM 역할이 부여되어있어야 한다. 구성은 다음과 같다.
구성 예시 :
EC2
│
└ IAM Role (EC2SSMRole)
│
├ AmazonEC2RoleforAWSCodeDeploy : CodeDeploy가 EC2에 배포할 수 있도록 하는 핵심 권한 (S3 배포 파일 다운로드, CodeDeploy와 통신, 배포 상태 보고)
└ AmazonSSMManagedInstanceCore : AWS Systems Manager (SSM) 관련 권한 (EC2 원격 명령 실행, Session Manager SSH, 패치 관리, 로그 관리)

역할 부여가 완료되었다면 EC2 에 CodrDeploy Agent 가 설치되어있어야 한다. (설치 방법은 설치 가이드 참고)

4. CodeDeploy 구성

- CodeDeploy 구성 전 IAM 사용자 > 역할 에서 CodeDeployServiceRole (AWSCodeDeployRole) 를 등록한다.
  등록 후 CodeDeploy 애플리케이션 생성 => 배포그룹(FrontEnd/BackEnd) 생성을 진행한다.
  서비스 역할은 CodeDeployServiceRole 선택
  환경구성은 Amazon EC2 인스턴스 체크, 키 : Role / 값 : Front는 WEB, Back은 WAS 입력
  키, 값은 CodeDeploy 가 EC2를 Tag 기반으로 찾기 떄문에 설정
  배포 설정은 무중단배포를 위해 CodeDeployDefault.OneAtTime (=한번에 하나씩 배포)
  로드밸런서 활성화 체크, 로드밸런서 유형 체크 후 대상그룹 (Front는 Front-TargetGroup / Back은 Back-TargetGroup)

이제 FrontEnd 배포 과정에 대한 전반적인 구조를 잡는다.
우선 Github Actions 로 main 브랜치 push 발생시 Runner 를 사용한 배포 결과물을 만든다. 이때 배포결과물을 만들기 위한 스크립트를 작성하여 .github > workflows 하위에 deploy-private-frontend.yml 스크립트를 작성한다.
