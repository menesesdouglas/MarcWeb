document.addEventListener('DOMContentLoaded', () => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) {
        window.location.href = 'login.html';
        return;
    }

    // Configurar datas padrão (mês atual)
    configurarDatasPadrao();

    // Configurar eventos
    setupEventListeners();

    // Carregar dados iniciais
    carregarHistorico();
});

function setupEventListeners() {
    // Logout
    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Deseja realmente sair do sistema?')) {
            localStorage.removeItem("auth");
            window.location.href = 'login.html';
        }
    });

    // Enter nos filtros
    document.getElementById('dataInicio').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') carregarHistorico();
    });
    document.getElementById('dataFinal').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') carregarHistorico();
    });
    document.getElementById('tipoRegistro').addEventListener('change', carregarHistorico);
}

function configurarDatasPadrao() {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    const ultimoDiaMes = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);

    document.getElementById('dataInicio').value = formatarDataParaInput(primeiroDiaMes);
    document.getElementById('dataFinal').value = formatarDataParaInput(ultimoDiaMes);
}

function formatarDataParaInput(data) {
    return data.toISOString().split('T')[0];
}

async function carregarHistorico() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const historyBody = document.getElementById('historyBody');

    // Mostrar loading
    loadingIndicator.style.display = 'block';
    historyBody.innerHTML = '<tr><td colspan="5" class="loading-state"><i class="fas fa-spinner fa-spin"></i> Carregando registros...</td></tr>';

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const dataInicio = document.getElementById('dataInicio').value;
        const dataFinal = document.getElementById('dataFinal').value;

        if (!dataInicio || !dataFinal) {
            showToast('Selecione as datas inicial e final', 'error');
            return;
        }

        // Converter para timestamps
        const inicioTimestamp = new Date(dataInicio + 'T00:00:00').toISOString().replace(".000Z", "");
        const fimTimestamp = new Date(dataFinal + 'T23:59:59').toISOString().replace(".000Z", "");
        const response = await fetch(`http://localhost:8080/api/ponto/me?start=${encodeURIComponent(inicioTimestamp)}&end=${encodeURIComponent(fimTimestamp)}`, {
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const resultado = await response.json();
            renderizarHistorico(resultado.registros);
            calcularEstatisticas(resultado.registros, resultado.minutosTrabalhados);
        } else {
            if (response.status == 403) window.location.href = "login.html";
            const error = await response.json();
            historyBody.innerHTML = `<tr><td colspan="5" class="error-state"><i class="fas fa-exclamation-triangle"></i> ${error.erro || 'Erro ao carregar registros'}</td></tr>`;
        }
    } catch (error) {
        console.error('Erro ao carregar histórico:', error);
        historyBody.innerHTML = '<tr><td colspan="5" class="error-state"><i class="fas fa-exclamation-triangle"></i> Erro ao carregar registros</td></tr>';
    } finally {
        loadingIndicator.style.display = 'none';
    }
}

function renderizarHistorico(registros) {
    const historyBody = document.getElementById('historyBody');

    if (!registros || registros.length === 0) {
        historyBody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-state">
                    <i class="fas fa-calendar-times"></i>
                    <p>Nenhum registro encontrado para o período selecionado.</p>
                </td>
            </tr>
        `;
        return;
    }

    // Ordenar por data/hora decrescente
    registros.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    const registrosHtml = registros.map(registro => {
        const data = new Date(registro.timestamp);
        const tipoFormatado = formatarTipoRegistro(registro.tipo);
        const localizacao = registro.latitude && registro.longitude ?
            `${registro.latitude.toFixed(4)}, ${registro.longitude.toFixed(4)}` : 'N/A';

        return `
            <tr>
                <td>${data.toLocaleDateString('pt-BR')}</td>
                <td><span class="tipo-badge tipo-${registro.tipo.toLowerCase()}">${tipoFormatado}</span></td>
                <td>${data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</td>
                <td>${localizacao}</td>
                <td><span class="status-badge status-success">Validado</span></td>
            </tr>
        `;
    }).join('');

    historyBody.innerHTML = registrosHtml;
}

function calcularEstatisticas(registros, minutosTrabalhados) {
    if (!registros || registros.length === 0) {
        document.getElementById('totalHoras').textContent = '--:--';
        document.getElementById('diasTrabalhados').textContent = '--';
        document.getElementById('horasExtras').textContent = '--:--';
        document.getElementById('horasDebito').textContent = '--:--';
        return;
    }

    // Calcular estatísticas básicas
    const diasUnicos = new Set();

    registros.forEach(registro => {
        const data = new Date(registro.timestamp);
        diasUnicos.add(data.toDateString());
    });

    // QUANTIDADE DE MINUTOS QUE O USUÁRIO PRECISA TER TRABALHADO NOS DIAS REGISTRADOS
    const minutosExigidos = 480 * diasUnicos.size;
    const minutosDebito = minutosExigidos - minutosTrabalhados;



    // Para cálculo mais preciso de horas, seria necessário agrupar por dia
    // e calcular diferenças entre entrada/saída
    // Por enquanto, vamos mostrar estatísticas básicas
    document.getElementById('diasTrabalhados').textContent = diasUnicos.size;
    document.getElementById('totalHoras').textContent = `${Math.floor(minutosTrabalhados / 60)}:${(minutosTrabalhados % 60).toString().padStart(2, '0')}`;
    document.getElementById('horasExtras').textContent = minutosDebito < 0 ? `${Math.floor(Math.abs(minutosDebito) / 60)}:${(Math.abs(minutosDebito) % 60).toString().padStart(2, '0')}` : '00:00';
    document.getElementById('horasDebito').textContent = minutosDebito > 0 ? `${Math.floor(Math.abs(minutosDebito) / 60)}:${(Math.abs(minutosDebito) % 60).toString().padStart(2, '0')}` : '00:00';
}

function formatarTipoRegistro(tipo) {
    const tipos = {
        'ENTRADA': 'Entrada',
        'INICIO_DESCANSO': 'Início Descanso',
        'FIM_DESCANSO': 'Fim Descanso',
        'SAIDA': 'Saída'
    };
    return tipos[tipo] || tipo;
}

function exportarCSV() {
    const historyBody = document.getElementById('historyBody');
    const rows = historyBody.querySelectorAll('tr');

    if (rows.length === 0 || rows[0].classList.contains('empty-state') || rows[0].classList.contains('error-state')) {
        showToast('Nenhum dado para exportar', 'error');
        return;
    }

    const csvContent = [
        ['Data', 'Tipo', 'Horário', 'Localização', 'Status'],
        ...Array.from(rows).map(row => {
            const cells = row.querySelectorAll('td');
            return Array.from(cells).map(cell => {
                // Remover elementos HTML e pegar apenas o texto
                const badge = cell.querySelector('.tipo-badge, .status-badge');
                return badge ? badge.textContent.trim() : cell.textContent.trim();
            });
        })
    ];

    // Converter para CSV
    const csvString = csvContent.map(row =>
        row.map(field => `"${field.replace(/"/g, '""')}"`).join(',')
    ).join('\n');

    // Criar e baixar arquivo
    const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', `historico_ponto_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('Arquivo CSV exportado com sucesso!', 'success');
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
