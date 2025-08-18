#!/usr/bin/env bash
set -euo pipefail

# 어떤 쉘 프로필을 쓸지 결정
PROFILE="${HOME}/.zshrc"
case "${SHELL:-}" in
  */bash) PROFILE="${HOME}/.bashrc";;
  */zsh)  PROFILE="${HOME}/.zshrc";;
esac

ENV_DIR="${HOME}/.marqvision"
ENV_FILE="${ENV_DIR}/.env"

mkdir -p "$ENV_DIR"
chmod 700 "$ENV_DIR"

echo "MARQVISION 환경변수를 설정합니다."
read -rp "MARQVISION_EMAIL: " EMAIL
read -rsp "MARQVISION_PASSWORD: " PASSWORD; echo

# .env 파일에 저장 (권한 제한)
cat > "$ENV_FILE" <<EOF
# MarqVision automation env (created $(date))
export MARQVISION_EMAIL="$EMAIL"
export MARQVISION_PASSWORD="$PASSWORD"
EOF
chmod 600 "$ENV_FILE"

# 쉘 프로필에 source 라인 추가 (중복 방지)
if ! grep -Fq 'source "$HOME/.marqvision/.env"' "$PROFILE" 2>/dev/null; then
  {
    echo
    echo '# Load MarqVision env'
    echo '[ -f "$HOME/.marqvision/.env" ] && source "$HOME/.marqvision/.env"'
  } >> "$PROFILE"
  echo "프로필에 로드 설정 추가: $PROFILE"
else
  echo "프로필에 로드 설정이 이미 존재합니다: $PROFILE"
fi

echo "저장 완료: $ENV_FILE"
echo
echo "지금 바로 적용하려면 다음 명령 중 하나를 실행하세요:"
echo "  source \"$ENV_FILE\""
echo "  또는 새 터미널을 열어 실행하세요."
