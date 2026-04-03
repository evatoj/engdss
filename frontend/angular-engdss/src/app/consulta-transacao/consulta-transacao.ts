import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';

import { TransacaoService } from '../../../serviços/transacao-service';
import { TransacaoResponse } from '../../../models/transacao_response';

@Component({
  selector: 'app-consulta-transacao',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    InputTextModule
  ],
  templateUrl: './consulta-transacao.html'
})
export class ConsultaTransacaoComponent {
  transacaoId: string | null = null;
  transacao: TransacaoResponse | null = null;

  constructor(private transacaoService: TransacaoService) {}

  consultar(): void {
    if (!this.transacaoId) {
      return;
    }

    this.transacaoService.buscarTransacaoPorId(this.transacaoId).subscribe({
      next: (res) => {
        this.transacao = res;
      },
      error: (err) => {
        console.error('Erro ao buscar transação', err);
        this.transacao = null;
      }
    });
  }
}