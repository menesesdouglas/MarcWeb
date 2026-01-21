// Dados iniciais (Mock)
/// REGISTROS RECENTES VIA API ♠♠♠♠♠

let employees = [
    { name: "João Silva", role: "Desenvolvedor", status: "Ativo", lastStatus: "Entrada", lastTime: "08:55" },
    { name: "Maria Clara", role: "RH Especialista", status: "Ativo", lastStatus: "Início Descanso", lastTime: "12:02" },
    { name: "Roberto Souza", role: "Gestor Comercial", status: "Inativo", lastStatus: "Saída", lastTime: "18:00" }
];

document.addEventListener('DOMContentLoaded', async () => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (auth == null) window.location.href = "login.html";
    const result = await fetch("http://localhost:8080/api/ponto/recentes", {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${auth.token}`
        }
    });
    if (result.status == 403) window.location.href = "login.html";
    const employees = await result.json();

    initNavigation();
    populateSelects();
    renderEmployees(employees);
    renderDashboard(employees);

    // Formulário de Cadastro/Edição
    document.getElementById('employeeForm').addEventListener('submit', handleEmployeeSubmit);

    // Formulário de Ajuste
    document.getElementById('adjustmentForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const employee = document.getElementById('adjEmployee').value;
        const date = document.getElementById('adjDate').value;
        const status = document.getElementById('adjStatus').value;
        const time = document.getElementById('adjTime').value;
        const justification = document.getElementById('adjJustification').value;

        alert(`Ajuste de ponto realizado com sucesso!\n\nFuncionário: ${employee}\nData: ${date}\nStatus: ${status}\nHorário: ${time}\nJustificativa: ${justification}`);
        e.target.reset();
    });

    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Deseja encerrar a sessão administrativa?')) window.location.href = 'login.html';
    });
});

// Navegação entre seções (Sidebar)
function initNavigation() {
    const links = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('.content-section');

    links.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const target = link.getAttribute('data-section');

            links.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            sections.forEach(s => {
                s.classList.remove('active');
                if (s.id === target) s.classList.add('active');
            });
        });
    });
}

// Renderizar Lista de Funcionários
function renderEmployees(employees) {
    const container = document.getElementById('employeeListBody');
    container.innerHTML = '';

    employees.forEach((emp, index) => {

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${emp.name}</td>
            <td>${emp.role}</td>
            <td><span class="status-badge status-${emp.status}">${emp.status}</span></td>
            <td>
                <button onclick="editEmployee(${index})" style="background:none; border:none; color:#0a9396; cursor:pointer">Editar</button>
            </td>
        `;
        container.appendChild(tr);
    });
}

// Renderizar Dashboard
function renderDashboard(employees) {
    const activity = document.getElementById('recentActivityBody');
    activity.innerHTML = '';

    // Mostra as últimas atividades
    employees.forEach(emp => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${emp.name}</td>
            <td><strong>${emp.lastStatus}</strong></td>
            <td>${emp.lastTime}</td>
        `;
        activity.appendChild(tr);
    });
}

// Popula campos de seleção em Ajustes e Relatórios
function populateSelects() {
    const selects = [document.getElementById('adjEmployee'), document.getElementById('reportEmployee')];
    selects.forEach(sel => {
        sel.innerHTML = employees.map(e => `<option value="${e.name}">${e.name}</option>`).join('');
    });
}

// Modal Logic
function openModal(type) {
    document.getElementById('modalOverlay').classList.add('active');
    document.getElementById('modalOverlay').style.animation = 'fadeIn 0.3s ease';
    if (type === 'add') {
        document.getElementById('modalTitle').textContent = 'Cadastrar Funcionário';
        document.getElementById('employeeForm').reset();
        document.getElementById('editIndex').value = '';
    }
}

function closeModal() {
    const overlay = document.getElementById('modalOverlay');
    overlay.style.animation = 'fadeOut 0.3s ease';
    setTimeout(() => {
        overlay.classList.remove('active');
        overlay.style.animation = '';
    }, 300);
}

// Add fadeOut animation to CSS
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }
`;
document.head.appendChild(style);

function editEmployee(index) {
    const emp = employees[index];
    openModal('edit');
    document.getElementById('modalTitle').textContent = 'Editar Funcionário';
    document.getElementById('empName').value = emp.name;
    document.getElementById('empRole').value = emp.role;
    document.getElementById('empActive').value = emp.status;
    document.getElementById('editIndex').value = index;
}

async function handleEmployeeSubmit(e) {
    e.preventDefault();
    const index = document.getElementById('editIndex').value;
    const newData = {
        name: document.getElementById('empName').value,
        role: document.getElementById('empRole').value,
        status: document.getElementById('empActive').value,
        lastStatus: "Sem registro",
        lastTime: "--:--"
    };

    if (index === "") {
        employees.push(newData);
    } else {
        employees[index] = { ...employees[index], ...newData };
    }


    /// ------- | -----------
    const newUser = {
        username: document.getElementById("empUsername").value,
        nome: document.getElementById('empName').value,
        password: document.getElementById("empPassword").value,
        role: document.getElementById("empProfile").value,
        consentGeoloc: true

    }

    const token = JSON.parse(localStorage.getItem("auth")).token;

    const result = await fetch("http://localhost:8080/api/user", {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newUser)
    });
    if (result.status == 201) {
        alert("Usuário cadastrado com sucesso")
    } else {
        const erro = await result.json();
        alert(erro.erro);
    }

    closeModal();
}

function generateReport() {
    alert('Relatório gerado com sucesso! Iniciando download do PDF...');
}
