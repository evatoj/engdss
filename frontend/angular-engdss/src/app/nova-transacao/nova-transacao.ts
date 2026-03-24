import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { FloatLabelModule } from 'primeng/floatlabel';
import { FormsModule } from '@angular/forms';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { PanelModule } from 'primeng/panel';


interface nova_transacao_form {
  chave: string | null;
  valor: number | null;
  descricao: string | null;
}
@Component({
  selector: 'app-nova-transacao',
  imports: [PanelModule, ToastModule, InputTextModule, InputNumberModule, ButtonModule, FloatLabelModule, FormsModule],
  templateUrl: './nova-transacao.html',
  styleUrl: './nova-transacao.css',
  providers: [MessageService]
})
export class NovaTransacao {

  form: nova_transacao_form = {
    chave: null,
    valor: null,
    descricao: null
  }

  constructor(private messageService: MessageService) {}
  
  enviar_transacao() {
    this.messageService.add({ severity: 'success', summary: 'Transação Enviada', detail: `Valor: ${this.form.valor}, Chave: ${this.form.chave}, Descrição: ${this.form.descricao}` });
  }

}
