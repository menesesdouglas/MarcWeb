document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

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
        
        const btn = document.querySelector('.btn-login');
        const originalText = btn.textContent;
        
        // Efeito de Loading
        btn.textContent = 'Autenticando...';
        btn.style.opacity = '0.7';
        btn.style.pointerEvents = 'none';

        // Simula um atraso de rede
        setTimeout(() => {
            alert('Acesso autorizado! Redirecionando para o dashboard...');
            // window.location.href = 'dashboard.html';
            btn.textContent = originalText;
            btn.style.opacity = '1';
            btn.style.pointerEvents = 'all';
        }, 1500);
    });

    // Efeito sutil de movimento no blob de fundo baseado no mouse
    document.addEventListener('mousemove', (e) => {
        const blob = document.querySelector('.blob');
        const x = e.clientX / window.innerWidth;
        const y = e.clientY / window.innerHeight;
        
        blob.style.transform = `translate(${x * 50}px, ${y * 50}px)`;
    });
});