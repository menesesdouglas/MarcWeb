
const urlBase = "http://192.168.15.158";


document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const btn = document.querySelector('.btn-login');
    const selectCompany = document.getElementById("company");
    let companysLoaded = false;


    // Alternar visibilidade da senha
    togglePassword.addEventListener('click', () => {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);

        // Troca o ícone 
        togglePassword.textContent = type === 'password' ? '👁️' : '🔒';
    });

    // Simulação de submissão do formulário
    loginForm.addEventListener('submit', async (e) => {
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
            //alert('✅ Acesso autorizado! Redirecionando para o dashboard...');
            // Em um cenário real, seria: window.location.href = 'dashboard.html';
        }, 1500);


        /// -->>  LOGIN VIA API <<<---
        const dadosLogin = {
            username: username,
            password: password
        }
        const loginResponse = await fetch(`${urlBase}/back/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Tenant-Id": selectCompany.value
            },
            body: JSON.stringify(dadosLogin)
        });
        const resultado = await loginResponse.json();
        if (loginResponse.status == 200) {
            const auth = {
                id: resultado.userId,
                role: resultado.role,
                token: resultado.token,
                username: resultado.username
            }
            localStorage.setItem("auth", JSON.stringify(auth));
            window.location.href = auth.role == "ADMIN" ? "admin.html" : "employee.html";
        } else {
            alert(resultado.erro);
        }



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
        field.addEventListener('invalid', function (e) {
            e.preventDefault();
            this.style.borderColor = '#ff4444';
            this.style.boxShadow = '0 0 0 2px rgba(255, 68, 68, 0.2)';

            setTimeout(() => {
                this.style.borderColor = '';
                this.style.boxShadow = '';
            }, 2000);
        });

        field.addEventListener('input', function () {
            this.style.borderColor = '';
            this.style.boxShadow = '';
        });
    });

    loadTenants(true);

    selectCompany.addEventListener("click", () => {
        if (!companysLoaded) {
            loadTenants(false);
            companysLoaded = true;
        }
    })
});


async function loadTenants(isAutomatic) {

    const currentTenantIdentification = localStorage.getItem("tenant_identification");
    const select = document.getElementById("company");
    if (currentTenantIdentification && isAutomatic) {
        const currentTenant = JSON.parse(currentTenantIdentification);
        const option = document.createElement("option");
        option.addEventListener("click", (event) => {
            eventoTest(event);
        })
        option.value = currentTenant.tenant_id;
        option.textContent = currentTenant.nome_empresa;
        select.appendChild(option);
        select.value = option.value;
        return;
    }

    const response = await fetch(`${urlBase}/back/tenant`);

    if (response.ok) {
        select.innerHTML = `<option  value="" disabled selected>Selecione sua empresa
                            </option> `;
        const tenants = await response.json();
        tenants.forEach(tenant => {
            const option = document.createElement("option");
            option.addEventListener("click", (event) => {
                eventoTest(event);
            })
            option.value = tenant.tenantId;
            option.textContent = tenant.nomeEmpresa;

            select.appendChild(option);
        })
    }
}

function eventoTest(event) {
    const currentTenant = {
        nome_empresa: event.target.textContent,
        tenant_id: event.target.value
    };
    try {
        localStorage.removeItem("tenant_identification");
        localStorage.setItem("tenant_identification", JSON.stringify(currentTenant));
    } catch (error) {
        alert("Algo deu errado")
    }
}
