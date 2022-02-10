docker/db:
	docker run -d \
	-e MYSQL_ROOT_PASSWORD=pass \
	-e MYSQL_USER=qualtet \
	-e MYSQL_PASSWORD=pass \
	-e MYSQL_DATABASE=qualtet \
	--expose 3306 \
	-v tmpdata:/var/lib/mysql \
	yoshinorin/docker-mariadb:10.6.3 \
	--character-set-server=utf8mb4
