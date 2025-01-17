# :womans_hat: Look At Me

<div align="center">
<br><br>

![2023-02-02 10;31;35](https://user-images.githubusercontent.com/74748851/216215221-b583319a-06d3-4c45-b2aa-9eca4fe8205e.PNG)
<br><br>
</div>

## :mag: 프로젝트 소개
### 사용자가 직접 자신의 패션 스타일을 공유할 수 있고 렌탈을 통해 다른 사람들과 쉐어도 가능!
### 평소 입지 않는 옷을 빌려주고 돈을 벌고, 다른 사람들의 옷을 마음껏 빌려 입을 수 있어요 :)
Loot At Me에서는 다른 사용자들이 올린 다양한 코디를 둘러보며 코디에 대한 고민을 해결하고 새로운 옷을 발견할 수 있습니다.

<br>

### :pushpin: 타겟층
1. 평소 어떤 옷을 입을지 막막한 사람들
2. 특별한 날 특별한 옷을 입고싶지만 한번 입으려 구입하기에는 너무 비싸 고민인 사람들
3. 평소 입지 않는 옷들을 어떻게 해야 할 지 고민인 사람들
4. 다양한 코디를 둘러보며 새로운 옷을 발견하고 싶은 사람들

### :bulb: 구상 배경
사람들이 옷을 공유하며 활용 가치작 있는 의류가 버려지거나 방치되는 것을 막기 위해 이러한 서비스를 기획하게 되었습니다.

### :link: 배포 주소
(:star:**배포 주소 수정**) https://lookatme.myprojectsite.shop/

<br>

## :family: 팀원 소개

| FE | FE | FE | BE | BE | BE |
|:---:|:---:|:---:|:---:|:---:|:---:|
|**김나율**|**박영선**|**이승준**|**오성범**|**이도형**|**조혜주**|
| [@nayul34](https://github.com/nayul34)  | [@yspark14](https://github.com/yspark14) | [@01055986186](https://github.com/01055986186) | [@Seongbaem](https://github.com/Seongbaem) | [@eheh12321](https://github.com/eheh12321) | [@hyejuc](https://github.com/hyejuc) |

<br>

## :wrench: 기술 스택

> API 문서, ERD 다이어그램, 피그마 등 문서는 [Github Wiki](https://github.com/eheh12321/Look-At-Me_fork/wiki)를 참고해주세요 :smiley:

![2023-02-02 11;03;18](https://user-images.githubusercontent.com/74748851/216216126-290f3a7b-82e6-4575-abfb-e930a67debbc.PNG)


<br>

## :open_file_folder: 서비스 주요 기능 소개
### 회원 가입
- 아래 내용 입력 후 회원 가입이 가능합니다.
  - 닉네임
  - 이메일 주소
  - 비밀번호 (문자, 숫자 포함 8글자 이상)
  - 키 (선택 사항)
  - 몸무게 (선택 사항)
### 로그인
- 일반 로그인, 구글 로그인이 가능합니다.
- 일반 로그인의 경우 아이디는 이메일입니다.
### 로그아웃
- 로그아웃이 가능합니다.
### 게시물
#### 작성
- 원하는 사진과 게시글 내용, 상품 정보를 작성할 수 있습니다.
- 상품 정보는 최소 2개의 상품을 입력행 하며 아래 정보들을 기입해야 합니다.
  - 카테고리
  - 브랜드
  - 제품명
  - 사이즈
  - 가격
  - 구매링크
  - 렌탈 가능 체크
  - 렌탈 금액
#### 수정, 삭제
- 본인이 올린 게시글을 수정, 삭제할 수 있습니다.
#### 조회
- 게시글은 카테고리별로 조회가 가능합니다.
  - 아우터
  - 상의
  - 하의
  - 원피스
  - 모자
  - 신발
- 게시글은 최신순, 가격높은순, 가격낮은순으로 정렬이 가능합니다.
- 대여가 가능한 상품이 포함된 게시글만 조회할 수 있습니다.
### 댓글
- 원하는 게시글에 댓글을 작성할 수 있습니다.
- 본인이 작성한 댓글을 수정, 삭제할 수 있습니다.
### 팔로우
- 다른 사용자를 팔로우할 수 있습니다.
- 팔로우 아이콘을 한 번 더 눌러 팔로우를 취소할 수 있습니다.
- 팔로우와 팔로워 목록은 사용자 페이지에서 확인할 수 있습니다.
### 좋아요
- 게시물 목록과 게시물 상세 페이지에서 마음에 드는 게시물을 좋아요할 수 있습니다.
- 좋아요 아이콘을 한 번 더 눌러 좋아요를 취소할 수 있습니다.
- 좋아요를 누른 게시물은 나의 관심코디에 등록되어 마이페이지에서 확인할 수 있습니다.
### 채팅
- 게시물에 채팅을 누르면 게시글을 올린 사용자에게 메시지를 보낼 수 있습니다.
- 목록에서 채팅방 목록을 확인할 수 있습니다.
### 마이 페이지
- 나의 정보를 조회하거나 수정할 수 있습니다.
- My Codi에서 사용자가 올린 게시물을 볼 수 있습니다.
- Like Codi에서 사용자가 좋아요를 누른 게시물을 볼 수 있습니다.
### 다른 사용자 페이지
- 해당 사용자의 정보와 팔로우, 팔로워 목록을 볼 수 있습니다.
- 해당 사용자가 작성한 게시물을 볼 수 있습니다.
### 사이트 소개, 서비스 사용법 페이지
- 배너 슬라이더 주 사이트 소개르 누르면 사이트 소개와 서비스 사용법을 볼 수 있습니다.

