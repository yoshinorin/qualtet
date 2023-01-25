setx QUALTET_DB_DATASOURCE_URL "jdbc:mariadb://127.0.0.1/qualtet?useUnicode=true&characterEncoding=utf8mb4"
setx QUALTET_DB_USER "root"
setx QUALTET_DB_PASSWORD "pass"

setx QUALTET_HTTP_BIND_ADDRESS "0.0.0.0"
setx QUALTET_HTTP_PORT 9001

setx QUALTET_JWT_ISS "http://localhost:9001"
setx QUALTET_JWT_AUD "qualtet_dev_1111"

setx QUALTET_CORS_ALLOW_ORIGINS.0 = "http://localhost:8080"
setx QUALTET_CORS_ALLOW_ORIGINS.1 = "http://127.0.0.1:8080"
setx QUALTET_CORS_ALLOW_ORIGINS.2 = "http://localhost:3000"
setx QUALTET_CORS_ALLOW_ORIGINS.3 = "http://127.0.0.1:8080"
