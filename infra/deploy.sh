#!/bin/bash
set -e

docker pull arthurbrd/glotrush-back:latest

docker service update --image arthurbrd/glotrush-back:latest --force skaldly_back

docker network inspect monitoring_net >/dev/null 2>&1 \
  || docker network create --driver overlay --attachable monitoring_net

docker service update --network-add monitoring_net skaldly_back || true

docker stack rm monitoring || true
timeout 30 bash -c 'until ! docker stack ls | grep -q "^monitoring "; do sleep 2; done' || true

cd "$(dirname "$0")"
docker stack deploy -c monitoring.yml monitoring
