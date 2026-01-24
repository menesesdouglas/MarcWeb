document.addEventListener('DOMContentLoaded', () => {
    const clockElement = document.getElementById('digitalClock');
    const dateElement = document.getElementById('currentDate');
    const statusElement = document.getElementById('currentStatus');
    const punchList = document.getElementById('punchList');
    const punchButtons = document.querySelectorAll('.punch-btn');
    const usernameTag = document.getElementById("username");
    const auth = JSON.parse(localStorage.getItem("auth"));
    usernameTag.textContent = auth.username;
    // 1. Atualizar Relógio em Tempo Real com animação suave
    function updateTime() {
        const now = new Date();

        // Formatar Hora
        const timeString = now.toLocaleTimeString('pt-BR', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });

        // Animação suave para o relógio
        if (clockElement.textContent !== timeString) {
            clockElement.style.opacity = '0.7';
            setTimeout(() => {
                clockElement.textContent = timeString;
                clockElement.style.opacity = '1';
            }, 200);
        }

        // Formatar Data
        const dateOptions = { day: 'numeric', month: 'long', year: 'numeric' };
        dateElement.textContent = now.toLocaleDateString('pt-BR', dateOptions);
    }

    setInterval(updateTime, 1000);
    updateTime();

    // 2. Lógica de Marcação de Ponto com animações modernas
    punchButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const type = button.getAttribute('data-type');
            const time = clockElement.textContent;
            const value = button.value;

            // Animação de clique no botão
            button.style.transform = 'scale(0.95)';
            setTimeout(() => {
                button.style.transform = '';
            }, 150);

            registerPunch(type, time, value);
        });
    });

    async function registerPunch(type, time, value) {

        /// -- ☻☻☻☻☻ VIA API
        const dados = {
            tipo: value, // ENTRADA, INICIO_DESCANSO, FIM_DESCANSO, SAIDA
            latitude: -23.5505,
            longitude: -46.6333,
            precisao: 10,
            justificativa: "Batendo ponto em dia normal de trabalho"
        }

        const result = await fetch("http://localhost:8080/api/ponto", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${auth.token}`
            },
            body: JSON.stringify(dados)
        });

        if (result.status == 201) {
            const response = await result.json();

            // Remover mensagem de lista vazia se for o primeiro item
            if (punchList.querySelector('.empty-msg')) {
                punchList.innerHTML = '';
            }

            // Criar novo item na lista com animação
            const li = document.createElement('li');
            li.className = 'punch-item';
            li.style.opacity = '0';
            li.style.transform = 'translateY(10px)';
            li.innerHTML = `
                <span class="item-type">${type}</span>
                <span class="item-time">${time}</span>
            `;

            // Adicionar ao topo da lista
            punchList.prepend(li);

            // Animação de entrada
            setTimeout(() => {
                li.style.opacity = '1';
                li.style.transform = 'translateY(0)';
                li.style.transition = 'all 0.3s ease-out';
            }, 50);

            // Atualizar Badge de Status com animação
            statusElement.textContent = `Status: ${type}`;
            statusElement.style.color = '#2dce89'; // Verde sucesso
            statusElement.classList.add('loading');

            // Remover animação após 2 segundos
            setTimeout(() => {
                statusElement.classList.remove('loading');
            }, 2000);

            // Feedback visual moderno (toast notification)
            showToast(`✅ Ponto registrado: ${type} às ${time}`, 'success');

            // Verificar se há aviso de registro incompleto do dia anterior
            if (response.hasIncompletePreviousDay && response.warningMessage) {
                showIncompleteRecordWarning(response.warningMessage);
            }
        } else {
            const erro = await result.json();
            alert(erro.erro)
        }

    }

    // Função para mostrar aviso de registro incompleto
    function showIncompleteRecordWarning(message) {
        const modal = document.createElement('div');
        modal.style.position = 'fixed';
        modal.style.top = '0';
        modal.style.left = '0';
        modal.style.width = '100%';
        modal.style.height = '100%';
        modal.style.backgroundColor = 'rgba(0, 0, 0, 0.7)';
        modal.style.display = 'flex';
        modal.style.justifyContent = 'center';
        modal.style.alignItems = 'center';
        modal.style.zIndex = '2000';
        modal.style.backdropFilter = 'blur(5px)';

        const modalContent = document.createElement('div');
        modalContent.style.backgroundColor = 'white';
        modalContent.style.padding = '30px';
        modalContent.style.borderRadius = '12px';
        modalContent.style.maxWidth = '500px';
        modalContent.style.width = '90%';
        modalContent.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.3)';
        modalContent.style.textAlign = 'center';
        modalContent.style.position = 'relative';

        const closeButton = document.createElement('button');
        closeButton.style.position = 'absolute';
        closeButton.style.top = '10px';
        closeButton.style.right = '10px';
        closeButton.style.background = 'none';
        closeButton.style.border = 'none';
        closeButton.style.fontSize = '24px';
        closeButton.style.cursor = 'pointer';
        closeButton.style.color = '#666';
        closeButton.textContent = '×';
        closeButton.onclick = () => {
            modal.remove();
        };

        const title = document.createElement('h3');
        title.style.color = '#e74c3c';
        title.style.marginTop = '10px';
        title.style.marginBottom = '20px';
        title.textContent = '⚠️ ATENÇÃO';

        const messageElement = document.createElement('p');
        messageElement.style.color = '#333';
        messageElement.style.fontSize = '16px';
        messageElement.style.lineHeight = '1.6';
        messageElement.style.marginBottom = '25px';
        messageElement.textContent = message;

        const okButton = document.createElement('button');
        okButton.style.backgroundColor = '#3498db';
        okButton.style.color = 'white';
        okButton.style.border = 'none';
        okButton.style.padding = '12px 25px';
        okButton.style.borderRadius = '6px';
        okButton.style.fontSize = '16px';
        okButton.style.fontWeight = '600';
        okButton.style.cursor = 'pointer';
        okButton.style.transition = 'background-color 0.3s';
        okButton.textContent = 'Entendi';
        okButton.onmouseover = () => {
            okButton.style.backgroundColor = '#2980b9';
        };
        okButton.onmouseout = () => {
            okButton.style.backgroundColor = '#3498db';
        };
        okButton.onclick = () => {
            modal.remove();
        };

        modalContent.appendChild(closeButton);
        modalContent.appendChild(title);
        modalContent.appendChild(messageElement);
        modalContent.appendChild(okButton);
        modal.appendChild(modalContent);
        document.body.appendChild(modal);

        // Animação de entrada
        modalContent.style.transform = 'scale(0.9)';
        modalContent.style.opacity = '0';
        setTimeout(() => {
            modalContent.style.transform = 'scale(1)';
            modalContent.style.opacity = '1';
            modalContent.style.transition = 'all 0.3s ease-out';
        }, 10);
    }

    // 3. Sistema de Toast Notifications
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast-notification ${type}`;
        toast.textContent = message;

        // Estilos inline para o toast
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

        // Animação de entrada
        setTimeout(() => {
            toast.style.transform = 'translateY(0)';
            toast.style.opacity = '1';
        }, 10);

        // Remover após 3 segundos
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(20px)';
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }

    // 4. Botões Adicionais com feedback melhorado
    document.getElementById('btnReport').addEventListener('click', () => {
        showToast('📄 Gerando folha de ponto... Aguarde um momento.', 'info');

        // Simular processo de geração
        setTimeout(() => {
            showToast('✅ Folha de ponto gerada com sucesso!', 'success');
        }, 1500);
    });

    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Deseja realmente sair do sistema?')) {
            showToast('👋 Saindo do sistema... Até logo!', 'info');
            setTimeout(() => {
                localStorage.removeItem("auth");
                window.location.href = 'login.html';
            }, 1000);
        }
    });

    // 5. Animação inicial dos elementos
    function animateOnLoad() {
        const elements = document.querySelectorAll('.clock-card, .actions-grid, .history-card');
        elements.forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            setTimeout(() => {
                el.style.transition = 'all 0.5s ease-out';
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }

    // Iniciar animações após um pequeno delay
    setTimeout(animateOnLoad, 200);
});