FROM node:20-slim

ENV NODE_ENV=production
WORKDIR /app

COPY services/whatsapp-baileys/package.json ./package.json
RUN npm install --omit=dev

COPY services/whatsapp-baileys/src ./src

EXPOSE 3000
CMD ["npm", "start"]
