document.addEventListener('DOMContentLoaded', () => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) {
        window.location.href = 'login.html';
        return;
    }

    // Carregar dados do usuário
    loadUserProfile();

    // Carregar folgas do usuário
    loadUserFolgas();

    // Carregar atividades recentes
    loadRecentActivity();

    // Configurar eventos
    setupEventListeners();
});

function setupEventListeners() {
    // Logout
    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Deseja realmente sair do sistema?')) {
            localStorage.removeItem("auth");
            window.location.href = 'login.html';
        }
    });

    // Formulário de perfil
    document.getElementById('profileForm').addEventListener('submit', handleProfileUpdate);

    // Formulário de folga
    document.getElementById('folgaForm').addEventListener('submit', handleFolgaSubmit);
}

async function loadUserProfile() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch('http://localhost:8080/api/user/me', {
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const user = await response.json();
            document.getElementById('userName').textContent = user.nome;
            document.getElementById('userUsername').textContent = `Usuário: ${user.username}`;

            // Exibir cargo (campo correto) ou role como fallback
            const cargoDisplay = user.cargo || user.role || 'Não especificado';
            document.getElementById('userRole').textContent = cargoDisplay;

            // Preencher formulário
            document.getElementById('nome').value = user.nome;
            document.getElementById('username').value = user.username;
            document.getElementById('role').value = user.cargo || user.role || '';

            // Lógica de permissões para edição de cargo (baseada no role real, não no cargo exibido)
            setupRolePermissions(user.role);
            
            // Armazenar o role real do usuário para uso nas validações de permissão
            document.getElementById('userRole').setAttribute('data-role', user.role);
        } else {
            showToast('Erro ao carregar dados do usuário', 'error');
        }
    } catch (error) {
        console.error('Erro ao carregar perfil:', error);
        showToast('Erro ao carregar dados do usuário', 'error');
    }
}

function setupRolePermissions(userRole) {
    const roleInput = document.getElementById('role');
    
    // Apenas administradores podem editar o cargo
    if (userRole === 'ADMIN') {
        roleInput.disabled = false;
        roleInput.style.backgroundColor = '#fff';
        roleInput.style.cursor = 'text';
    } else {
        roleInput.disabled = true;
        roleInput.style.backgroundColor = '#f5f5f5';
        roleInput.style.cursor = 'not-allowed';
    }
}

async function loadUserFolgas() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch('http://localhost:8080/api/folgas/me', {
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const folgas = await response.json();
            renderFolgas(folgas);
        } else {
            document.getElementById('folgasList').innerHTML = '<p>Nenhuma folga encontrada.</p>';
        }
    } catch (error) {
        console.error('Erro ao carregar folgas:', error);
        document.getElementById('folgasList').innerHTML = '<p>Erro ao carregar folgas.</p>';
    }
}

async function loadRecentActivity() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch('http://localhost:8080/api/auditoria/me/logs', {
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const logs = await response.json();
            renderRecentActivity(logs.slice(0, 10)); // Últimas 10 atividades
        } else {
            const error = await response.json();
            console.error('Erro ao carregar atividades:', error);
            document.getElementById('recentActivity').innerHTML = '<p>Nenhuma atividade recente.</p>';
        }
    } catch (error) {
        console.error('Erro ao carregar atividades:', error);
        document.getElementById('recentActivity').innerHTML = '<p>Erro ao carregar atividades.</p>';
    }
}

function renderFolgas(folgas) {
    const container = document.getElementById('folgasList');

    if (folgas.length === 0) {
        container.innerHTML = '<p>Nenhuma folga encontrada.</p>';
        return;
    }

    const folgasHtml = folgas.map(folga => `
        <div class="folga-item">
            <div class="folga-header">
                <span class="folga-type">${formatTipoFolga(folga.tipo)}</span>
                <span class="folga-status status-${folga.aprovada ? 'approved' : 'pending'}">
                    ${folga.aprovada ? 'Aprovada' : 'Pendente'}
                </span>
            </div>
            <div class="folga-dates">
                ${formatDate(folga.dataInicio)} - ${formatDate(folga.dataFim)}
            </div>
            ${folga.justificativa ? `<div class="folga-justificativa">${folga.justificativa}</div>` : ''}
        </div>
    `).join('');

    container.innerHTML = folgasHtml;
}

function renderRecentActivity(logs) {
    const container = document.getElementById('recentActivity');

    if (logs.length === 0) {
        container.innerHTML = '<p>Nenhuma atividade recente.</p>';
        return;
    }

    const activityHtml = logs.map(log => `
        <div class="activity-item">
            <div class="activity-icon">
                <i class="fas fa-${getActivityIcon(log.categoria)}"></i>
            </div>
            <div class="activity-content">
                <div class="activity-action">${log.acao}</div>
                <div class="activity-time">${formatDateTime(log.timestamp)}</div>
            </div>
        </div>
    `).join('');

    container.innerHTML = activityHtml;
}

async function handleProfileUpdate(e) {
    e.preventDefault();

    const nome = document.getElementById('nome').value;
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const cargo = document.getElementById('role').value;

    // Validação de senha
    if (newPassword && newPassword !== confirmPassword) {
        showToast('As senhas não coincidem', 'error');
        return;
    }

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        
        // Verificar se o usuário pode editar o cargo (baseado no role real armazenado no atributo data-role)
        const userRole = document.getElementById('userRole').getAttribute('data-role');
        let updateData;
        
        if (userRole === 'ADMIN') {
            // Administrador pode editar tudo
            updateData = { 
                nome: nome,
                cargo: cargo
            };
        } else {
            // Usuário comum só pode editar nome e senha
            updateData = { 
                nome: nome
            };
        }

        if (newPassword) {
            updateData.currentPassword = currentPassword;
            updateData.newPassword = newPassword;
        }

        const response = await fetch('http://localhost:8080/api/user/profile', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.token}`
            },
            body: JSON.stringify(updateData)
        });

        if (response.ok) {
            showToast('Perfil atualizado com sucesso!', 'success');
            loadUserProfile(); // Recarregar dados
        } else {
            const error = await response.json();
            showToast(error.erro || 'Erro ao atualizar perfil', 'error');
        }
    } catch (error) {
        console.error('Erro ao atualizar perfil:', error);
        showToast('Erro ao atualizar perfil', 'error');
    }
}

async function handleFolgaSubmit(e) {
    e.preventDefault();

    const tipo = document.getElementById('tipoFolga').value;
    const dataInicio = document.getElementById('dataInicio').value;
    const dataFim = document.getElementById('dataFim').value;
    const justificativa = document.getElementById('justificativa').value;

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const folgaData = {
            tipo: tipo,
            dataInicio: dataInicio,
            dataFim: dataFim,
            justificativa: justificativa
        };

        const response = await fetch('http://localhost:8080/api/folgas/solicitar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.token}`
            },
            body: JSON.stringify(folgaData)
        });

        if (response.ok) {
            showToast('Folga solicitada com sucesso!', 'success');
            closeFolgaModal();
            loadUserFolgas(); // Recarregar folgas
        } else {
            const error = await response.json();
            showToast(error.erro || 'Erro ao solicitar folga', 'error');
        }
    } catch (error) {
        console.error('Erro ao solicitar folga:', error);
        showToast('Erro ao solicitar folga', 'error');
    }
}

function openFolgaModal() {
    document.getElementById('folgaModal').style.display = 'block';
}

function closeFolgaModal() {
    document.getElementById('folgaModal').style.display = 'none';
    document.getElementById('folgaForm').reset();
}

function formatTipoFolga(tipo) {
    const tipos = {
        'FERIAS': 'Férias',
        'ATESTADO': 'Atestado Médico',
        'FALTA_JUSTIFICADA': 'Falta Justificada'
    };
    return tipos[tipo] || tipo;
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('pt-BR');
}

function formatDateTime(dateTimeString) {
    return new Date(dateTimeString).toLocaleString('pt-BR');
}

function getActivityIcon(categoria) {
    const icons = {
        'PONTO': 'clock',
        'ADMIN': 'cog',
        'AUDITORIA': 'eye',
        'RELATORIO': 'file-pdf',
        'SISTEMA': 'server'
    };
    return icons[categoria] || 'circle';
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast-notification ${type}`;
    toast.textContent = message;

    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '8px';
    toast.style.backgroundColor = type === 'success' ? 'rgba(45, 206, 137, 0.95)' : 'rgba(220, 53, 69, 0.95)';
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
