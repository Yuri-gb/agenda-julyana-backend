import express from 'express';
import QRCode from 'qrcode';
import pino from 'pino';
import swaggerUi from 'swagger-ui-express';
import * as baileys from '@whiskeysockets/baileys';
import fs from 'node:fs/promises';
import path from 'node:path';

const makeWASocket = baileys.makeWASocket ?? (typeof baileys.default === 'function' ? baileys.default : (baileys.default?.makeWASocket ?? baileys.default?.default));
const { Browsers, DisconnectReason, fetchLatestBaileysVersion, useMultiFileAuthState } = baileys;

if (typeof makeWASocket !== 'function') {
  throw new TypeError('A biblioteca Baileys não exportou uma função de criação de socket compatível.');
}

const openApiSpec = {
  openapi: '3.0.3',
  info: {
    title: 'Agenda Julyana — WhatsApp Gateway',
    version: '1.0.0',
    description: 'API para verificar a sessão do WhatsApp, obter QR Code e testar envio de mensagens.'
  },
  servers: [{ url: '/' }],
  components: {
    securitySchemes: {
      BearerAuth: { type: 'http', scheme: 'bearer', bearerFormat: 'API_TOKEN' }
    },
    schemas: {
      Error: { type: 'object', properties: { error: { type: 'string' } } },
      SessionStatus: {
        type: 'object',
        properties: {
          status: { type: 'string', example: 'QR_REQUIRED' },
          connected: { type: 'boolean', example: false },
          qrAvailable: { type: 'boolean', example: true },
          lastDisconnect: { nullable: true, type: 'object', properties: {
            statusCode: { type: 'integer', nullable: true },
            message: { type: 'string' }
          } }
        }
      },
      SendMessage: {
        type: 'object', required: ['phone', 'message'],
        properties: {
          phone: { type: 'string', description: 'Telefone com DDI + DDD + número, ex.: 5511999999999', example: '5511999999999' },
          message: { type: 'string', maxLength: 4000, example: 'Olá! Teste do Agenda Julyana.' }
        }
      }
    }
  },
  paths: {
    '/health': {
      get: { summary: 'Verificar se a API está no ar', security: [], responses: {
        '200': { description: 'API disponível', content: { 'application/json': { schema: { type: 'object', properties: { status: { type: 'string' }, service: { type: 'string' } } } } }
      } }
    },
    '/v1/session/status': {
      get: { summary: 'Consultar status da sessão WhatsApp', security: [{ BearerAuth: [] }], responses: {
        '200': { description: 'Status da sessão', content: { 'application/json': { schema: { $ref: '#/components/schemas/SessionStatus' } } },
        '401': { description: 'Token ausente ou inválido' }
      } }
    },
    '/v1/session/start': {
      post: { summary: 'Iniciar ou tentar iniciar a sessão', security: [{ BearerAuth: [] }], responses: {
        '202': { description: 'Solicitação aceita' }, '401': { description: 'Token ausente ou inválido' }, '500': { description: 'Falha ao iniciar sessão' }
      } }
    },
    '/v1/session/qr': {
      get: { summary: 'Obter QR Code para vincular o WhatsApp', security: [{ BearerAuth: [] }], responses: {
        '200': { description: 'QR Code atual', content: { 'application/json': { schema: { type: 'object', properties: { qr: { type: 'string' }, qrCodeDataUrl: { type: 'string', nullable: true, description: 'Data URL da imagem do QR Code' } } } } },
        '404': { description: 'QR ainda não disponível' }, '409': { description: 'WhatsApp já conectado' }, '401': { description: 'Token ausente ou inválido' }
      } }
    },
    '/v1/messages': {
      post: { summary: 'Enviar uma mensagem de texto', security: [{ BearerAuth: [] }], requestBody: {
        required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/SendMessage' } } }
      }, responses: {
        '200': { description: 'Mensagem aceita para envio' }, '400': { description: 'Telefone ou mensagem inválidos' },
        '401': { description: 'Token ausente ou inválido' }, '502': { description: 'Falha no envio' }, '503': { description: 'WhatsApp não conectado' }
      } }
    },
    '/v1/session/logout': {
      post: { summary: 'Desconectar a sessão e apagar credenciais locais', security: [{ BearerAuth: [] }], responses: {
        '200': { description: 'Sessão encerrada' }, '401': { description: 'Token ausente ou inválido' }, '500': { description: 'Falha ao encerrar sessão' }
      } }
    }
  }
};

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '1mb' }));

const PORT = Number(process.env.PORT || 3000);
const API_TOKEN = process.env.API_TOKEN || '';
const AUTH_DIR = process.env.AUTH_DIR || '/var/data/auth_info';
const WEBHOOK_URL = process.env.WEBHOOK_URL || '';
const WEBHOOK_SECRET = process.env.WEBHOOK_SECRET || '';
const logger = pino({ level: process.env.LOG_LEVEL || 'info' });

let sock;
let connection = 'close';
let qrText = null;
let qrDataUrl = null;
let lastDisconnect = null;
let reconnectTimer;
let isStarting = false;

if (!API_TOKEN) {
  logger.error('API_TOKEN não foi definido; encerrando para não expor a API.');
  process.exit(1);
}

function auth(req, res, next) {
  const supplied = req.get('authorization')?.replace(/^Bearer\s+/i, '') || '';
  if (supplied.length !== API_TOKEN.length ||
      !Buffer.from(supplied).equals(Buffer.from(API_TOKEN))) {
    return res.status(401).json({ error: 'Não autorizado' });
  }
  next();
}

async function sendWebhook(event, data) {
  if (!WEBHOOK_URL) return;
  try {
    const response = await fetch(WEBHOOK_URL, {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        ...(WEBHOOK_SECRET ? { 'x-webhook-secret': WEBHOOK_SECRET } : {})
      },
      body: JSON.stringify({ provider: 'baileys', event, data, occurredAt: new Date().toISOString() }),
      signal: AbortSignal.timeout(8000)
    });
    if (!response.ok) logger.warn({ status: response.status, event }, 'Webhook retornou status não-2xx');
  } catch (error) {
    logger.warn({ error: error.message, event }, 'Falha ao enviar webhook');
  }
}

function normalizePhone(value) {
  const digits = String(value || '').replace(/\D/g, '');
  if (digits.length < 10 || digits.length > 15) {
    throw new Error('Telefone inválido. Informe DDI + DDD + número, somente dígitos ou com símbolos.');
  }
  return digits;
}

async function startSocket() {
  if (isStarting || (sock && connection === 'open')) return;
  isStarting = true;
  try {
    await fs.mkdir(AUTH_DIR, { recursive: true });
    const { state, saveCreds } = await useMultiFileAuthState(AUTH_DIR);
    let version;
    try {
      ({ version } = await fetchLatestBaileysVersion());
    } catch (error) {
      logger.warn({ error: error.message }, 'Não foi possível buscar a versão mais recente; usando versão padrão da biblioteca');
    }

    sock = makeWASocket({
      ...(version ? { version } : {}),
      auth: state,
      logger: logger.child({ component: 'baileys' }),
      browser: Browsers.ubuntu('Agenda Julyana'),
      printQRInTerminal: false,
      markOnlineOnConnect: false,
      syncFullHistory: false,
      connectTimeoutMs: 60000,
      defaultQueryTimeoutMs: 60000
    });

    sock.ev.on('creds.update', saveCreds);
    sock.ev.on('connection.update', async (update) => {
      const { connection: nextConnection, lastDisconnect: disconnectInfo, qr } = update;
      if (qr) {
        qrText = qr;
        try { qrDataUrl = await QRCode.toDataURL(qr); } catch { qrDataUrl = null; }
        logger.info('QR Code gerado; consulte GET /v1/session/qr');
      }
      if (nextConnection) connection = nextConnection;
      if (disconnectInfo) {
        lastDisconnect = {
          statusCode: disconnectInfo.error?.output?.statusCode ?? null,
          message: disconnectInfo.error?.message ?? 'Conexão encerrada'
        };
      }
      if (nextConnection === 'open') {
        qrText = null;
        qrDataUrl = null;
        lastDisconnect = null;
        logger.info('WhatsApp conectado');
        await sendWebhook('connection.open', { connected: true });
      } else if (nextConnection === 'close') {
        logger.warn({ lastDisconnect }, 'Conexão WhatsApp encerrada');
        await sendWebhook('connection.close', { connected: false, lastDisconnect });
        const statusCode = disconnectInfo?.error?.output?.statusCode;
        const loggedOut = statusCode === DisconnectReason.loggedOut;
        sock = undefined;
        if (!loggedOut && !reconnectTimer) {
          reconnectTimer = setTimeout(() => {
            reconnectTimer = undefined;
            startSocket().catch(error => logger.error({ error: error.message }, 'Falha ao reconectar'));
          }, 3000);
        }
      }
    });

    sock.ev.on('messages.upsert', async ({ messages, type }) => {
      for (const message of messages || []) {
        if (!message.message || message.key?.fromMe) continue;
        const remoteJid = message.key?.remoteJid || '';
        if (remoteJid.endsWith('@g.us') || remoteJid === 'status@broadcast') continue;
        const textMessage =
          message.message.conversation ||
          message.message.extendedTextMessage?.text ||
          message.message.imageMessage?.caption ||
          message.message.videoMessage?.caption ||
          '';
        await sendWebhook('message.received', {
          type,
          messageId: message.key?.id || null,
          from: remoteJid.replace(/@s\.whatsapp\.net$/, ''),
          pushName: message.pushName || null,
          text: textMessage,
          timestamp: message.messageTimestamp ? Number(message.messageTimestamp) : null
        });
      }
    });
  } finally {
    isStarting = false;
  }
}

app.get('/health', (_req, res) => res.json({ status: 'ok', service: 'agenda-julyana-whatsapp-gateway' }));
app.get('/openapi.json', (_req, res) => res.json(openApiSpec));
app.use('/docs', swaggerUi.serve, swaggerUi.setup(openApiSpec, { explorer: true }));

app.use('/v1', auth);

app.get('/v1/session/status', (_req, res) => {
  res.json({
    status: connection === 'open' ? 'CONNECTED' : qrText ? 'QR_REQUIRED' : connection.toUpperCase(),
    connected: connection === 'open',
    qrAvailable: Boolean(qrText),
    lastDisconnect
  });
});

app.post('/v1/session/start', async (_req, res) => {
  try {
    await startSocket();
    res.status(202).json({ status: connection === 'open' ? 'CONNECTED' : qrText ? 'QR_REQUIRED' : 'STARTING' });
  } catch (error) {
    logger.error({ error: error.message }, 'Não foi possível iniciar sessão');
    res.status(500).json({ error: 'Não foi possível iniciar a sessão WhatsApp' });
  }
});

app.get('/v1/session/qr', (_req, res) => {
  if (connection === 'open') return res.status(409).json({ error: 'WhatsApp já está conectado' });
  if (!qrText) return res.status(404).json({ error: 'QR ainda não disponível; inicie a sessão e tente novamente' });
  res.json({ qr: qrText, qrCodeDataUrl: qrDataUrl });
});

app.post('/v1/messages', async (req, res) => {
  if (connection !== 'open' || !sock) return res.status(503).json({ error: 'WhatsApp não está conectado' });
  const { phone, message } = req.body || {};
  if (typeof message !== 'string' || !message.trim() || message.length > 4000) {
    return res.status(400).json({ error: 'message é obrigatório e deve ter até 4000 caracteres' });
  }
  let number;
  try { number = normalizePhone(phone); } catch (error) {
    return res.status(400).json({ error: error.message });
  }
  try {
    const result = await sock.sendMessage(number + '@s.whatsapp.net', { text: message.trim() });
    res.json({ accepted: true, messageId: result?.key?.id || null, to: number });
  } catch (error) {
    logger.error({ error: error.message }, 'Falha ao enviar mensagem');
    res.status(502).json({ error: 'Falha ao enviar mensagem pelo WhatsApp' });
  }
});

app.post('/v1/session/logout', async (_req, res) => {
  try {
    if (sock) await sock.logout();
    sock = undefined;
    connection = 'close';
    qrText = null;
    qrDataUrl = null;
    await fs.rm(AUTH_DIR, { recursive: true, force: true });
    res.json({ status: 'LOGGED_OUT' });
  } catch (error) {
    logger.error({ error: error.message }, 'Falha ao encerrar sessão');
    res.status(500).json({ error: 'Não foi possível encerrar a sessão' });
  }
});

app.use((error, _req, res, _next) => {
  logger.error({ error: error.message }, 'Erro HTTP não tratado');
  res.status(500).json({ error: 'Erro interno' });
});

const server = app.listen(PORT, '0.0.0.0', () => {
  logger.info({ port: PORT }, 'Gateway WhatsApp iniciado');
  startSocket().catch(error => logger.error({ error: error.message }, 'Falha ao iniciar conexão WhatsApp'));
});

async function shutdown() {
  logger.info('Encerrando gateway');
  clearTimeout(reconnectTimer);
  try { sock?.end(undefined); } catch {}
  server.close(() => process.exit(0));
  setTimeout(() => process.exit(0), 5000).unref();
}
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
