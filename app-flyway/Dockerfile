# Flyway CLI als Basis
FROM flyway/flyway:11.19.1

# Migrationen liegen zentral im Repo unter db/migration
# WICHTIG: Beim docker build muss der Build-Context das Repo-Root sein
COPY db/migration /flyway/db/migration
ENV FLYWAY_LOCATIONS=filesystem:/flyway/db/migration
