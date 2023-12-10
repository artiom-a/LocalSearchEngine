FROM mysql/mysql-server:5.7

LABEL author="Artem Abramov"
LABEL description="MySQL Image for LinkDex Skillbox"
LABEL version="1.0"

ENV MYSQL_ALLOW_EMPTY_PASSWORD=false
ENV MYSQL_USER=skillbox_engine
ENV MYSQL_PASSWORD=skillbox_engine
ENV MYSQL_DATABASE=linkdex

COPY ../../src/main/resources/sql_scripts/init_mysql.sql /docker-entrypoint-initdb.d/