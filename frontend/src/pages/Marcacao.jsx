import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { pontoService } from "../services/api";
import { ProtectedRoute } from "../components/ProtectedRoute";
import { Clock, Play, LogOut, CheckCircle2, AlertCircle } from "lucide-react";

const TIPO_LABEL = {
  entrada: "Entrada",
  saida_intervalo: "Saída Intervalo",
  retorno_intervalo: "Retorno Intervalo",
  saida: "Saída Final",
};

const TIPO_COR = {
  entrada: "bg-green-600 hover:bg-green-700 text-white",
  saida_intervalo: "bg-amber-600 hover:bg-amber-700 text-white",
  retorno_intervalo: "bg-blue-600 hover:bg-blue-700 text-white",
  saida: "bg-red-600 hover:bg-red-700 text-white",
};

const TIPO_COR_TEXTO = {
  entrada: "text-green-600",
  saida_intervalo: "text-amber-600",
  retorno_intervalo: "text-blue-600",
  saida: "text-red-600",
};

const TIPO_ICON = {
  entrada: Play,
  saida_intervalo: Clock,
  retorno_intervalo: Clock,
  saida: LogOut,
};

export default function Marcacao() {
  const { user } = useAuth();
  const [resumo, setResumo] = useState(null);
  const [extrato, setExtrato] = useState([]);
  const [mensagem, setMensagem] = useState(null);
  const [tipoMensagem, setTipoMensagem] = useState("sucesso");
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    carregarDados();
  }, []);

  async function carregarDados() {
    try {
      const [rResp, eResp] = await Promise.all([
        pontoService.resumo(),
        pontoService.extrato(),
      ]);
      setResumo(rResp.data);
      setExtrato(eResp.data.registros || []);
    } catch (err) {
      console.error(err);
    }
  }

  async function handleMarcar(tipo) {
    setCarregando(true);
    setMensagem(null);
    try {
      const resposta = await pontoService.marcar(tipo);
      setTipoMensagem("sucesso");
      setMensagem(
        `Marcacao de ${TIPO_LABEL[tipo]} realizada com sucesso!`
      );
      await carregarDados();
    } catch (err) {
      setTipoMensagem("erro");
      setMensagem(
        err.response?.data?.detail || "Erro ao marcar ponto. Tente novamente."
      );
    } finally {
      setCarregando(false);
    }
  }

  const podeMarcar = (tipo) =>
    !resumo || resumo.proximo_tipo_esperado === tipo;

  return (
    <ProtectedRoute>
      <div className="max-w-4xl mx-auto">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Marcacao de Ponto</h1>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <span className="font-medium">{user.nome}</span>
            <span className="px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 text-xs">
              {user.perfil === "adm" ? "ADM" : "Funcionario"}
            </span>
          </div>
        </div>

        {mensagem && (
          <div className={`mb-6 p-4 rounded-lg text-sm ${
            tipoMensagem === "sucesso"
              ? "bg-green-50 text-green-700 border border-green-200"
              : "bg-red-50 text-red-700 border border-red-200"
          }`}>
            {tipoMensagem === "sucesso" ? (
              <>
                <CheckCircle2 size={18} className="mr-2" /> {mensagem}
              </>
            ) : (
              <>
                <AlertCircle size={18} className="mr-2" /> {mensagem}
              </>
            )}
          </div>
        )}

        {/* Painel principal de marcacao */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6 border border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Registrar ponto</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {["entrada", "saida_intervalo", "retorno_intervalo", "saida"].map(
              (tipo) => {
                const Icon = TIPO_ICON[tipo];
                const pode = podeMarcar(tipo);
                return (
                  <button
                    key={tipo}
                    onClick={() => handleMarcar(tipo)}
                    disabled={!pode || carregando}
                    className={`w-full py-4 rounded-lg font-semibold flex items-center justify-center gap-2 transition-all disabled:opacity-50 ${
                      pode
                        ? TIPO_COR[tipo]
                        : "bg-gray-200 text-gray-400"
                    }`}
                  >
                    <Icon size={20} className="mr-2" />
                    {TIPO_LABEL[tipo]}
                  </button>
                );
              }
            )}
          </div>

          {resumo && (
            <div className="mt-6 pt-6 border-t border-gray-100">
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-500">Ultima marcacao</span>
                <span className="text-sm font-medium text-gray-700">
                  {resumo.ultima_marcacao ? resumo.ultima_marcacao.tipo : "Nenhuma"}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">Proxima acao esperada</span>
                <span className="text-sm font-medium text-blue-600">
                  {resumo.proximo_tipo_esperado}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Extrato do dia */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h2 className="text-lg font-semibold text-gray-900">Extrato de hoje</h2>
            <span className="text-sm font-medium text-gray-600">
              Total: {extrato.length} marcacao(oes)
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Tipo
                  </th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Horario (servidor)
                  </th>
                  <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase">
                    Origem
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {extrato.length > 0 ? (
                  extrato.map((r) => (
                    <tr key={r.id}>
                      <td className="px-6 py-3 text-sm font-medium">
                        <span className={`${TIPO_COR_TEXTO[r.tipo]} font-medium`}>
                          {TIPO_LABEL[r.tipo]}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-700">
                        {new Date(r.timestamp).toLocaleString("pt-BR", {
                          hour12: false,
                        })}
                      </td>
                      <td className="px-6 py-3 text-sm text-gray-500">{r.origem}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-gray-400 text-sm">
                      Nenhuma marcacao registrada hoje.
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