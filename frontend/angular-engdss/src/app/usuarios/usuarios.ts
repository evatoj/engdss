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
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    CardModule,
    InputNumberModule,
  ],
  templateUrl: './usuarios.html'
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [{ id: 'Vazio', nome: 'Vazio', saldo: 0 }];
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
    setTimeout(() => {
        this.carregandoUsuarios = false;
        }, 500); // Simula um pequeno atraso para exibir o carregamento

    this.transacaoService.listarUsuarios().subscribe({
      next: (usuarios) => {
        this.transacaoService.usuarios = usuarios; // Atualiza a lista de usuários no serviço
        this.usuarios = usuarios;
        setTimeout(() => {
        this.carregandoUsuarios = false;
        }, 500); // Simula um pequeno atraso para exibir o carregamento
      },
      error: (err) => {
        console.error('Erro ao listar usuários', err);
        setTimeout(() => {
        this.carregandoUsuarios = false;
        }, 500); // Simula um pequeno atraso para exibir o carregamento
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
    setTimeout(() => {
      this.carregandoDetalhes = true;
    }, 500); // Simula um pequeno atraso para exibir o carregamento

    this.transacaoService.consultarSaldoUsuario(usuario.id.toString()).subscribe({
      next: (res: any) => {
        this.saldoUsuario = typeof res === 'number' ? res : res.saldo;
      },
      error: (err) => {
        console.error('Erro ao consultar saldo', err); 
      }
    });

    this.transacaoService.listarTransacoesDoUsuario(usuario.id.toString()).subscribe({
      next: (transacoes) => {
        this.transacoes = transacoes;
        setTimeout(() => {
        this.carregandoDetalhes = false;
        }, 500); // Simula um pequeno atraso para exibir o carregamento
      },
      error: (err) => {
        console.error('Erro ao listar transações', err);
        setTimeout(() => {
        this.carregandoDetalhes = false;
        }, 500); // Simula um pequeno atraso para exibir o carregamento
      }
    });
  }
}