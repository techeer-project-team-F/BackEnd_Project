# AWS 실서버 환경 (EC2 + RDS)
# 사용 전 BASE_URL, EC2_IP, InfluxDB 인증 정보를 실제 값으로 채우세요
#
# ⚠️ 부하 테스트 대상 서버는 운영 RDS와 분리된 perf-RDS를 가리켜야 합니다.
#    서버 배포 시 SPRING_PROFILES_ACTIVE=prod,prod-perf,perf-seed
#    PERF_DB_URL / PERF_DB_USERNAME / PERF_DB_PASSWORD 환경변수 설정 필수

export BASE_URL="https://your-aws-domain.com"
export CONSTANT_VUS=200
export DURATION=3m
export USER_COUNT=500

# AWS InfluxDB는 인증이 켜져 있음 (docker-compose.prod.yml 참고)
# 형식: http://<EC2_IP>:8086/k6?username=<USER>&password=<PASS>
export INFLUXDB_URL="http://<EC2_IP>:8086/k6?username=admin&password=changeme"

# AWS는 perf-seed 프로파일 없이 운영되므로 체크 건너뜀
export SKIP_PROFILE_CHECK=1
