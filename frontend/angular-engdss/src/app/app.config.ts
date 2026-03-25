import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideAnimations } from '@angular/platform-browser/animations';
import { providePrimeNG } from 'primeng/config';
import MyPreset from './mypreset';
import { provideHttpClient, withFetch } from '@angular/common/http';
import localeBr from '@angular/common/locales/pt';

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimationsAsync(),
        providePrimeNG({
            theme: {
                preset: MyPreset,
                options: {
                  darkModeSelector: false || 'none',
                  ripple: true
                }
            }
        }),
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    { provide: localeBr, useValue: 'pt-BR' } 
    // provideClientHydration(withEventReplay())
  ]
};
