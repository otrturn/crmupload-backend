FROM eclipse-temurin:21-jre

ARG APP_UID=1000
ARG APP_GID=1000

RUN set -eux; \
    # Gruppe: existiert GID schon -> Name ermitteln, sonst neu anlegen
    if getent group "${APP_GID}" >/dev/null; then \
      APP_GROUP="$(getent group "${APP_GID}" | cut -d: -f1)"; \
    else \
      groupadd -g "${APP_GID}" appuser; \
      APP_GROUP="appuser"; \
    fi; \
    # User: existiert UID schon -> ok, sonst anlegen
    if getent passwd "${APP_UID}" >/dev/null; then \
      APP_USER="$(getent passwd "${APP_UID}" | cut -d: -f1)"; \
    else \
      useradd -u "${APP_UID}" -g "${APP_GID}" -s /usr/sbin/nologin -M appuser; \
      APP_USER="appuser"; \
    fi; \
    mkdir -p /app /data/invoices /data/tmp; \
    chown -R "${APP_UID}:${APP_GID}" /app /data/invoices /data/tmp

WORKDIR /app
COPY target/*.jar /app/app-billing.jar

USER 1000:1000
ENTRYPOINT ["java","-jar","/app/app-billing.jar"]
