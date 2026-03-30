import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';

import { TransacaoService } from '../../../serviços/transacao-service';
import { Usuario } from '../../../models/usuario';
import { TransacaoResponse } from '../../../models/transacao_response';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    CardModule
  ],
  templateUrl: './usuarios.html'
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  transacoes: TransacaoResponse[] = [];

  nome = '';
  saldoInicial: number | null = null;

  usuarioSelecionado: Usuario | null = null;
  saldoUsuario: number | null = null;

  carregandoUsuarios = false;
  carregandoDetalhes = false;

  constructor(private transacaoService: TransacaoService) {}

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    this.carregandoUsuarios = true;

    this.transacaoService.listarUsuarios().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.carregandoUsuarios = false;
      },
      error: (err) => {
        console.error('Erro ao listar usuários', err);
        this.carregandoUsuarios = false;
      }
    });
  }

  criarUsuario(): void {
    if (!this.nome.trim() || this.saldoInicial === null) {
      return;
    }

    this.transacaoService.criarUsuario({
      nome: this.nome,
      saldoInicial: this.saldoInicial
    }).subscribe({
      next: () => {
        this.nome = '';
        this.saldoInicial = null;
        this.carregarUsuarios();
      },
      error: (err) => {
        console.error('Erro ao criar usuário', err);
      }
    });
  }

  selecionarUsuario(usuario: Usuario): void {
    this.usuarioSelecionado = usuario;
    this.transacoes = [];
    this.saldoUsuario = null;
    this.carregandoDetalhes = true;

    this.transacaoService.consultarSaldoUsuario(usuario.id).subscribe({
      next: (res: any) => {
        this.saldoUsuario = typeof res === 'number' ? res : res.saldo;
      },
      error: (err) => {
        console.error('Erro ao consultar saldo', err);
      }
    });

    this.transacaoService.listarTransacoesDoUsuario(usuario.id).subscribe({
      next: (transacoes) => {
        this.transacoes = transacoes;
        this.carregandoDetalhes = false;
      },
      error: (err) => {
        console.error('Erro ao listar transações', err);
        this.carregandoDetalhes = false;
      }
    });
  }
}