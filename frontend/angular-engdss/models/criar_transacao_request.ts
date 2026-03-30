export interface CriarTransacaoRequest {
  usuarioId: number;
  chavePixDestino: string;
  valor: number;
  descricao?: string;
}