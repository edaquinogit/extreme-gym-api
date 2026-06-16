import dotenv from 'dotenv'
import { join } from 'node:path'
import { z } from 'zod'

dotenv.config({ path: process.env.GATEWAY_ENV_PATH ?? '.env' })

const schema = z.object({
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  GATEWAY_ID: z.string().min(1).default('dev-gateway'),
  GATEWAY_NAME: z.string().min(1).default('dev-gateway'),
  GATEWAY_VERSION: z.string().min(1).default('0.1.0'),
  BACKEND_BASE_URL: z.string().url().default('http://localhost'),
  DEVICE_ID: z.string().min(1).default('dev-device'),
  DEVICE_API_KEY: z.string().min(1).default('dev-api-key'),
  DEVICE_HMAC_SECRET: z.string().min(1).default('dev-hmac-secret'),
  ADMIN_API_KEY: z.string().min(1).default('dev-admin-key'),
  GATEWAY_PORT: z.string().default('4000'),
  SNAPSHOT_REFRESH_INTERVAL_SECONDS: z.string().default('300'),
  SNAPSHOT_TTL_SECONDS: z.string().default('900'),
  OFFLINE_MODE_ENABLED: z.string().default('true'),
  OFFLINE_STRICT_MODE: z.string().default('true'),
  SYNC_INTERVAL_SECONDS: z.string().default('60'),
  SYNC_BATCH_SIZE: z.string().default('50'),
  LOG_LEVEL: z.string().default('info'),
  GATEWAY_DATA_PATH: z.string().default(join(process.cwd(), 'gateway.sqlite')),
})

export type RawConfig = z.infer<typeof schema>
export type GatewayConfig = {
  environment: string
  gateway: {
    id: string
    name: string
    version: string
    port: number
    dataPath: string
  }
  backend: {
    baseUrl: string
    deviceId: string
    deviceApiKey: string
    deviceHmacSecret: string
  }
  admin: {
    apiKey: string
  }
  storage: {
    databasePath: string
  }
  timing: {
    snapshotRefreshSeconds: number
    snapshotTtlSeconds: number
    syncIntervalSeconds: number
    syncBatchSize: number
  }
  feature: {
    offlineMode: boolean
    offlineStrict: boolean
  }
  logger: {
    level: string
  }
}

const parsed = schema.parse(process.env)

export const config = {
  environment: parsed.NODE_ENV,
  gateway: {
    id: parsed.GATEWAY_ID,
    name: parsed.GATEWAY_NAME,
    version: parsed.GATEWAY_VERSION,
    port: Number(parsed.GATEWAY_PORT),
    dataPath: parsed.GATEWAY_DATA_PATH,
  },
  backend: {
    baseUrl: parsed.BACKEND_BASE_URL,
    deviceId: parsed.DEVICE_ID,
    deviceApiKey: parsed.DEVICE_API_KEY,
    deviceHmacSecret: parsed.DEVICE_HMAC_SECRET,
  },
  admin: {
    apiKey: parsed.ADMIN_API_KEY,
  },
  storage: {
    databasePath: parsed.GATEWAY_DATA_PATH,
  },
  timing: {
    snapshotRefreshSeconds: Number(parsed.SNAPSHOT_REFRESH_INTERVAL_SECONDS),
    snapshotTtlSeconds: Number(parsed.SNAPSHOT_TTL_SECONDS),
    syncIntervalSeconds: Number(parsed.SYNC_INTERVAL_SECONDS),
    syncBatchSize: Number(parsed.SYNC_BATCH_SIZE),
  },
  feature: {
    offlineMode: parsed.OFFLINE_MODE_ENABLED === 'true',
    offlineStrict: parsed.OFFLINE_STRICT_MODE === 'true',
  },
  logger: {
    level: parsed.LOG_LEVEL,
  },
}
