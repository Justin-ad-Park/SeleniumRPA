#!/bin/bash

# 실행 중인 chromedriver 프로세스를 모두 kill
PIDS=$(pgrep chromedriver)

if [ -z "$PIDS" ]; then
  echo "실행 중인 chromedriver 프로세스가 없습니다."
else
  echo "chromedriver 프로세스를 종료합니다: $PIDS"
  kill -9 $PIDS
fi