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
                icon: 'pi pi-plus',
                routerLink: '/nova-transacao'
            },
            {
                label: 'Consulta Transação',
                icon: 'pi pi-list',
                routerLink: '/consulta-transacao',
            },
            {
                label: 'Histórico',
                icon: 'pi pi-envelope',
                routerLink: '/historico'
            }
        ]
    }
  

  
}
