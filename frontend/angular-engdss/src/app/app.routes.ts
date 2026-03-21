import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./home/home').then(m => m.Home)
    },
    {
        path: 'nova-transacao',
        loadComponent: () => import('./nova-transacao/nova-transacao').then(m => m.NovaTransacao)
    },
    {  
        path: 'consulta-transacao',
        loadComponent: () => import('./consulta-transacao/consulta-transacao').then(m => m.ConsultaTransacao)
    },
    {
        path: 'historico',
        loadComponent: () => import('./historico/historico').then(m => m.Historico)
    }
];
