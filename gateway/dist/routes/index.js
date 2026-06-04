import { healthRoutes } from './health';
import { statusRoutes } from './status';
import { devRoutes } from './dev';
import { adminRoutes } from './admin';
export function buildRoutes(app, deps) {
    healthRoutes(app);
    statusRoutes(app, deps.gatewayService);
    if (deps.config.environment === 'development') {
        devRoutes(app, deps.logger);
    }
    // Admin routes enabled in all environments but protected by API key
    adminRoutes(app, { gatewayService: deps.gatewayService, config: deps.config });
}
