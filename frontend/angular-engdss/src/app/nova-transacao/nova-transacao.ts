import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';

import { TransacaoService } from '../../../serviços/transacao-service';
import { TransacaoResponse } from '../../../models/transacao_response';
import { Select, SelectModule } from 'primeng/select';
import { Usuario } from '../../../models/usuario';
import { HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-nova-transacao',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    CardModule,
    SelectModule,
    Select
  ],
  templateUrl: './nova-transacao.html'
})
export class NovaTransacaoComponent {
  usuario: Usuario | undefined = {
    id: '',
    nome: 'Vazio',
    saldo: 0
  };
  chavePixDestino = '';
  valor: number | null = null;
  descricao = '';

  resposta: TransacaoResponse | null = null;

  usuarios: Usuario[] | null = [];

  constructor(private transacaoService: TransacaoService) {}

  ngOnInit(): void {
    this.usuarios = this.transacaoService.usuarios; // Obtém a lista de usuários do serviço

    if (!this.usuarios || this.usuarios.length === 0) {
      // Se a lista de usuários estiver vazia, carregue os usuários do backend
      this.transacaoService.listarUsuarios().subscribe({
        next: (usuarios) => {
          this.usuarios = usuarios;
          this.transacaoService.usuarios = usuarios; // Atualiza a lista de usuários no serviço
        },
        error: (err) => {
          console.error('Erro ao listar usuários', err);
        }
      });
    }
  }

  enviar(): void {
    if (!this.usuario || !this.chavePixDestino.trim() || this.valor === null) {
      console.log("Não funciona!");
      console.log("Usuário:", this.usuario);
      console.log("Chave PIX destino:", this.chavePixDestino);
      console.log("Valor:", this.valor);
      return;
    }

    const headers : HttpHeaders = new HttpHeaders({
      'Idempotency-Key': this.transacaoService.gerarUUID() // Gera um UUID para a chave de idempotência
    });

    this.transacaoService.criarTransacao({
      usuarioId: this.usuario?.id,
      chavePixDestino: this.chavePixDestino,
      valor: this.valor,
      descricao: this.descricao,
    },
    headers
  ).subscribe({
      next: (res) => {
        this.resposta = res;
      },
      error: (err) => {
        console.error('Erro ao criar transação', err);
      }
    });
  }
}