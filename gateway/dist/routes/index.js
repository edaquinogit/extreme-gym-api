import { healthRoutes } from './health';
import { statusRoutes } from './status';
import { devRoutes } from './dev';
export function buildRoutes(app, deps) {
    healthRoutes(app);
    statusRoutes(app, deps.gatewayService);
    if (deps.config.environment === 'development') {
        devRoutes(app, deps.logger);
    }
}
