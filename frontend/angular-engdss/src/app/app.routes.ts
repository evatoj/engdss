import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./usuarios/usuarios').then(m => m.UsuariosComponent)
    },
    {
        path: 'nova-transacao',
        loadComponent: () => import('./nova-transacao/nova-transacao').then(m => m.NovaTransacaoComponent)
    },
    {  
        path: 'consulta-transacao',
        loadComponent: () => import('./consulta-transacao/consulta-transacao').then(m => m.ConsultaTransacaoComponent)
    }
];
