document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const btn = document.querySelector('.btn-login');

    // Alternar visibilidade da senha
    togglePassword.addEventListener('click', () => {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);

        // Troca o ícone (simples feedback visual)
        togglePassword.textContent = type === 'password' ? '👁️' : '🔒';
    });

    // Simulação de submissão do formulário
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        // Validação básica
        const company = document.getElementById('company').value;
        const username = document.getElementById('username').value;
        const password = passwordInput.value;

        if (!company || !username || !password) {
            alert('Por favor, preencha todos os campos!');
            return;
        }

        // Efeito de Loading com classe CSS
        btn.classList.add('loading');
        btn.textContent = 'Autenticando...';

        // Simula um atraso de rede
        setTimeout(() => {
            btn.classList.remove('loading');
            btn.textContent = 'Entrar no Sistema';

            // Simula sucesso de login
            alert('✅ Acesso autorizado! Redirecionando para o dashboard...');
            // Em um cenário real, seria: window.location.href = 'dashboard.html';
        }, 1500);
    });

    // Efeito sutil de movimento no blob de fundo baseado no mouse
    document.addEventListener('mousemove', (e) => {
        const blob = document.querySelector('.blob');
        if (blob) {
            const x = e.clientX / window.innerWidth;
            const y = e.clientY / window.innerHeight;
            blob.style.transform = `translate(${x * 50}px, ${y * 50}px)`;
        }
    });

    // Animação de entrada para os inputs
    const inputs = document.querySelectorAll('input, select');
    inputs.forEach((input, index) => {
        input.style.transitionDelay = `${index * 50}ms`;
    });

    // Feedback visual para campos obrigatórios
    const requiredFields = document.querySelectorAll('[required]');
    requiredFields.forEach(field => {
        field.addEventListener('invalid', function(e) {
            e.preventDefault();
            this.style.borderColor = '#ff4444';
            this.style.boxShadow = '0 0 0 2px rgba(255, 68, 68, 0.2)';

            setTimeout(() => {
                this.style.borderColor = '';
                this.style.boxShadow = '';
            }, 2000);
        });

        field.addEventListener('input', function() {
            this.style.borderColor = '';
            this.style.boxShadow = '';
        });
    });
});
