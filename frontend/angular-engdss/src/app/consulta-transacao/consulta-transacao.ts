import { Component } from '@angular/core';
import { StatusTransacao } from '../../../models/type_status_transacao';
import { finalize } from 'rxjs';
import { TransacaoResponse } from '../../../models/transacao_response';
import { TransacaoService } from '../../../serviços/transacao-service';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';

@Component({
  selector: 'app-consulta-transacao',
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    CardModule,
    InputTextModule,
    ButtonModule,
    TagModule,
    MessageModule,
    DividerModule,
    SkeletonModule],
  templateUrl: './consulta-transacao.html',
  styleUrl: './consulta-transacao.css',
})
export class ConsultaTransacao {

idTransacao = '';
  loading = false;
  pesquisou = false;

  transacao: TransacaoResponse | null = null;
  mensagemErro = '';

  constructor(private transacaoService: TransacaoService) {}

  consultar(): void {
    const id = this.idTransacao.trim();

    this.pesquisou = true;
    this.transacao = null;
    this.mensagemErro = '';

    if (!id) {
      this.mensagemErro = 'Informe o ID da transação.';
      return;
    }

    this.loading = true;

    this.transacaoService
      .consultarPorId(id)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe((response) => {
        this.transacao = response;
      });
  }

  limpar(): void {
    this.idTransacao = '';
    this.transacao = null;
    this.mensagemErro = '';
    this.pesquisou = false;
  }

  getSeverity(status: StatusTransacao): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'SUCCESS':
        return 'success';
      case 'PENDING':
        return 'warn';
      case 'PROCESSING':
        return 'info';
      case 'FAILED':
        return 'danger';
      case 'CANCELLED':
        return 'secondary';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status: StatusTransacao): string | undefined {
    switch (status) {
      case 'SUCCESS':
        return 'Sucesso';
      case 'PENDING':
        return 'Pendente';
      case 'PROCESSING':
        return 'Processando';
      case 'FAILED':
        return 'Falhou';
      case 'CANCELLED':
        return 'Cancelada';
      default:
        return undefined;
    }
  }
}
