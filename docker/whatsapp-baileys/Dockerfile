FROM node:20-slim

ENV NODE_ENV=production
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends git ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY services/whatsapp-baileys/package.json ./package.json
RUN npm install --omit=dev

COPY services/whatsapp-baileys/src ./src

EXPOSE 3000
CMD ["npm", "start"]
