import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection} from '@angular/core';
import {provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling} from '@angular/router';

import {routes} from './app.routes';
import {HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi} from '@angular/common/http';
import {AuthInterceptor} from './core/interceptor/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({eventCoalescing: true}),
      provideRouter(
          routes,
          withInMemoryScrolling({
              anchorScrolling: 'enabled',
              scrollPositionRestoration: 'enabled',
          }),
          withEnabledBlockingInitialNavigation()
      ),
      provideHttpClient(
          withFetch(),
          withInterceptorsFromDi()
      ),
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ]
};
