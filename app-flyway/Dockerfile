# Flyway CLI als Basis
FROM flyway/flyway:10

# Migrationen liegen zentral im Repo unter db/migration
# WICHTIG: Beim docker build muss der Build-Context das Repo-Root sein
COPY db/migration /flyway/sql