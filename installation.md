# Verbinden auf Prod

ssh 168.119.63.209

# VPN

`cat server_private.key`
GNcUSKOsaowVxObvP0l6YsTOGOxFOyC1EonvClVhulU=

`cat server_public.key`
CzNlQVoGX2frHqkeZNCUMQkA2lvHLiOrG0m5JBY8zU0=

`cat client_private.key`
kBv3eytHAcYMTQvjO3m3SR/0XGhLDA6R/jtY1jvZx24=

`cat client_public.key`
GNcR7Eu4ZKXsgAUvW8MFTiri14J8svQGnFt31FWrOFo=

## Wireguard starten

`sudo wg-quick up wg-client`

oder

`sudo wg-quick up ./wg-client.conf`

## Wireguard starten

`sudo wg-quick down wg-client`

oder

`sudo wg-quick down ./wg-client.conf`

## /etc
`sudo cp wg-client.conf /etc/wireguard/wg-client.conf`

`sudo chmod 600 /etc/wireguard/wg-client.conf`


