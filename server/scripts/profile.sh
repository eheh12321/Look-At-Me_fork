#!/usr/bin/env bash

# 쉬고있는 profile 찾기
function find_idle_profile() {
    DOMAIN=https://myprojectsite.shop/auth/profile

    # 현재 애플리케이션이 몇번 포트로 실행되고 있는지 확인
    RESPONSE_CODE=$(curl --max-time 5 -s -o /dev/null -w "%{http_code}" $DOMAIN)

    if [ "${RESPONSE_CODE}" -ge 400 ] || [ "${RESPONSE_CODE}" -eq 000 ] # 400보다 크거나, 000(TimeOut)이면 -> Error 발생
    then
      CURRENT_PROFILE=real2 # 에러 발생 시 real1 포트로 보내도록 세팅
    else # 정상 상태(200) 이라면
      CURRENT_PROFILE=$(curl -s $DOMAIN) # 사이트에서 현재 사용중인 포트를 응답해줌(real1/real2)
    fi
    # 현재 nginx가 real1 포트로 연결되어 있음 -> 새로운 앱은 real2 포트로 연결되어야 함
    if [ "${CURRENT_PROFILE}" == real1 ]
    then
      IDLE_PROFILE=real2
    else
      IDLE_PROFILE=real1
    fi

    echo "${IDLE_PROFILE}"
}

# profile -> port 변환
function find_idle_port() {
  IDLE_PROFILE=$(find_idle_profile) # SubShell 호출
  if [ "${IDLE_PROFILE}" == real1 ]
  then
    echo "8081"
  else
    echo "8082"
  fi
}