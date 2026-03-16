VPC 구축 이후 데이터 흐름 정리 (FrontEnd)

현재 도메인은 NameCheap 에서 구매하였고, 해당 도메인과 로드밸런서의 DNS 를 다음과 같이 매핑하여 관리중이다.

이때 도메인을 구매하였다면, AWS Certification Manager (=ACM) 에서 해당 도메인 인증서를 먼저 발급해야한다. ACM 인증서 검증을 통해 발급된 인증서는 HTTPS 연결을 위해 사용된다.

이후 도메인 구성은 다음과 같다.

qwerty-azit.com → 웹 서비스 (루트 도메인)
www.qwerty-azit.com → 웹 호환 (서브 도메인)
api.qwerty-azit.com → 백엔드 API (api 서브도메인)

이때 호스트 (=api, www, @, CNAME) 애 대한 구성은 다음과 같다.

Type Host Value 목적
ALIAS @ ALB DNS 루트 도메인 연결
CNAME www ALB DNS www 도메인 연결
CNAME api ALB DNS API 도메인 연결
CNAME ACM CNAME 이름 ACM CNAME 값 주소 ACM 인증서 검증

이렇게 도메인-로드밸런서 연결이 마무리되었고, 웹에서의 api 요청에 대한 흐름은 다음과 같다.

Browser
│
▼
DNS (Namecheap)
│
▼
ALB DNS
│
▼
Internet Gateway
│
▼
ALB (Public Subnet)
│
▼
VPC 내부 라우팅
│
▼
Private Subnet
│
▼
EC2 in Private Subnet
