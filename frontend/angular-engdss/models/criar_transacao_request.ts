export interface CriarTransacaoRequest {
  usuarioId: string;
  chavePixDestino: string;
  valor: number;
  descricao?: string;
}