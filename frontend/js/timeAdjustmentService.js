class TimeAdjustmentService {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api/ponto/ajuste';
    }

    async getTimeAdjustmentHistory(userId, startDate, endDate) {
        try {
            const auth = JSON.parse(localStorage.getItem("auth"));
            if (!auth) throw new Error("Usuário não autenticado");

            // This would require a new endpoint to get adjustment history
            // For now, we'll return the recent punches which include adjustments
            const response = await fetch(`http://localhost:8080/api/ponto/me?start=${startDate}&end=${endDate}`, {
                headers: {
                    "Authorization": `Bearer ${auth.token}`
                }
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.erro || 'Erro ao buscar histórico de ajustes');
            }

            return await response.json();
        } catch (error) {
            console.error('Erro ao buscar histórico de ajustes:', error);
            throw error;
        }
    }

    async createTimeAdjustment(adjustmentData) {
        try {
            const auth = JSON.parse(localStorage.getItem("auth"));
            if (!auth) throw new Error("Usuário não autenticado");

            const response = await fetch(this.baseUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${auth.token}`
                },
                body: JSON.stringify(adjustmentData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.erro || 'Erro ao realizar ajuste de ponto');
            }

            return await response.text();
        } catch (error) {
            console.error('Erro ao realizar ajuste de ponto:', error);
            throw error;
        }
    }

    validateAdjustmentData(data) {
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

    formatAdjustmentForDisplay(adjustment) {
        return {
            employeeName: adjustment.user?.nome || 'Desconhecido',
            date: new Date(adjustment.timestamp).toLocaleDateString('pt-BR'),
            time: new Date(adjustment.timestamp).toLocaleTimeString('pt-BR'),
            type: adjustment.tipo,
            justification: adjustment.justificativa || 'Sem justificativa',
            isAdjustment: adjustment.ip === 'AJUSTE_ADMIN'
        };
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = TimeAdjustmentService;
}