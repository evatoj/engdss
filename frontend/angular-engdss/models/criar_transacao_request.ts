export interface CriarTransacaoRequest {
  chavePix: string;
  valor: number;
  descricao?: string;
  idempotencyKey?: string;
}