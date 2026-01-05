// Efeito de rolagem suave para links internos
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// Efeito de revelação ao rolar a página (Scroll Reveal)
function reveal() {
    var reveals = document.querySelectorAll(".reveal");
    for (var i = 0; i < reveals.length; i++) {
        var windowHeight = window.innerHeight;
        var elementTop = reveals[i].getBoundingClientRect().top;
        var elementVisible = 150;
        if (elementTop < windowHeight - elementVisible) {
            reveals[i].classList.add("active");
        }
    }
}

window.addEventListener("scroll", reveal);

// Mudar estilo da navbar ao rolar
window.addEventListener("scroll", function() {
    const navbar = document.querySelector(".navbar");
    if (window.scrollY > 50) {
        navbar.style.padding = "0.8rem 0";
        navbar.style.borderBottom = "1px solid rgba(0, 95, 115, 0.3)";
    } else {
        navbar.style.padding = "1.5rem 0";
        navbar.style.borderBottom = "1px solid rgba(255, 255, 255, 0.05)";
    }
});

// Inicializar a função de revelação
reveal();