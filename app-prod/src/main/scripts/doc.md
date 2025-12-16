# Backup

```
scp ./backup_prod_db.sh ralf@10.10.0.1:/opt/crmupload-deploy/backup
```

# VPN
```
sudo systemctl enable wg-quick@wg-client
sudo systemctl start wg-quick@wg-client
systemctl status wg-quick@wg-client
```
