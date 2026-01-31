// Variáveis globais
let allUsers = [];
let recentPunches = [];
let employees = []; // Alias para compatibilidade
const textField = document.getElementById("filter-by-name");

document.addEventListener('DOMContentLoaded', async () => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) {
        window.location.href = "login.html";
        return;
    }

    // Verificar se é admin
    if (auth.role !== 'ADMIN') {
        alert('Acesso negado. Apenas administradores podem acessar esta página.');
        window.location.href = "employee.html";
        return;
    }

    initNavigation();

    // Carregar dados
    await Promise.all([
        loadUsers(),
        loadRecentPunches()
    ]);

    // Configurar eventos
    setupEventListeners();
});

function setupEventListeners() {
    // Formulário de Cadastro/Edição
    const employeeForm = document.getElementById('employeeForm');
    if (employeeForm) {
        employeeForm.addEventListener('submit', handleEmployeeSubmit);
    }

// Formulário de Ajuste
    document.getElementById('adjustmentForm').addEventListener('submit', handleAdjustmentSubmit);

// Carregar histórico de ajustes
    loadAdjustmentHistory();

    // Logout
    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Deseja encerrar a sessão administrativa?')) {
            localStorage.removeItem("auth");
            window.location.href = 'login.html';
        }
    });

// ♠♠♠♠♠♠6 FILTRANDO POR NOME
    textField.addEventListener("keypress", (e) => {
        if (e.key !== "Enter") {
            let text = textField.value + e.key;
            renderEmployees(allUsers.filter(item => item.nome.toLowerCase().startsWith(text.trim().toLowerCase())));
        }
    })

    textField.addEventListener("input", (e) => {
        if (e.inputType == "deleteContentBackward" || e.inputType == "deleteContentForward") {
            renderEmployees(allUsers.filter(item => item.nome.toLowerCase().startsWith(textField.value.trim().toLowerCase())));
        }
    })

    // ♠♠♠♠♠♠7 FORMATANDO CPF
    const cpfField = document.getElementById('empCpf');
    if (cpfField) {
        cpfField.addEventListener('input', (e) => {
            let value = e.target.value;
            // Remove tudo que não é número
            value = value.replace(/\D/g, '');
            // Limita a 11 dígitos
            value = value.slice(0, 11);
            // Formata o CPF
            if (value.length > 3 && value.length <= 6) {
                value = value.replace(/(\d{3})(\d)/, '$1.$2');
            } else if (value.length > 6 && value.length <= 9) {
                value = value.replace(/(\d{3})(\d{3})(\d)/, '$1.$2.$3');
            } else if (value.length > 9) {
                value = value.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
            }
            e.target.value = value;
        });
    }
}

async function loadUsers() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch("http://localhost:8080/api/user", {
            headers: {
                "Authorization": `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            allUsers = await response.json();
            renderEmployees(allUsers);
            populateSelects();
        } else {
            if (response.status === 403) {
                localStorage.removeItem("auth");
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 2000)
            } else {
                showToast('Erro ao carregar usuários', 'error');
            }
        }
    } catch (error) {
        console.error('Erro ao carregar usuários:', error);
        showToast('Erro ao carregar usuários', 'error');
    }
}

async function loadRecentPunches() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch("http://localhost:8080/api/ponto/status-atual", {
            headers: {
                "Authorization": `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            recentPunches = await response.json();
            renderDashboard(recentPunches);
        } else {
            showToast('Erro ao carregar status dos funcionários', 'error');
        }
    } catch (error) {
        console.error('Erro ao carregar status dos funcionários:', error);
        showToast('Erro ao carregar status dos funcionários', 'error');
    }
}



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
        const status = emp.ativo ? 'Ativo' : 'Inativo';
        const roleName = emp.role ? emp.role : 'N/A';
        const cargo = emp.cargo || 'Não informado';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${emp.nome}</td>
            <td>${cargo}</td>
            <td>${roleName}</td>
            <td><span class="status-badge status-${status.toLowerCase()}">${status}</span></td>
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

    // Mostra o status atual dos funcionários
    employees.forEach(emp => {
        const tr = document.createElement('tr');
        const statusClass = emp.emServico ? 'status-active' : 'status-inactive';
        const statusText = emp.emServico ? emp.status : `<span class="status-absent">${emp.status}</span>`;
        tr.innerHTML = `
            <td>${emp.nome}</td>
            <td><strong class="${statusClass}">${statusText}</strong></td>
            <td>${emp.horarioEntrada}</td>
        `;
        activity.appendChild(tr);
    });
}

// Popula campos de seleção em Ajustes e Relatórios
function populateSelects() {
    const selects = [document.getElementById('adjEmployee'), document.getElementById('reportEmployee')];
    selects.forEach(sel => {
        sel.innerHTML = allUsers.map(e => `<option value="${e.id}">${e.nome}</option>`).join('');
    });
}

// Modal Logic
function openModal(type) {
    document.getElementById('modalOverlay').classList.add('active');
    document.getElementById('modalOverlay').style.animation = 'fadeIn 0.3s ease';
    if (type === 'add') {
        document.getElementById('modalTitle').textContent = 'Cadastrar Funcionário';
        document.getElementById('employeeForm').reset();
        document.getElementById('empPassword').required = true; // Senha obrigatória para novo usuário
        document.getElementById('empPassword').placeholder = '';
        document.getElementById('editIndex').value = '';
        document.getElementById('editUserId').value = '';
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
    const emp = allUsers[index];
    openModal('edit');
    document.getElementById('modalTitle').textContent = 'Editar Funcionário';
    document.getElementById('empName').value = emp.nome;
    document.getElementById('empUsername').value = emp.username;
    document.getElementById('empPassword').required = false; // Senha opcional na edição
    document.getElementById('empPassword').placeholder = 'Deixe em branco para manter a senha atual';
    document.getElementById('empRole').value = emp.cargo || '';
    document.getElementById('empCpf').value = emp.cpf || '';
    document.getElementById('empActive').value = emp.ativo ? 'Ativo' : 'Inativo';
    document.getElementById('empProfile').value = emp.role;
    document.getElementById('editIndex').value = index;
    document.getElementById('editUserId').value = emp.id;
}




async function handleEmployeeSubmit(e) {
    e.preventDefault();

    const userId = document.getElementById('editUserId').value;
    const isEdit = userId && userId.trim() !== '';

    const userData = {
        username: document.getElementById("empUsername").value,
        nome: document.getElementById('empName').value,
        role: document.getElementById("empProfile").value,
        ativo: document.getElementById("empActive").value === 'Ativo',
        consentGeoloc: true,
        cargo: document.getElementById("empRole").value,
        cpf: document.getElementById("empCpf").value
    }

    // Incluir senha apenas se foi preenchida (para edição) ou se é criação
    const password = document.getElementById("empPassword").value;
    if (password.trim() !== '') {
        userData.password = password;
    }

    const token = JSON.parse(localStorage.getItem("auth")).token;
    const url = `http://localhost:8080/api/user${isEdit ? `/${userId}` : ''}`;
    const method = isEdit ? "PUT" : "POST";

    try {
        const result = await fetch(url, {
            method: method,
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userData)
        });

        if (result.status == 201 || result.status == 200) {
            const message = isEdit ? "Usuário atualizado com sucesso" : "Usuário cadastrado com sucesso";
            showToast(message, "success");
            // Recarregar lista de usuários
            await loadUsers();
        } else {
            const erro = await result.json();
            const errorMessage = isEdit ? "Erro ao atualizar usuário" : "Erro ao cadastrar usuário";
            showToast(erro.erro || errorMessage, "error");
        }
    } catch (error) {
        console.error('Erro ao salvar usuário:', error);
        const errorMessage = isEdit ? "Erro ao atualizar usuário" : "Erro ao cadastrar usuário";
        showToast(errorMessage, "error");
    }

    closeModal();
}

async function handleAdjustmentSubmit(e) {
    e.preventDefault();

    const employeeId = document.getElementById('adjEmployee').value;
    const date = document.getElementById('adjDate').value;
    const tipo = document.getElementById('adjStatus').value;
    const time = document.getElementById('adjTime').value;
    const justificativa = document.getElementById('adjJustification').value;

    // Validate data
    const validation = validateAdjustmentData({
        userId: employeeId,
        date: date,
        tipo: tipo,
        time: time,
        justificativa: justificativa
    });

    if (!validation.isValid) {
        validation.errors.forEach(error => showToast(error, 'error'));
        return;
    }

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const adjustmentData = {
            userId: employeeId,
            date: date,
            tipo: tipo,
            time: time,
            justificativa: justificativa
        };

        const response = await fetch('http://localhost:8080/api/ponto/ajuste', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.token}`
            },
            body: JSON.stringify(adjustmentData)
        });

        if (response.ok) {
            showToast('Ajuste de ponto realizado com sucesso!', 'success');
            e.target.reset();
            // Refresh adjustment history
            await loadAdjustmentHistory();
        } else {
            const error = await response.json();
            showToast(error.erro || 'Erro ao realizar ajuste', 'error');
        }
    } catch (error) {
        console.error('Erro ao realizar ajuste:', error);
        showToast('Erro ao realizar ajuste', 'error');
    }
}

async function loadAdjustmentHistory() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        const endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);

        // Format dates for API
        const formatDateForAPI = (date) => {
            return date.toISOString().split('T')[0] + 'T00:00:00';
        };

        const response = await fetch(`http://localhost:8080/api/ponto/ajustes-admin?start=${formatDateForAPI(firstDayOfMonth)}&end=${formatDateForAPI(endDate)}`, {
            headers: {
                "Authorization": `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const adjustments = await response.json();
            renderAdjustmentHistory(adjustments);
        }
    } catch (error) {
        console.error('Erro ao carregar histórico de ajustes:', error);
    }
}

function renderAdjustmentHistory(adjustments) {
    // Create adjustment history section if it doesn't exist
    let historySection = document.getElementById('adjustmentHistory');
    if (!historySection) {
        const adjustmentsSection = document.getElementById('adjustments');
        if (adjustmentsSection) {
            historySection = document.createElement('div');
            historySection.id = 'adjustmentHistory';
            historySection.className = 'card';
            historySection.style.marginTop = '20px';
            historySection.innerHTML = `
                <h3>Histórico de Ajustes Recentes</h3>
                <div id="adjustmentHistoryList" style="max-height: 300px; overflow-y: auto;"></div>
            `;
            adjustmentsSection.appendChild(historySection);
        }
    }

    const historyList = document.getElementById('adjustmentHistoryList');
    if (historyList) {
        historyList.innerHTML = '';

        // Filter adjustments (those with "AJUSTE_ADMIN" as IP)
        const adminAdjustments = adjustments.filter(adj => adj.ip === 'AJUSTE_ADMIN');

        if (adminAdjustments.length === 0) {
            historyList.innerHTML = '<p>Nenhum ajuste recente encontrado.</p>';
            return;
        }

        adminAdjustments.forEach(adj => {
            const adjElement = document.createElement('div');
            adjElement.style.padding = '10px';
            adjElement.style.borderBottom = '1px solid #eee';
            adjElement.style.marginBottom = '5px';

            // Debug: Verificar o que está vindo do backend
            console.log('Ajuste recebido:', adj);
            console.log('Campos disponíveis:', Object.keys(adj));

            // Buscar o nome do funcionário no array allUsers
            // O backend retorna o nome do usuário no campo 'nomeUsuario' (não 'user')
            const employeeName = adj.nomeUsuario || 'Desconhecido';

            adjElement.innerHTML = `
                <strong>${employeeName}</strong> - ${new Date(adj.timestamp).toLocaleString('pt-BR')}<br>
                <small style="color: #666;">${adj.tipo} - ${adj.justificativa}</small>
            `;
            historyList.appendChild(adjElement);
        });
    }
}

function validateAdjustmentData(data) {
    const errors = [];

    if (!data.userId) errors.push("Funcionário não selecionado");
    if (!data.date) errors.push("Data não informada");
    if (!data.tipo) errors.push("Tipo de registro não informado");
    if (!data.time) errors.push("Horário não informado");
    if (!data.justificativa || data.justificativa.trim().length < 10) {
        errors.push("Justificativa deve ter pelo menos 10 caracteres");
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}

async function generateReport() {
    const userId = document.getElementById('reportEmployee').value;
    const monthYear = document.getElementById('reportMonth').value;

    if (!userId || !monthYear) {
        showToast('Selecione o funcionário e o período', 'error');
        return;
    }

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const [year, month] = monthYear.split('-');

        // Mostrar feedback de carregamento
        showToast('Gerando relatório PDF...', 'info');

        // Gerar o PDF diretamente com o novo endpoint
        const pdfResponse = await fetch('http://localhost:8080/api/relatorios/pdf', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.token}`
            },
            body: JSON.stringify({
                userId: parseInt(userId),
                mes: parseInt(month),
                ano: parseInt(year)
            })
        });

        if (pdfResponse.ok) {
            const pdfBase64 = await pdfResponse.text();

            // Criar link para download
            const link = document.createElement('a');
            link.href = `data:application/pdf;base64,${pdfBase64}`;
            link.download = `folha_ponto_${monthYear}.pdf`;
            link.click();

            showToast('Relatório PDF gerado e baixado com sucesso!', 'success');
        } else {
            // Tratar respostas de erro que podem não ser JSON
            let errorMessage = 'Erro ao gerar PDF';
            try {
                const errorData = await pdfResponse.json();
                errorMessage = errorData.erro || 'Erro ao gerar PDF';
            } catch (e) {
                // Se não for JSON, tentar obter texto simples
                try {
                    const errorText = await pdfResponse.text();
                    errorMessage = errorText || 'Erro ao gerar PDF';
                } catch (e2) {
                    errorMessage = 'Erro ao gerar PDF';
                }
            }
            showToast(errorMessage, 'error');
        }
    } catch (error) {
        console.error('Erro ao gerar relatório:', error);
        showToast('Erro ao gerar relatório PDF', 'error');
    }
}

function showToast(message, type) {
    const toast = document.createElement('div');
    let backgroundToast = type === 'success' ? 'rgba(45, 206, 137, 0.95)' : 'rgba(220, 53, 69, 0.95)';
    if (type === 'info') backgroundToast = 'rgba(255, 152, 0, 0.9)';
    toast.className = `toast-notification ${type}`;
    toast.textContent = message;
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '8px';
    toast.style.backgroundColor = backgroundToast;
    toast.style.color = 'white';
    toast.style.fontSize = '0.9rem';
    toast.style.fontWeight = '600';
    toast.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.3)';
    toast.style.zIndex = '1000';
    toast.style.transform = 'translateY(20px)';
    toast.style.opacity = '0';
    toast.style.transition = 'all 0.3s ease-out';

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';
    }, 10);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3000);
}