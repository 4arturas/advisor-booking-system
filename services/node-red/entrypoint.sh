#!/bin/sh
cp /default-flows.json /data/flows.json
echo "Seeded /data/flows.json from default"
cp /default-settings.js /data/settings.js
echo "Seeded /data/settings.js from default"
rm -rf /data/ui
cp -r /default-ui /data/ui
echo "Seeded /data/ui from default"
node <<'NODE'
const fs = require('fs');

const flowsPath = '/data/flows.json';
const host = process.env.REDIS_HOST || 'redis';
const port = Number(process.env.REDIS_PORT || '6379');

const flows = JSON.parse(fs.readFileSync(flowsPath, 'utf8'));
let updated = false;

for (const node of flows) {
  if (node && node.type === 'redis-config' && typeof node.options === 'string') {
    const options = JSON.parse(node.options);
    if (options.host !== host || options.port !== port) {
      options.host = host;
      options.port = port;
      node.options = JSON.stringify(options);
      updated = true;
    }
  }
}

if (updated) {
  fs.writeFileSync(flowsPath, JSON.stringify(flows, null, 4) + '\n');
  console.log('Patched Redis flow host from REDIS_HOST/REDIS_PORT');
}
NODE
/usr/src/node-red/entrypoint.sh "$@" &
NR_PID=$!
echo "Waiting for Node-RED to start..."
node /deploy-flows.js
echo "Flows deployed, Node-RED is ready"
wait $NR_PID
