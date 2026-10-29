import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { ajusteService } from "../services/api";
import { ProtectedRoute } from "../components/ProtectedRoute";
import { Clock, FileText, AlertCircle, CheckCircle2 } from "lucide-react";

const TIPO_LABEL = {
  entrada: "Entrada",
  saida_intervalo: "Saída Intervalo",
  retorno_intervalo: "Retorno Intervalo",
  saida: "Saída Final",
};

const STATUS_COR = {
  pendente: "text-amber-700 bg-amber-100 border-amber-200",
  aprovado: "text-green-700 bg-green-100 border-green-200",
  rejeitado: "text-red-700 bg-red-100 border-red-200",
};

export default function SolicitarAjuste() {
  const { user } = useAuth();
  const [form, setForm] = useState({
    tipo_solicitado: "entrada",
    data_solicitada: new Date().toISOString().split("T")[0],
    justificativa: "",
  });
  const [ajustes, setAjustes] = useState([]);
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [carregando, setCarregando] = useState(false);
  const [carregandoLista, setCarregandoLista] = useState(false);

  const TIPO_OPCOES = Object.entries(TIPO_LABEL);

  useEffect(() => {
    carregarAjustes();
  }, []);

  async function carregarAjustes() {
    setCarregandoLista(true);
    try {
      const resposta = await ajusteService.listarMeus();
      setAjustes(resposta.data || []);
    } catch (err) {
      setErro(
        err.response?.data?.detail || "Erro ao carregar seus ajustes"
      );
    } finally {
      setCarregandoLista(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setErro("");
    setSucesso("");
    setCarregando(true);
    try {
      await ajusteService.solicitar(form);
      setSucesso(
        "Solicitacao de ajuste enviada com sucesso! Aguarde analise do ADM."
      );
      setForm({
        tipo_solicitado: "entrada",
        data_solicitada: new Date().toISOString().split("T")[0],
        justificativa: "",
      });
      await carregarAjustes();
    } catch (err) {
      setErro(
        err.response?.data?.detail || "Erro ao solicitar ajuste"
      );
    } finally {
      setCarregando(false);
    }
  }

  return (
    <ProtectedRoute>
      <div className="max-w-3xl mx-auto">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">
            Solicitacao de Ajuste de Ponto
          </h1>
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

        {/* Formulario de solicitacao */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Nova solicitacao
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tipo de marcacao solicitado
                </label>
                <select
                  required
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none bg-white"
                  value={form.tipo_solicitado}
                  onChange={(e) =>
                    setForm({ ...form, tipo_solicitado: e.target.value })
                  }
                >
                  {TIPO_OPCOES.map(([valor, label]) => (
                    <option key={valor} value={valor}>
                      {label}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Data da marcacao
                </label>
                <input
                  type="date"
                  required
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                  value={form.data_solicitada}
                  onChange={(e) =>
                    setForm({ ...form, data_solicitada: e.target.value })
                  }
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Justificativa (obrigatoria, minimo 10 caracteres)
              </label>
              <textarea
                required
                minLength={10}
                maxLength={1000}
                rows={4}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                value={form.justificativa}
                onChange={(e) =>
                  setForm({ ...form, justificativa: e.target.value })
                }
              />
              <p className="text-xs text-gray-500 mt-1">
                {form.justificativa.length}/1000 caracteres
              </p>
            </div>
            <button
              type="submit"
              disabled={carregando}
              className="w-full bg-blue-600 text-white font-medium py-2.5 rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
            >
              {carregando ? "Enviando solicitacao..." : "Solicitar ajuste"}
            </button>
          </form>
        </div>

        {/* Lista de ajustes do usuario */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">
              Suas solicitacoes
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              Total: {ajustes.length} solicitacao(oes)
            </p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Tipo
                  </th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Data
                  </th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Solicitado em
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {ajustes.length > 0 ? (
                  ajustes.map((a) => (
                    <tr key={a.id}>
                      <td className="px-6 py-3 text-sm font-medium">
                        {TIPO_LABEL[a.tipo_solicitado] || a.tipo_solicitado}
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-700">
                        {new Date(a.data_solicitada).toLocaleDateString(
                          "pt-BR"
                        )}
                      </td>
                      <td className="px-6 py-3 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium border ${STATUS_COR[a.status] || STATUS_COR.pendente}`}>
                          {a.status.charAt(0).toUpperCase() + a.status.slice(1)}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-700">
                        {a.criado_em
                          ? new Date(a.criado_em).toLocaleString("pt-BR", {
                              hour12: false,
                            })
                          : "-"}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="px-6 py-8 text-center text-gray-400 text-sm">
                      Nenhuma solicitacao de ajuste registrada.
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