cd /home/ralf/IdeaProjects/crmupload/ || exit

rsync -avz ./db-init/ ralf@server:/opt/crmupload-deploy/db-init/
rsync -avz ./app-prod/docker-compose.prod.yml ralf@server:/opt/crmupload-deploy/docker-compose.prod.yml
rsync -avz ./app-prod/src/main/scripts/deploy-on-server.sh ralf@server:/opt/crmupload-deploy/deploy.sh
