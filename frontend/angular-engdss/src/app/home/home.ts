import { Component, inject, OnInit, signal } from '@angular/core';
import { TransacaoResponse } from '../../../models/transacao_response';
import { StatusTransacao } from '../../../models/type_status_transacao';
import { ResumoCard } from '../../../models/resumo_card';
import { finalize, from } from 'rxjs';
import { Router } from '@angular/router';
import { TransacaoService } from '../../../serviços/transacao-service';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';

@Component({
  selector: 'app-home',
  imports: [
    CommonModule,
    CurrencyPipe,
    DatePipe,
    CardModule,
    TableModule,
    TagModule,
    ButtonModule,
    DividerModule,
    SkeletonModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  constructor(private router: Router, private transacaoService: TransacaoService) {}

  // private readonly router = inject(Router);
  // private readonly transacaoService = inject(TransacaoService);

  loading = false;

  saldo = 2500.75;

  transacoesRecentes: TransacaoResponse[] = [];

  resumoCards: ResumoCard[] = [
    {
      titulo: 'Transações hoje',
      valor: 0,
      icone: 'pi pi-bolt',
      classe: 'card-info'
    },
    {
      titulo: 'Sucessos',
      valor: 0,
      icone: 'pi pi-check-circle',
      classe: 'card-success'
    },
    {
      titulo: 'Pendentes',
      valor: 0,
      icone: 'pi pi-clock',
      classe: 'card-warning'
    },
    {
      titulo: 'Falhas',
      valor: 0,
      icone: 'pi pi-times-circle',
      classe: 'card-danger'
    }
  ];

  ngOnInit(): void {
    this.carregarHome();
  }

  carregarHome(): void {
    this.loading = true;

    this.transacaoService
      .listar({
        page: 0,
        size: 5,
        sort: 'criadaEm,desc'
      })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.transacoesRecentes = response.content ?? [];
          this.atualizarResumo(this.transacoesRecentes);
        },
        error: (err) => {
          console.error('Erro ao carregar home', err);
        }
      });
  }

  atualizarResumo(transacoes: TransacaoResponse[]): void {
    const totalHoje = transacoes.length;
    const sucessos = transacoes.filter(t => t.status === 'SUCCESS').length;
    const pendentes = transacoes.filter(
      t => t.status === 'PENDING' || t.status === 'PROCESSING'
    ).length;
    const falhas = transacoes.filter(
      t => t.status === 'FAILED' || t.status === 'CANCELLED'
    ).length;

    this.resumoCards = [
      {
        titulo: 'Transações recentes',
        valor: totalHoje,
        icone: 'pi pi-bolt',
        classe: 'card-info'
      },
      {
        titulo: 'Sucessos',
        valor: sucessos,
        icone: 'pi pi-check-circle',
        classe: 'card-success'
      },
      {
        titulo: 'Pendentes',
        valor: pendentes,
        icone: 'pi pi-clock',
        classe: 'card-warning'
      },
      {
        titulo: 'Falhas',
        valor: falhas,
        icone: 'pi pi-times-circle',
        classe: 'card-danger'
      }
    ];
  }

  irParaNovaTransacao(): void {
    this.router.navigate(['/nova-transacao']);
  }

  irParaHistorico(): void {
    this.router.navigate(['/historico']);
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
