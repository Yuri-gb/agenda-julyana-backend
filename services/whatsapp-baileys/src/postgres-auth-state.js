import baileys from '@whiskeysockets/baileys';
const { BufferJSON, initAuthCreds, proto } = baileys;
import { createCipheriv, createDecipheriv, randomBytes } from 'node:crypto';

const ENVELOPE_PREFIX = 'enc:v1:';

function loadEncryptionKey() {
  const raw = process.env.AUTH_ENCRYPTION_KEY || '';
  if (!/^[a-fA-F0-9]{64}$/.test(raw)) {
    throw new Error('AUTH_ENCRYPTION_KEY é obrigatória no modo PostgreSQL e deve conter exatamente 64 caracteres hexadecimais (32 bytes).');
  }
  return Buffer.from(raw, 'hex');
}

/** Encrypt serialized Baileys auth values with AES-256-GCM before writing to PostgreSQL. */
function createValueCodec(key) {
  return {
    encrypt(value) {
      const iv = randomBytes(12);
      const cipher = createCipheriv('aes-256-gcm', key, iv);
      const plaintext = Buffer.from(JSON.stringify(value, BufferJSON.replacer), 'utf8');
      const ciphertext = Buffer.concat([cipher.update(plaintext), cipher.final()]);
      const tag = cipher.getAuthTag();
      return ENVELOPE_PREFIX + [iv, tag, ciphertext].map(part => part.toString('base64')).join(':');
    },
    decrypt(envelope) {
      if (typeof envelope !== 'string' || !envelope.startsWith(ENVELOPE_PREFIX)) {
        throw new Error('Registro de autenticação sem envelope criptográfico válido. Não é seguro carregá-lo.');
      }
      const parts = envelope.slice(ENVELOPE_PREFIX.length).split(':');
      if (parts.length !== 3) throw new Error('Registro de autenticação criptografado está malformado.');
      const [iv, tag, ciphertext] = parts.map(part => Buffer.from(part, 'base64'));
      if (iv.length !== 12 || tag.length !== 16) throw new Error('Metadados criptográficos inválidos.');
      const decipher = createDecipheriv('aes-256-gcm', key, iv);
      decipher.setAuthTag(tag);
      const plaintext = Buffer.concat([decipher.update(ciphertext), decipher.final()]).toString('utf8');
      return JSON.parse(plaintext, BufferJSON.reviver);
    }
  };
}

/**
 * PostgreSQL-backed Baileys auth state.
 * Credentials and Signal/key-store entries are encrypted at rest with AES-256-GCM.
 */
export async function usePostgresAuthState(pool, sessionId = 'agenda-julyana') {
  const codec = createValueCodec(loadEncryptionKey());
  await pool.query(`
    CREATE TABLE IF NOT EXISTS baileys_auth_state (
      session_id TEXT NOT NULL,
      key_id TEXT NOT NULL,
      value TEXT NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      PRIMARY KEY (session_id, key_id)
    )
  `);

  const readValue = async (keyId) => {
    const { rows } = await pool.query(
      'SELECT value FROM baileys_auth_state WHERE session_id = $1 AND key_id = $2',
      [sessionId, keyId]
    );
    return rows.length ? codec.decrypt(rows[0].value) : null;
  };

  const writeValue = async (keyId, value) => {
    const encrypted = codec.encrypt(value);
    await pool.query(
      `INSERT INTO baileys_auth_state (session_id, key_id, value, updated_at)
       VALUES ($1, $2, $3, NOW())
       ON CONFLICT (session_id, key_id)
       DO UPDATE SET value = EXCLUDED.value, updated_at = NOW()`,
      [sessionId, keyId, encrypted]
    );
  };

  const storedCreds = await readValue('creds');
  const creds = storedCreds || initAuthCreds();

  const keys = {
    async get(type, ids) {
      const result = {};
      await Promise.all(ids.map(async (id) => {
        const value = await readValue(`key:${type}:${id}`);
        if (value !== null) result[id] = value;
      }));
      if (type === 'app-state-sync-key') {
        for (const [id, value] of Object.entries(result)) {
          result[id] = proto.Message.AppStateSyncKeyData.fromObject(value);
        }
      }
      return result;
    },
    async set(data) {
      const operations = [];
      for (const [type, entries] of Object.entries(data || {})) {
        for (const [id, value] of Object.entries(entries || {})) {
          const keyId = `key:${type}:${id}`;
          if (value === null || value === undefined) {
            operations.push(pool.query(
              'DELETE FROM baileys_auth_state WHERE session_id = $1 AND key_id = $2',
              [sessionId, keyId]
            ));
          } else {
            operations.push(writeValue(keyId, value));
          }
        }
      }
      await Promise.all(operations);
    }
  };

  return {
    state: { creds, keys },
    async saveCreds() { await writeValue('creds', creds); },
    async clear() {
      await pool.query('DELETE FROM baileys_auth_state WHERE session_id = $1', [sessionId]);
    }
  };
}
