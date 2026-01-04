document.addEventListener('DOMContentLoaded', () => {
    const clockElement = document.getElementById('digitalClock');
    const dateElement = document.getElementById('currentDate');
    const statusElement = document.getElementById('currentStatus');
    const punchList = document.getElementById('punchList');
    const punchButtons = document.querySelectorAll('.punch-btn');

    // 1. Atualizar Relógio em Tempo Real
    function updateTime() {
        const now = new Date();
        
        // Formatar Hora
        const timeString = now.toLocaleTimeString('pt-BR', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        clockElement.textContent = timeString;

        // Formatar Data
        const dateOptions = { day: 'numeric', month: 'long', year: 'numeric' };
        dateElement.textContent = now.toLocaleDateString('pt-BR', dateOptions);
    }

    setInterval(updateTime, 1000);
    updateTime();

    // 2. Lógica de Marcação de Ponto
    punchButtons.forEach(button => {
        button.addEventListener('click', () => {
            const type = button.getAttribute('data-type');
            const time = clockElement.textContent;
            
            registerPunch(type, time);
        });
    });

    function registerPunch(type, time) {
        // Remover mensagem de lista vazia se for o primeiro item
        if (punchList.querySelector('.empty-msg')) {
            punchList.innerHTML = '';
        }

        // Criar novo item na lista
        const li = document.createElement('li');
        li.className = 'punch-item';
        li.innerHTML = `
            <span class="item-type">${type}</span>
            <span class="item-time">${time}</span>
        `;
        
        // Adicionar ao topo da lista
        punchList.prepend(li);

        // Atualizar Badge de Status
        statusElement.textContent = `Status: ${type}`;
        statusElement.style.color = '#2dce89'; // Verde sucesso

        // Feedback visual simples
        alert(`Ponto registrado: ${type} às ${time}`);
    }

    // 3. Botões Adicionais
    document.getElementById('btnReport').addEventListener('click', () => {
        alert('Gerando folha de ponto em PDF... O download começará em instantes.');
    });

    document.getElementById('btnLogout').addEventListener('click', () => {
        if(confirm('Deseja realmente sair do sistema?')) {
            window.location.href = 'login.html';
        }
    });
});