import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

import { Usuario } from '../models/usuario';
import { CriarUsuarioRequest } from '../models/criar_usuario_request';
import { SaldoUsuario } from '../models/saldo_usuario';
import { TransacaoResponse } from '../models/transacao_response';
import { CriarTransacaoRequest } from '../models/criar_transacao_request';
import { UUID } from 'node:crypto';

@Injectable({
  providedIn: 'root'
})
export class TransacaoService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  usuarios: Usuario[] = [];
  transacoes: TransacaoResponse[] = [];

  criarUsuario(payload: CriarUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/usuarios`, payload);
  }

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/usuarios`);
  }

  consultarSaldoUsuario(usuarioId: string): Observable<SaldoUsuario> {
    return this.http.get<SaldoUsuario>(`${this.apiUrl}/usuarios/${usuarioId}/saldo`);
  }

  listarTransacoesDoUsuario(usuarioId: string): Observable<TransacaoResponse[]> {
    return this.http.get<TransacaoResponse[]>(
      `${this.apiUrl}/usuarios/${usuarioId}/transacoes`
    );
  }

  criarTransacao(payload: CriarTransacaoRequest, headers: HttpHeaders): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(`${this.apiUrl}/transacoes`, payload, { headers });
  }

  buscarTransacaoPorId(id: string): Observable<TransacaoResponse> {
    return this.http.get<TransacaoResponse>(`${this.apiUrl}/transacoes/${id}`);
  }

  gerarUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.floor(Math.random() * 16);
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
}