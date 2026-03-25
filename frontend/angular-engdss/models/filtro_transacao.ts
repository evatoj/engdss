import { StatusTransacao } from "./type_status_transacao";

export interface FiltroTransacao {
  page?: number;
  size?: number;
  status?: StatusTransacao;
  chavePix?: string;
  descricao?: string;
  dataInicio?: string;
  dataFim?: string;
  valorMin?: number;
  valorMax?: number;
  sort?: string;
}