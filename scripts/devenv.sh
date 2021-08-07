#!/bin/sh

export QUALTET_DB_DATASOURCE_URL="jdbc:mariadb://127.0.0.1/qualtet?useUnicode=true&characterEncoding=utf8mb4"
export QUALTET_DB_USER="root"
export QUALTET_DB_PASSWORD="pass"

export QUALTET_HTTP_BIND_ADDRESS="0.0.0.0"
export QUALTET_HTTP_PORT=9001

export QUALTET_JWT_ISS="http://localhost:9001"
export QUALTET_JWT_AUD="qualtet_dev_1111"
