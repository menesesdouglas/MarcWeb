import { Outlet, useNavigate, Link } from "react-router-dom";
import { LogOut, User, Clock, FileText, ShieldAlert, LayoutDashboard, Edit3 } from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const isAdm = user?.perfil === "adm";

  const menuItems = [
    { label: "Marcacao de ponto", href: "/marcacao", icon: <Clock size={18} /> },
    { label: "Meu extrato", href: "/extrato", icon: <FileText size={18} /> },
    { label: "Solicitar ajuste", href: "/ajuste", icon: <Edit3 size={18} /> },
    ...(isAdm
      ? [
          { label: "Cadastro de usuarios", href: "/cadastro", icon: <User size={18} /> },
          { label: "Dashboard ADM", href: "/dashboard", icon: <LayoutDashboard size={18} /> },
        ]
      : []),
  ];

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-6 flex items-center gap-2">
          <ShieldAlert className="h-6 w-6 text-blue-600" />
          <span className="font-bold text-lg tracking-tight">REP-P</span>
        </div>

        <nav className="flex-1 px-4 space-y-1">
          {menuItems.map((item) => (
            <Link
              key={item.label}
              to={item.href}
              className="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg transition-colors hover:bg-gray-100 hover:text-blue-600"
            >
              {item.icon}
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="p-4 border-t border-gray-200">
          <button
            onClick={() => {
              logout();
              navigate("/login");
            }}
            className="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
          >
            <LogOut size={18} /> Sair
          </button>
        </div>
      </aside>

      {/* Conteudo principal */}
      <main className="flex-1 overflow-y-auto bg-gray-50 p-6">
        <Outlet />
      </main>
    </div>
  );
}