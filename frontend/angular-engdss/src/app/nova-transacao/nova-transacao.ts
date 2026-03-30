import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';

import { TransacaoService } from '../../../serviços/transacao-service';
import { TransacaoResponse } from '../../../models/transacao_response';

@Component({
  selector: 'app-nova-transacao',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    CardModule
  ],
  templateUrl: './nova-transacao.html'
})
export class NovaTransacaoComponent {
  usuarioId: number | null = null;
  chavePixDestino = '';
  valor: number | null = null;
  descricao = '';

  resposta: TransacaoResponse | null = null;

  constructor(private transacaoService: TransacaoService) {}

  enviar(): void {
    if (!this.usuarioId || !this.chavePixDestino.trim() || this.valor === null) {
      return;
    }

    this.transacaoService.criarTransacao({
      usuarioId: this.usuarioId,
      chavePixDestino: this.chavePixDestino,
      valor: this.valor,
      descricao: this.descricao
    }).subscribe({
      next: (res) => {
        this.resposta = res;
      },
      error: (err) => {
        console.error('Erro ao criar transação', err);
      }
    });
  }
}