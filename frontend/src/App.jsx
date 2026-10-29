import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Marcacao from "./pages/Marcacao";
import Extrato from "./pages/Extrato";
import Cadastro from "./pages/Cadastro";
import SolicitarAjuste from "./pages/SolicitarAjuste";
import AprovarAjuste from "./pages/AprovarAjuste";

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <Layout>
                <Routes>
                  <Route index element={<Navigate to="/marcacao" replace />} />
                  <Route path="/marcacao" element={<Marcacao />} />
                  <Route path="/extrato" element={<Extrato />} />
                  <Route path="/cadastro" element={<Cadastro />} />
                  <Route path="/ajuste" element={<SolicitarAjuste />} />
                  <Route
                    path="/ajustes-pendentes"
                    element={<AprovarAjuste />}
                  />
                </Routes>
              </Layout>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;