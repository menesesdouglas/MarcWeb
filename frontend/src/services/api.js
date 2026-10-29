import axios from "axios";

// O Vite proxy encaminha "/api" para "http://localhost:8000" (vite.config.js).
// Os endpoints do backend estão montados em "/api/v1", então os caminhos
// absolutos "/api/v1/..." abaixo são suficientes; não prefixamos "/api" duas vezes.
const API_BASE = "/api";

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor para injetar o token JWT em cada requisicao
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("repp_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para tratar erros de autenticacao (401)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("repp_token");
      localStorage.removeItem("repp_user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;

export const authService = {
  login: (email, senha) =>
    api.post("/api/v1/auth/login", { username: email, password: senha }),
  logout: () => api.post("/api/v1/auth/logout"),
};

export const usuarioService = {
  listar: () => api.get("/api/v1/users/"),
  criar: (data) => api.post("/api/v1/users/", data),
  obter: (id) => api.get(`/api/v1/users/${id}`),
};

export const pontoService = {
  marcar: (tipo) => api.post("/api/v1/ponto/marcar", { tipo }),
  extrato: (dia) => {
    const params = dia ? { dia } : {};
    return api.get("/api/v1/ponto/extrato", { params });
  },
  resumo: () => api.get("/api/v1/ponto/resumo"),
};

export const ajusteService = {
  solicitar: (data) => api.post("/api/v1/ajustes/solicitar", data),
  listarMeus: () => api.get("/api/v1/ajustes/meus"),
};