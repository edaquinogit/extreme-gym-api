import pino from 'pino'
import { config } from '../config'

export const logger = pino({
  level: config.logger.level,
  redact: {
    paths: ['backend.deviceApiKey', 'backend.deviceHmacSecret', 'error.stack'],
    censor: '***',
  },
  serializers: {
    err: pino.stdSerializers.err,
  },
})
