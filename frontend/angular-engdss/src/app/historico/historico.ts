import { Component, OnInit } from '@angular/core';
import { TransacaoResponse } from '../../../models/transacao_response';
import { FiltroTransacao } from '../../../models/filtro_transacao';
import { TransacaoService } from '../../../serviços/transacao-service';
import { TableLazyLoadEvent } from 'primeng/types/table';
import { StatusTransacao } from '../../../models/type_status_transacao';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';
@Component({
  selector: 'app-historico',
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    TableModule,
    CardModule,
    TagModule,
    ButtonModule,
    SelectModule,
    InputTextModule,
    DividerModule,
    SkeletonModule
    
  ],
  templateUrl: './historico.html',
  styleUrl: './historico.css',
})
export class Historico {
  transacoes: TransacaoResponse[] = [];
  loading = false;
  totalRecords = 0;

  rows = 10;
  first = 0;

  filtros: FiltroTransacao = {
    page: 0,
    size: 10,
    sort: 'criadaEm,desc'
  };

  statusOptions = [
    { label: 'Todos', value: null },
    { label: 'Sucesso', value: 'SUCCESS' },
    { label: 'Pendente', value: 'PENDING' },
    { label: 'Processando', value: 'PROCESSING' },
    { label: 'Falhou', value: 'FAILED' },
    { label: 'Cancelada', value: 'CANCELLED' }
  ];

  constructor(private transacaoService: TransacaoService) {}

  carregarTransacoes(): void {
    this.loading = true;

    this.transacaoService.listar(this.filtros).subscribe({
      next: (response) => {
        this.transacoes = response.content ?? [];
        this.totalRecords = response.totalElements ?? 0;
        this.loading = false;
      },
      error: () => {
        this.transacoes = [];
        this.totalRecords = 0;
        this.loading = false;
      }
    });
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    const first = event.first ?? 0;
    const rows = event.rows ?? 10;

    this.first = first;
    this.rows = rows;

    this.filtros.page = Math.floor(first / rows);
    this.filtros.size = rows;

    this.carregarTransacoes();
  }

  aplicarFiltros(): void {
    this.first = 0;
    this.filtros.page = 0;
    this.filtros.size = this.rows;

    queueMicrotask(() => {
      this.carregarTransacoes();
    });
  }

  limparFiltros(): void {
    this.first = 0;
    this.filtros = {
      page: 0,
      size: this.rows,
      sort: 'criadaEm,desc'
    };

    queueMicrotask(() => {
      this.carregarTransacoes();
    });
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