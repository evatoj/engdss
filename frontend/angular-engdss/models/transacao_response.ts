import { StatusTransacao } from "./type_status_transacao";

export interface TransacaoResponse {
  id: string;
  usuarioId: string;
  chavePixDestino: string;
  valor: number;
  descricao?: string;
  status: string;
  dataCriacao?: string;
}