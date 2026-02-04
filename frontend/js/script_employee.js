function showToast(message, type = 'info') {
    console.log('showToast chamada:', message, type);
    const toast = document.createElement('div');
    toast.className = `toast-notification ${type}`;
    toast.textContent = message;
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '8px';
    toast.style.backgroundColor = type === 'success' ? 'rgba(45, 206, 137, 0.95)' : 'rgba(10, 147, 150, 0.95)';
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
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('DOMContentLoaded disparado');
    const clockElement = document.getElementById('digitalClock');
    const dateElement = document.getElementById('currentDate');
    const statusElement = document.getElementById('currentStatus');
    const punchList = document.getElementById('punchList');
    const punchButtons = document.querySelectorAll('.punch-btn');
    const usernameTag = document.getElementById("username");
    const auth = JSON.parse(localStorage.getItem("auth"));

    if (!auth) {
        window.location.href = 'login.html';
        return;
    }

    if (auth.role === 'ADMIN') {
        window.location.href = 'admin.html';
        return;
    }

    if (!clockElement || !dateElement || !statusElement || !usernameTag) {
        console.error('Elementos críticos não encontrados no DOM');
        return;
    }

    usernameTag.textContent = auth.username;
    
    function updateTime() {
        const now = new Date();
        const timeString = now.toLocaleTimeString('pt-BR', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        if (clockElement.textContent !== timeString) {
            clockElement.style.opacity = '0.7';
            setTimeout(() => {
                clockElement.textContent = timeString;
                clockElement.style.opacity = '1';
            }, 200);
        }
        const dateOptions = { day: 'numeric', month: 'long', year: 'numeric' };
        dateElement.textContent = now.toLocaleDateString('pt-BR', dateOptions);
    }

    setInterval(updateTime, 1000);
    updateTime();

    punchButtons.forEach(button => {
        button.addEventListener('click', () => {
            const type = button.getAttribute('data-type');
            const time = clockElement.textContent;
            const value = button.value;
            button.style.transform = 'scale(0.95)';
            setTimeout(() => button.style.transform = '', 150);
            registerPunch(type, time, value);
        });
    });

    async function registerPunch(type, time, value) {
        if (!auth.token || auth.token.trim() === '') {
            showToast('Token de autenticação inválido. Por favor, faça login novamente.', 'error');
            setTimeout(() => window.location.href = 'login.html', 2000);
            return;
        }

        const validTypes = ['ENTRADA', 'INICIO_DESCANSO', 'FIM_DESCANSO', 'SAIDA'];
        if (!validTypes.includes(value)) {
            showToast('Tipo de registro inválido.', 'error');
            return;
        }

        console.log('Registrando ponto:', { type, value, time });

        const dados = { tipo: value, justificativa: "Batendo ponto em dia normal de trabalho" };

        try {
            const result = await fetch("http://localhost:8080/api/ponto", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${auth.token}`
                },
                body: JSON.stringify(dados)
            });

            console.log('Resposta da API:', result.status);

            if (result.status == 201) {
                const response = await result.json();
                console.log('Resposta completa:', response);

                if (punchList.querySelector('.empty-msg')) {
                    punchList.innerHTML = '';
                }

                const li = document.createElement('li');
                li.className = 'punch-item';
                li.style.opacity = '0';
                li.style.transform = 'translateY(10px)';
                li.innerHTML = `<span class="item-type">${type}</span><span class="item-time">${time}</span>`;
                punchList.prepend(li);

                setTimeout(() => {
                    li.style.opacity = '1';
                    li.style.transform = 'translateY(0)';
                    li.style.transition = 'all 0.3s ease-out';
                }, 50);

                statusElement.textContent = `Status: ${type}`;
                statusElement.style.color = '#2dce89';
                statusElement.classList.add('loading');
                setTimeout(() => statusElement.classList.remove('loading'), 2000);

                showToast(`✅ Ponto registrado: ${type} às ${time}`, 'success');

                if (response.hasIncompletePreviousDay && response.warningMessage) {
                    showIncompleteRecordWarning(response.warningMessage);
                }
            } else {
                let errorMessage = 'Erro ao registrar ponto';
                try {
                    const errorData = await result.json();
                    errorMessage = errorData.erro || errorData.message || 'Erro desconhecido';
                } catch (e) {
                    try {
                        const errorText = await result.text();
                        errorMessage = errorText || `Erro HTTP: ${result.status}`;
                    } catch (e2) {
                        errorMessage = `Erro HTTP: ${result.status}`;
                    }
                }
                console.error('Erro ao registrar ponto:', errorMessage);
                showToast(`❌ ${errorMessage}`, 'error');
            }
        } catch (error) {
            console.error('Erro na requisição:', error);
            showToast('❌ Erro de conexão com o servidor. Verifique sua internet.', 'error');
        }
    }

    function showIncompleteRecordWarning(message) {
        const modal = document.createElement('div');
        modal.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.7);display:flex;justify-content:center;align-items:center;z-index:2000;backdrop-filter:blur(5px)';
        const modalContent = document.createElement('div');
        modalContent.style.cssText = 'background:white;padding:30px;border-radius:12px;max-width:500px;width:90%;box-shadow:0 8px 25px rgba(0,0,0,0.3);text-align:center;position:relative';
        const closeButton = document.createElement('button');
        closeButton.style.cssText = 'position:absolute;top:10px;right:10px;background:none;border:none;font-size:24px;cursor:pointer;color:#666';
        closeButton.textContent = '×';
        closeButton.onclick = () => modal.remove();
        const title = document.createElement('h3');
        title.style.cssText = 'color:#e74c3c;margin:10px 0 20px 0';
        title.textContent = '⚠️ ATENÇÃO';
        const messageElement = document.createElement('p');
        messageElement.style.cssText = 'color:#333;font-size:16px;line-height:1.6;margin-bottom:25px';
        messageElement.textContent = message;
        const okButton = document.createElement('button');
        okButton.style.cssText = 'background:#3498db;color:white;border:none;padding:12px 25px;border-radius:6px;font-size:16px;font-weight:600;cursor:pointer;transition:background-color 0.3s';
        okButton.textContent = 'Entendi';
        okButton.onmouseover = () => okButton.style.backgroundColor = '#2980b9';
        okButton.onmouseout = () => okButton.style.backgroundColor = '#3498db';
        okButton.onclick = () => modal.remove();
        modalContent.append(closeButton, title, messageElement, okButton);
        modal.appendChild(modalContent);
        document.body.appendChild(modal);
        modalContent.style.transform = 'scale(0.9)';
        modalContent.style.opacity = '0';
        setTimeout(() => {
            modalContent.style.transform = 'scale(1)';
            modalContent.style.opacity = '1';
            modalContent.style.transition = 'all 0.3s ease-out';
        }, 10);
    }

    function animateOnLoad() {
        document.querySelectorAll('.clock-card, .actions-grid, .history-card').forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            setTimeout(() => {
                el.style.transition = 'all 0.5s ease-out';
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }

    setTimeout(animateOnLoad, 200);
    loadTodayPunches();
    initLogout();  // Inicializar botão de logout
});

function initLogout() {
    console.log('initLogout chamada');
    const logoutBtn = document.getElementById('btnLogout');
    if (logoutBtn) {
        logoutBtn.removeEventListener('click', handleLogout);
        logoutBtn.addEventListener('click', handleLogout);
        console.log('Logout inicializado corretamente');
    } else {
        console.error('Botão de logout não encontrado');
    }
}

function handleLogout() {
    console.log('handleLogout chamada');
    console.log('showToast existe?', typeof showToast);
    if (confirm('Deseja realmente sair do sistema?')) {
        showToast('👋 Saindo do sistema... Até logo!', 'info');
        setTimeout(() => {
            localStorage.removeItem("auth");
            window.location.href = 'login.html';
        }, 1000);
    }
}

async function loadTodayPunches() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const today = new Date();
        const dateStr = today.toISOString().split('T')[0];
        const response = await fetch(`http://localhost:8080/api/ponto/historico?date=${dateStr}`, {
            headers: { "Authorization": `Bearer ${auth.token}` }
        });
        if (response.ok) {
            const punches = await response.json();
            renderPunchHistory(punches);
        } else {
            console.error('Erro ao carregar histórico de ponto');
        }
    } catch (error) {
        console.error('Erro ao carregar histórico de ponto:', error);
    }
}

function renderPunchHistory(punches) {
    const container = document.getElementById('punchList');
    container.innerHTML = '';
    if (punches.length === 0) {
        container.innerHTML = '<li class="empty-msg">Nenhum registro hoje.</li>';
        return;
    }
    punches.forEach(punch => {
        const li = document.createElement('li');
        li.className = 'punch-item';
        li.innerHTML = `<span class="item-type">${punch.tipo}</span><span class="item-time">${punch.horario}</span>`;
        container.appendChild(li);
    });
}