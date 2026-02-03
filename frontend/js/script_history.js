document.addEventListener('DOMContentLoaded', () => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) {
        window.location.href = 'login.html';
        return;
    }

    // Configurar mês-ano padrão (mês atual)
    configurarMesAnoPadrao();

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

    // Enter no campo de mês-ano
    document.getElementById('mesAno').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') carregarHistorico();
    });
}

function configurarMesAnoPadrao() {
    const hoje = new Date();
    const mesAtual = hoje.getMonth() + 1;
    const anoAtual = hoje.getFullYear();
    
    const mesFormatado = mesAtual < 10 ? `0${mesAtual}` : `${mesAtual}`;
    document.getElementById('mesAno').value = `${anoAtual}-${mesFormatado}`;
}

async function carregarHistorico() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const historyBody = document.getElementById('historyBody');

    // Mostrar loading
    loadingIndicator.style.display = 'block';
    historyBody.innerHTML = '<tr><td colspan="5" class="loading-state"><i class="fas fa-spinner fa-spin"></i> Carregando registros...</td></tr>';

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const mesAno = document.getElementById('mesAno').value;

        if (!mesAno) {
            showToast('Selecione o mês e ano', 'error');
            return;
        }

        // Converter mês-ano para timestamps (início e fim do mês)
        const [ano, mes] = mesAno.split('-').map(Number);
        const primeiroDia = new Date(ano, mes - 1, 1);
        const ultimoDia = new Date(ano, mes, 0);
        
        const inicioTimestamp = primeiroDia.toISOString().split('T')[0] + "T00:00:00";
        const fimTimestamp = ultimoDia.toISOString().split('T')[0] + 'T23:59:59';
        
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

async function exportarPDF() {
    const mesAno = document.getElementById('mesAno').value;

    if (!mesAno) {
        showToast('Selecione o mês e ano para gerar o PDF', 'error');
        return;
    }

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        const [ano, mes] = mesAno.split('-').map(Number);

        // Mostrar feedback de carregamento
        showToast('Gerando relatório PDF...', 'info');

        // Gerar o PDF usando o endpoint para funcionários
        const pdfResponse = await fetch('http://localhost:8080/api/relatorios/pdf/me', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.token}`
            },
            body: JSON.stringify({
                userId: auth.id,
                mes: parseInt(mes),
                ano: parseInt(ano)
            })
        });

        if (pdfResponse.ok) {
            const pdfBase64 = await pdfResponse.text();

            // Criar link para download
            const link = document.createElement('a');
            link.href = `data:application/pdf;base64,${pdfBase64}`;
            link.download = `folha_ponto_${mesAno}.pdf`;
            link.click();

            showToast('Relatório PDF gerado e baixado com sucesso!', 'success');
        } else {
            // Tratar respostas de erro
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
