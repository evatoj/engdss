import { StatusTransacao } from "./type_status_transacao";

export interface TransacaoResponse {
  id: string;
  chavePix: string;
  valor: number;
  descricao?: string;
  status: StatusTransacao;
  criadaEm: string;
  atualizadaEm?: string;
  protocolo?: string;
  mensagem?: string;
}