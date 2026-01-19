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
            alert("Ponto batido com sucesso")
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
        } else {
            const erro = await result.json();
            alert(erro.erro)
        }



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
