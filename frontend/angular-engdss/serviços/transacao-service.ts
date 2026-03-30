import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

import { Usuario } from '../models/usuario';
import { CriarUsuarioRequest } from '../models/criar_usuario_request';
import { SaldoUsuario } from '../models/saldo_usuario';
import { TransacaoResponse } from '../models/transacao_response';
import { CriarTransacaoRequest } from '../models/criar_transacao_request';

@Injectable({
  providedIn: 'root'
})
export class TransacaoService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  criarUsuario(payload: CriarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/usuarios`, payload);
  }

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/usuarios`);
  }

  consultarSaldoUsuario(usuarioId: number): Observable<SaldoUsuario> {
    return this.http.get<SaldoUsuario>(`${this.apiUrl}/usuarios/${usuarioId}/saldo`);
  }

  listarTransacoesDoUsuario(usuarioId: number): Observable<TransacaoResponse[]> {
    return this.http.get<TransacaoResponse[]>(
      `${this.apiUrl}/usuarios/${usuarioId}/transacoes`
    );
  }

  criarTransacao(payload: CriarTransacaoRequest): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(`${this.apiUrl}/transacoes`, payload);
  }

  buscarTransacaoPorId(id: number): Observable<TransacaoResponse> {
    return this.http.get<TransacaoResponse>(`${this.apiUrl}/transacoes/${id}`);
  }
}