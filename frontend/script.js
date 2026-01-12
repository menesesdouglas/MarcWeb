// Efeito de rolagem suave para links internos
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const targetId = this.getAttribute('href');

        if (targetId === '#learn-more') {
            // Scroll to features section
            const features = document.querySelector('.system-features');
            if (features) {
                features.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        } else {
            // Scroll to other sections
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        }
    });
});

// Animação de entrada para elementos com scroll
function checkScrollAnimations() {
    const elements = document.querySelectorAll('.feature-card, .benefit-item, .info-step');

    elements.forEach(element => {
        const elementPosition = element.getBoundingClientRect().top;
        const windowHeight = window.innerHeight;

        if (elementPosition < windowHeight - 100) {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }
    });
}

// Animação de entrada para a navbar ao rolar
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.welcome-header');
    if (window.scrollY > 50) {
        navbar.style.padding = '1rem 2rem';
        navbar.style.background = 'rgba(10, 14, 18, 0.95)';
    } else {
        navbar.style.padding = '1.5rem 2rem';
        navbar.style.background = 'rgba(10, 14, 18, 0.8)';
    }

    checkScrollAnimations();
});

// Efeito de hover para botões
document.querySelectorAll('.btn-primary, .btn-secondary, .btn-cta').forEach(button => {
    button.addEventListener('mouseenter', function() {
        if (this.classList.contains('btn-primary') || this.classList.contains('btn-cta')) {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 10px 20px rgba(0, 95, 115, 0.3)';
        }
    });

    button.addEventListener('mouseleave', function() {
        if (this.classList.contains('btn-primary') || this.classList.contains('btn-cta')) {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = 'none';
        }
    });
});

// Inicializar animações quando a página carregar
window.addEventListener('load', function() {
    // Animação inicial para elementos
    const elements = document.querySelectorAll('.feature-card, .benefit-item, .info-step');
    elements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'all 0.8s ease-out';
    });

    // Iniciar animação após um pequeno delay com atraso progressivo
    elements.forEach((element, index) => {
        setTimeout(() => {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 150);
    });

    // Verificar animações de scroll inicialmente
    checkScrollAnimations();
});

// Efeito de destaque para cards de recursos
document.querySelectorAll('.feature-card').forEach(card => {
    card.addEventListener('mouseenter', function() {
        this.style.borderColor = 'var(--petrol-light)';
        this.style.transform = 'translateY(-8px)';
    });

    card.addEventListener('mouseleave', function() {
        this.style.borderColor = 'rgba(255, 255, 255, 0.05)';
        this.style.transform = 'translateY(0)';
    });
});
