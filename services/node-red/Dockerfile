FROM nodered/node-red:latest

USER root
RUN npm install --no-audit --no-fund \
        node-red-contrib-postgresql \
        node-red-contrib-prometheus-exporter \
        node-red-contrib-redis \
        node-red-contrib-swagger \
        node-red-debugger \
        @villanoo/node-red-contrib-amqp \
        @digital-tvilling/node-red-openapi-generator
COPY services/node-red/entrypoint.sh /custom-entrypoint.sh
COPY services/node-red/settings.js /default-settings.js
COPY services/node-red/node-red-flows.json /default-flows.json
COPY services/node-red/deploy-flows.js /deploy-flows.js
COPY services/node-red/ui /default-ui
RUN chmod +x /custom-entrypoint.sh && chown node-red:node-red /custom-entrypoint.sh /deploy-flows.js && chown -R node-red:node-red /default-ui
USER node-red
ENTRYPOINT ["/custom-entrypoint.sh"]
