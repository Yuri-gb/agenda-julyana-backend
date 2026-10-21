import { BufferJSON, initAuthCreds, proto } from '@whiskeysockets/baileys';

/**
 * PostgreSQL-backed Baileys auth state.
 * Both credentials and Signal/key-store entries are required to restore a session.
 */
export async function usePostgresAuthState(pool, sessionId = 'agenda-julyana') {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS baileys_auth_state (
      session_id TEXT NOT NULL,
      key_id TEXT NOT NULL,
      value TEXT NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      PRIMARY KEY (session_id, key_id)
    )
  `);

  const serialize = (value) => JSON.stringify(value, BufferJSON.replacer);
  const deserialize = (value) => JSON.parse(value, BufferJSON.reviver);

  const readValue = async (keyId) => {
    const { rows } = await pool.query(
      'SELECT value FROM baileys_auth_state WHERE session_id = $1 AND key_id = $2',
      [sessionId, keyId]
    );
    return rows.length ? deserialize(rows[0].value) : null;
  };

  const writeValue = async (keyId, value) => {
    await pool.query(
      `INSERT INTO baileys_auth_state (session_id, key_id, value, updated_at)
       VALUES ($1, $2, $3, NOW())
       ON CONFLICT (session_id, key_id)
       DO UPDATE SET value = EXCLUDED.value, updated_at = NOW()`,
      [sessionId, keyId, serialize(value)]
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
    async saveCreds() {
      await writeValue('creds', creds);
    },
    async clear() {
      await pool.query('DELETE FROM baileys_auth_state WHERE session_id = $1', [sessionId]);
    }
  };
}
