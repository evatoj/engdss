import { StatusTransacao } from "./type_status_transacao";

export interface TransacaoResponse {
  id: number;
  usuarioId: number;
  chavePixDestino: string;
  valor: number;
  descricao?: string;
  status: string;
  dataCriacao?: string;
}