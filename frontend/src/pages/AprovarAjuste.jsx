import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { ajusteService, usuarioService } from "../services/api";
import { ProtectedRoute } from "../components/ProtectedRoute";
import { Clock, User, AlertCircle, CheckCircle2, Loader2 } from "lucide-react";

const STATUS_COR = {
  pendente: "text-amber-700 bg-amber-100 border-amber-200",
  aprovado: "text-green-700 bg-green-100 border-green-200",
  rejeitado: "text-red-700 bg-red-100 border-red-200",
};

const TIPO_LABEL = {
  entrada: "Entrada",
  saida_intervalo: "Saída Intervalo",
  retorno_intervalo: "Retorno Intervalo",
  saida: "Saída Final",
};

export default function AprovarAjuste() {
  const { user } = useAuth();
  const [ajustes, setAjustes] = useState([]);
  const [usuarios, setUsuarios] = useState({});
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [carregando, setCarregando] = useState(false);
  const [processando, setProcessando] = useState({});

  useEffect(() => {
    carregarDados();
  }, []);

  async function carregarDados() {
    setCarregando(true);
    try {
      // Carrega ajustes pendentes e a lista de usuarios para exibir o nome do funcionario
      const [aResp, uResp] = await Promise.all([
        ajusteService.listarPendentes(),
        usuarioService.listar(),
      ]);
      setAjustes(aResp.data || []);
      const mapa = {};
      (uResp.data || []).forEach((u) => {
        mapa[u.id] = u.nome;
      });
      setUsuarios(mapa);
    } catch (err) {
      setErro(
        err.response?.data?.detail || "Erro ao carregar ajustes pendentes"
      );
    } finally {
      setCarregando(false);
    }
  }

  async function handleAprovarRejeitar(ajusteId, novoStatus, observacao = "") {
    setProcessando({ ...processando, [ajusteId]: true });
    try {
      await ajusteService.atualizarStatus(ajusteId, novoStatus, observacao);
      setSucesso(
        `Ajuste ${novoStatus === "aprovado" ? "aprovado" : "rejeitado"} com sucesso!`
      );
      setAjustes((prev) =>
        prev.map((a) =>
          a.id === ajusteId ? { ...a, status: novoStatus, observacao_adm: observacao } : a
        )
      );
    } catch (err) {
      setErro(
        err.response?.data?.detail ||
          `Erro ao ${novoStatus === "aprovado" ? "aprovar" : "rejeitar"} ajuste`
      );
    } finally {
      const { [ajusteId]: _, ...rest } = processando;
      setProcessando(rest);
    }
  }

  const nomeUsuario = (usuarioId) => usuarios[usuarioId] || "Funcionário";

  if (carregando) {
    return (
      <ProtectedRoute onlyAdm>
        <div className="flex h-full items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        </div>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute onlyAdm>
      <div className="max-w-5xl mx-auto">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <User size={24} /> Ajustes Pendentes
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Solicitacoes aguardando analise do ADM
            </p>
          </div>
          <span className="px-3 py-1 rounded-full bg-amber-100 text-amber-700 text-xs font-medium border border-amber-200">
            {ajustes.length} pendente(s)
          </span>
        </div>

        {erro && (
          <div className="mb-4 p-3 rounded bg-red-50 text-red-700 text-sm border border-red-100">
            {erro}
          </div>
        )}
        {sucesso && (
          <div className="mb-4 p-3 rounded bg-green-50 text-green-700 text-sm border border-green-100">
            {sucesso}
          </div>
        )}

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Funcionario</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Tipo solicitado</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Data solicitada</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Justificativa</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Solicitado em</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase text-right">Acao</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {ajustes.length > 0 ? (
                  ajustes.map((a) => (
                    <tr key={a.id}>
                      <td className="px-6 py-4 text-sm font-medium text-gray-700">
                        {nomeUsuario(a.usuario_id)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-700">
                        {TIPO_LABEL[a.tipo_solicitado] || a.tipo_solicitado}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-700">
                        {new Date(a.data_solicitada).toLocaleDateString("pt-BR")}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {a.justificativa}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {a.criado_em
                          ? new Date(a.criado_em).toLocaleString("pt-BR", {
                              hour12: false,
                            })
                          : "-"}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() =>
                              handleAprovarRejeitar(a.id, "aprovado")
                            }
                            disabled={processando[a.id]}
                            className={`px-3 py-1.5 text-xs font-medium rounded-md border transition-colors disabled:opacity-50 ${
                              processando[a.id]
                                ? "bg-gray-200 text-gray-400 border-gray-200"
                                : "bg-green-600 text-white border-green-600 hover:bg-green-700"
                            }`}
                          >
                            {processando[a.id] ? "..." : "Aprovar"}
                          </button>
                          <button
                            onClick={() => {
                              const obs = window.prompt(
                                "Observacao da rejeicao (opcional):",
                                ""
                              );
                              if (obs === null) return; // usuario cancelou
                              handleAprovarRejeitar(a.id, "rejeitado", obs);
                            }}
                            disabled={processando[a.id]}
                            className={`px-3 py-1.5 text-xs font-medium rounded-md border transition-colors disabled:opacity-50 ${
                              processando[a.id]
                                ? "bg-gray-200 text-gray-400 border-gray-200"
                                : "bg-red-600 text-white border-red-600 hover:bg-red-700"
                            }`}
                          >
                            {processando[a.id] ? "..." : "Rejeitar"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="px-6 py-12 text-center text-gray-400 text-sm">
                      <Clock size={20} className="mx-auto mb-2 opacity-50" />
                      Nenhum ajuste pendente de analise no momento.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  );
}
