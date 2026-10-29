import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { pontoService } from "../services/api";
import { ProtectedRoute } from "../components/ProtectedRoute";

export default function Extrato() {
  const { user } = useAuth();
  const [registros, setRegistros] = useState([]);
  const [dataSelecionada, setDataSelecionada] = useState(new Date().toISOString().split("T")[0]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    carregarExtrato(dataSelecionada);
  }, [dataSelecionada]);

  async function carregarExtrato(dia) {
    setCarregando(true);
    setErro("");
    try {
      const resposta = await pontoService.extrato(dia);
      setRegistros(resposta.data.registros || []);
    } catch (err) {
      setErro(err.response?.data?.detail || "Erro ao carregar extrato");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <ProtectedRoute>
      <div className="max-w-3xl mx-auto">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Meu Extrato</h1>
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600">Usuario:</span>
            <span className="font-medium">{user.nome}</span>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <div className="flex flex-col md:flex-row gap-4 items-end">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Data
              </label>
              <input
                type="date"
                value={dataSelecionada}
                onChange={(e) => setDataSelecionada(e.target.value)}
                className="w-full md:w-64 border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              />
            </div>
            <button
              onClick={() => carregarExtrato(dataSelecionada)}
              disabled={carregando}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
            >
              {carregando ? "Carregando..." : "Consultar"}
            </button>
          </div>

          {erro && (
            <div className="mt-4 p-3 rounded bg-red-50 text-red-700 text-sm border border-red-100">
              {erro}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">
              Marcacoes em {new Date(dataSelecionada).toLocaleDateString("pt-BR")}
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              Total: {registros.length} marcacao(oes)
            </p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Tipo</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">Horario (servidor)</th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">IP de origem</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {registros.length > 0 ? (
                  registros.map((r) => (
                    <tr key={r.id}>
                      <td className="px-6 py-3 text-sm font-medium">
                        {r.tipo === "entrada" ? (
                          <span className="text-green-600">Entrada</span>
                        ) : (
                          <span className="text-red-600">Saida</span>
                        )}
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-700">
                        {new Date(r.timestamp).toLocaleString("pt-BR", {
                          hour12: false,
                        })}
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-500">
                        {r.ip_origem || "-"}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-gray-400 text-sm">
                      Nenhuma marcacao registrada nesta data.
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