#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
JAR_PATH="$PROJECT_DIR/target/clinica-api-0.0.1-SNAPSHOT.jar"

cd "$PROJECT_DIR"
mvn package

if curl --silent --fail --max-time 2 http://localhost:8080/ >/dev/null; then
	echo "A API ja esta em execucao em http://localhost:8080"
	exit 0
fi

if ss -ltn 'sport = :8080' | tail -n +2 | grep -q LISTEN; then
	echo "Erro: a porta 8080 ja esta ocupada por outro processo." >&2
	echo "Libere a porta ou encerre o processo que a utiliza antes de executar este script." >&2
	exit 1
fi

exec java -jar "$JAR_PATH"
