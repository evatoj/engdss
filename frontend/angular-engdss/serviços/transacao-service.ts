import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, delay, Observable, of } from 'rxjs';
import { CancelarTransacaoRequest } from '../models/cancelar_transacao_request';
import { PageResponse } from '../models/page_response';
import { FiltroTransacao } from '../models/filtro_transacao';
import { TransacaoResponse } from '../models/transacao_response';
import { CriarTransacaoRequest } from '../models/criar_transacao_request';






@Injectable({
  providedIn: 'root'
})
export class TransacaoService {
  private readonly http = inject(HttpClient);

  // Pode trocar por environment.apiUrl quando configurar ambiente
  private readonly baseUrl = 'http://localhost:8080/api/transacoes';

  criar(payload: CriarTransacaoRequest): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(this.baseUrl, payload);
  }

  consultarPorId(id: string): Observable<TransacaoResponse> {
    return this.http.get<TransacaoResponse>(`${this.baseUrl}/${id}`).pipe(
    catchError(() =>
      of({
        id: '',
        content: [],
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
        chavePix: '',
        valor: 0,
        descricao: '',
        status: null,
        criadaEm: '',
      })
    ));
  }

  listar(filtro?: FiltroTransacao): Observable<PageResponse<TransacaoResponse>> {
    const params = this.buildHttpParams(filtro);

    return this.http.get<PageResponse<TransacaoResponse>>(this.baseUrl, {
      params
    }).pipe(
    catchError(() =>
      of({
        content: [],
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true
      }).pipe(delay(4))
    ));
  }

  listarRecentes(limit = 5): Observable<TransacaoResponse[]> {
    const params = new HttpParams()
      .set('page', 0)
      .set('size', limit)
      .set('sort', 'criadaEm,desc');

    return this.http.get<TransacaoResponse[]>(`${this.baseUrl}/recentes`, {
      params
    });
  }

  cancelar(
    id: string,
    payload?: CancelarTransacaoRequest
  ): Observable<TransacaoResponse> {
    return this.http.patch<TransacaoResponse>(
      `${this.baseUrl}/${id}/cancelar`,
      payload ?? {}
    );
  }

  reprocessar(id: string): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(
      `${this.baseUrl}/${id}/reprocessar`,
      {}
    );
  }

  consultarPorIdempotencyKey(idempotencyKey: string): Observable<TransacaoResponse> {
    const params = new HttpParams().set('idempotencyKey', idempotencyKey);

    return this.http.get<TransacaoResponse>(`${this.baseUrl}/idempotencia`, {
      params
    });
  }

  private buildHttpParams(filtro?: FiltroTransacao): HttpParams {
    let params = new HttpParams();

    if (!filtro) {
      return params;
    }

    Object.entries(filtro).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });

    return params;
  }
}