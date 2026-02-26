
const isPaulo = false;
const urlBase = isPaulo ? "http://192.168.15.158" : "";

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
    //document.getElementById('mesAno').value = `${anoAtual}-${mesFormatado}`;
}

async function carregarHistorico() {
    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        if (!auth) sessaoExpirada();
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

        const response = await fetch(`${urlBase}/back/ponto/me?start=${encodeURIComponent(inicioTimestamp)}&end=${encodeURIComponent(fimTimestamp)}`, {
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });

        if (response.ok) {
            const resultado = await response.json();
            console.log('Resultado completo:', resultado);
            console.log('minutosTrabalhados:', resultado.minutosTrabalhados);
            console.log('monthLength:', resultado.monthLength);

            // Verificação defensiva - aceita ambos os formatos de nome de campo
            const minutosTrabalhados = resultado.minutosTrabalhados || resultado.MinutosTrabalhados || 0;
            const monthLength = resultado.monthLength || resultado.monthLength;

            calcularEstatisticas(resultado.registros, minutosTrabalhados, monthLength, primeiroDia, ultimoDia);

            const registros = resultado.registros;
            renderPunchs(registros);
        } else {
            if (response.status == 403) window.location.href = "login.html";
            const error = await response.json();
            showToast(error.erro || 'Erro ao carregar registros', 'error');
        }
    } catch (error) {
        console.error('Erro ao carregar histórico:', error);
        showToast('Erro ao carregar registros', 'error');
    }
}

function calcularEstatisticas(registros, minutosTrabalhados, monthLength, primeiroDia, ultimoDia) {
    if (!registros || registros.length === 0) {
        document.getElementById('totalHoras').textContent = '--:--';
        document.getElementById('diasTrabalhados').textContent = '--';
        document.getElementById('horasExtras').textContent = '--:--';
        document.getElementById('horasDebito').textContent = '--:--';
        return;
    }

    // Calcular dias únicos trabalhados (usando o ID do expediente como referência)
    const diasUnicos = new Set();
    registros.forEach(registro => {
        diasUnicos.add(registro.idExpediente);
    });

    const diasTrabalhados = diasUnicos.size;

    // Quantidade de minutos que o usuário deveria trabalhar nos dias trabalhados
    // Considerando 8 horas (480 minutos) por dia útil trabalhado
    const minutosExigidos = 480 * diasTrabalhados;
    const minutosDebito = minutosExigidos - minutosTrabalhados;

    // Exibir estatísticas
    document.getElementById('diasTrabalhados').textContent = diasTrabalhados;
    document.getElementById('totalHoras').textContent = `${Math.floor(minutosTrabalhados / 60)}:${(minutosTrabalhados % 60).toString().padStart(2, '0')}`;
    document.getElementById('horasExtras').textContent = minutosDebito < 0 ? `${Math.floor(Math.abs(minutosDebito) / 60)}:${(Math.abs(minutosDebito) % 60).toString().padStart(2, '0')}` : '00:00';
    document.getElementById('horasDebito').textContent = minutosDebito > 0 ? `${Math.floor(Math.abs(minutosDebito) / 60)}:${(Math.abs(minutosDebito) % 60).toString().padStart(2, '0')}` : '00:00';
}

// Função para calcular dias úteis (segunda a sexta) no período
function calcularDiasUteis(mesAno, primeiroDia, ultimoDia) {
    let diasUteis = 0;
    const currentDate = new Date(primeiroDia);

    while (currentDate <= ultimoDia) {
        const dayOfWeek = currentDate.getDay();
        // 0 = domingo, 6 = sábado
        if (dayOfWeek !== 0 && dayOfWeek !== 6) {
            diasUteis++;
        }
        currentDate.setDate(currentDate.getDate() + 1);
    }

    return diasUteis;
}


/// RENDERIZAR REGISTROS
function renderPunchs(registros) {
    const listBody = document.getElementById("historyBody");
    listBody.innerHTML = ``;
    registros.forEach(registro => {
        const tr = document.createElement("tr");
        const [data, hora] = registro.timestamp.split("T");
        const [ano, mes, dia] = data.split("-");
        tr.innerHTML = `
        <td>${dia}/${mes}/${ano}</td>
        <td> <span class="tipo-badge tipo-${registro.tipo.toLowerCase()}">${registro.tipo}</span></td>
        <td>${hora}</td>
        `;
        tr.addEventListener("click", () => {
            baixarPdfPonto();
        })
        listBody.appendChild(tr);
    })
}


function baixarPdfPonto() {
    confirm("Baixar pdf deste ponto??")
}

async function gerarFolhaPonto() {

    const mesAno = document.getElementById('mesAno').value;

    if (!mesAno) {
        showToast('Selecione o mês e ano para gerar a folha de ponto', 'error');
        return;
    }
    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) sessaoExpirada();
    const [ano, mes] = mesAno.split('-').map(Number);
    const response = await fetch(`${urlBase}/back/folha-ponto/me?mes=${mes}&ano=${ano}`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${auth.token}`
        }
    });
    if (response.status == 201) {
        showToast("Folha gerada com sucesso", "success");
    } else if (response.status === 403) {
        sessaoExpirada();
    } else {
        const responseError = await response.json();
        showToast(responseError.erro, "error");
    }
}


async function exportarPDF() {
    const mesAno = document.getElementById('mesAno').value;

    if (!mesAno) {
        showToast('Selecione o mês e ano para gerar o PDF', 'error');
        return;
    }

    try {
        const auth = JSON.parse(localStorage.getItem("auth"));
        if (!auth) sessaoExpirada();
        const [ano, mes] = mesAno.split('-').map(Number);

        // Mostrar feedback de carregamento
        showToast('Gerando relatório PDF...', 'info');

        // Gerar o PDF usando o endpoint para funcionários
        const pdfResponse = await fetch(`${urlBase}/back/relatorios/pdf/me`, {
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


function sessaoExpirada() {
    setTimeout(() => {
        showToast("Sua sessão terminou, efetue o login novamente", "info");
    }, 5000)
    localStorage.removeItem("auth");
    window.location.href = "login.html";
}

