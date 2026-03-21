import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MenubarModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  items: MenuItem[] | undefined;

    ngOnInit() {
        this.items = [
            {
                label: 'Home',
                icon: 'pi pi-home',
                routerLink: '/'
            },
            {
                label: 'Nova Transação',
                icon: 'pi pi-star',
                routerLink: '/nova-transacao'
            },
            {
                label: 'Consulta Transação',
                icon: 'pi pi-search',
                routerLink: '/consulta-transacao',
                items: [
                    {
                        label: 'Components',
                        icon: 'pi pi-bolt'
                    },
                    {
                        label: 'Blocks',
                        icon: 'pi pi-server'
                    },
                    {
                        label: 'UI Kit',
                        icon: 'pi pi-pencil'
                    },
                    {
                        label: 'Templates',
                        icon: 'pi pi-palette',
                        items: [
                            {
                                label: 'Apollo',
                                icon: 'pi pi-palette'
                            },
                            {
                                label: 'Ultima',
                                icon: 'pi pi-palette'
                            }
                        ]
                    }
                ]
            },
            {
                label: 'Histórico',
                icon: 'pi pi-envelope',
                routerLink: '/historico'
            }
        ]
    }
  

  
}
