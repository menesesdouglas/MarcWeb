import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authService } from "../services/api";

export default function Login() {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [carregando, setCarregando] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  async function handleSubmit(e) {
    e.preventDefault();
    setErro("");
    setCarregando(true);
    try {
      // OAuth2PasswordRequestForm espera application/x-www-form-urlencoded
      const resposta = await authService.login(email, senha);
      const token = resposta.data.access_token;

      // Decodifica o payload do JWT para preencher dados básicos do usuário.
      // Em fallback, armazena ao menos o id e o e-mail informado no login.
      let usuario = { id: "", email };
      try {
        const payloadBase64 = token.split(".")[1];
        const payload = JSON.parse(atob(payloadBase64));
        usuario = { id: payload.sub, email };
      } catch (_) {
        // token malformado: mantém usuario parcial; o backend ainda valida o JWT
      }

      login(token, usuario);
      navigate("/marcacao");
    } catch (err) {
      setErro(
        err.response?.data?.detail || err.message || "Erro ao fazer login"
      );
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md">
        <div className="mb-6 text-center">
          <h1 className="text-2xl font-bold text-gray-900">REP-P</h1>
          <p className="text-sm text-gray-500">Registro Eletronico de Ponto</p>
        </div>

        {erro && (
          <div className="mb-4 p-3 rounded bg-red-50 text-red-700 text-sm border border-red-100">
            {erro}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              E-mail
            </label>
            <input
              type="email"
              required
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="seu@email.com"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Senha
            </label>
            <input
              type="password"
              required
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </div>

          <button
            type="submit"
            disabled={carregando}
            className="w-full bg-blue-600 text-white font-medium py-2.5 rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
          >
            {carregando ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          Primeiro acesso? Use o usuario ADM cadastrado via seed.
        </p>
      </div>
    </div>
  );
}