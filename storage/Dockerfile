FROM postgres:12.2-alpine
# overridden in live environment
ENV POSTGRES_PASSWORD velocorner
ENV POSTGRES_USER velocorner
ENV POSTGRES_DB velocorner

COPY create-databases.sh /docker-entrypoint-initdb.d/create-databases.sh
