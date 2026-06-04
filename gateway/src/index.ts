import Fastify from 'fastify'
import { config } from './config'
import { buildRoutes } from './routes'
import { logger } from './logs/logger'
import { SqliteDatabase } from './storage/db'
import { BackendClient } from './backend/client'
import { GatewayService } from './core/gateway'
import { MockAccessDeviceAdapter } from './adapters/MockAccessDeviceAdapter'

const app = Fastify({ logger: logger as any })
const database = new SqliteDatabase(config.storage.databasePath)
const backendClient = new BackendClient(config, logger)
const gatewayService = new GatewayService({ config, database, backendClient, logger })

if (config.environment === 'production') {
  // Fail fast when secrets missing in production
  if (!config.backend.deviceApiKey || !config.backend.deviceHmacSecret) {
    logger.error('Missing DEVICE_API_KEY or DEVICE_HMAC_SECRET in production; refusing to start')
    process.exit(1)
  }
}

// Mock adapter MUST NOT run in production
let adapter
if (config.environment === 'production') {
  logger.error('No physical adapter implementation provided for production')
  process.exit(1)
} else {
  adapter = new MockAccessDeviceAdapter({ logger })
}

buildRoutes(app, { gatewayService, adapter, config, logger })

// Scheduler: periodic sync and heartbeat
let syncInterval: NodeJS.Timeout | null = null
let heartbeatInterval: NodeJS.Timeout | null = null

function startScheduler() {
  const syncSec = gatewayService.config.timing.syncIntervalSeconds
  const hbSec = gatewayService.config.timing.syncIntervalSeconds * 5
  syncInterval = setInterval(async () => {
    try {
      await gatewayService.syncPendingEvents()
    } catch (err) {
      logger.warn({ err }, 'Scheduled sync failed')
    }
  }, syncSec * 1000)

  heartbeatInterval = setInterval(async () => {
    try {
      await gatewayService.sendHeartbeat()
    } catch (err) {
      logger.warn({ err }, 'Scheduled heartbeat failed')
    }
  }, hbSec * 1000)
}

function stopScheduler() {
  if (syncInterval) clearInterval(syncInterval)
  if (heartbeatInterval) clearInterval(heartbeatInterval)
}

const start = async () => {
  try {
    await database.initialize()
    await adapter.start()
    await gatewayService.initialize()
    startScheduler()
    await app.listen({ port: config.gateway.port, host: '0.0.0.0' })
    logger.info({ port: config.gateway.port }, 'Gateway started')
  } catch (error) {
    logger.error({ error }, 'Failed to start gateway')
    process.exit(1)
  }
}

start()

// Graceful shutdown
process.on('SIGINT', async () => {
  logger.info('Shutting down...')
  stopScheduler()
  try {
    await app.close()
    await adapter.stop()
    await database.close()
    process.exit(0)
  } catch (err) {
    logger.error({ err }, 'Error during shutdown')
    process.exit(1)
  }
})
process.on('SIGTERM', () => process.emit('SIGINT'))
