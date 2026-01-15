document.addEventListener('DOMContentLoaded', function() {
    // Form validation and submission
    const financialForm = document.getElementById('financialContactForm');
    const submitBtn = document.getElementById('submitBtn');
    const formStatus = document.getElementById('formStatus');

    if (financialForm) {
        financialForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // Validate form
            if (validateForm()) {
                // Show loading state
                showLoadingState();

                // Collect form data
                const formData = collectFormData();

                // Send data via email
                sendEmail(formData);
            }
        });

        // Add input validation
        addInputValidation();
    }

    // Form validation function
    function validateForm() {
        let isValid = true;
        const requiredFields = financialForm.querySelectorAll('[required]');

        // Clear previous error states
        requiredFields.forEach(field => {
            field.style.borderColor = 'rgba(255, 255, 255, 0.1)';
        });

        // Check required fields
        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                field.style.borderColor = '#ff6b6b';
                isValid = false;
            }
        });

        // Validate email format
        const emailField = document.getElementById('contactEmail');
        if (emailField && emailField.value) {
            if (!validateEmail(emailField.value)) {
                emailField.style.borderColor = '#ff6b6b';
                isValid = false;
            }
        }

        if (!isValid) {
            showStatus('Por favor, preencha todos os campos obrigatórios corretamente.', 'error');
        }

        return isValid;
    }

    // Email validation
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Collect form data
    function collectFormData() {
        return {
            companyName: document.getElementById('companyName').value,
            companySize: document.getElementById('companySize').value,
            employeeCount: document.getElementById('employeeCount').value,
            contactName: document.getElementById('contactName').value,
            contactEmail: document.getElementById('contactEmail').value,
            contactPhone: document.getElementById('contactPhone').value,
            interestLevel: document.getElementById('interestLevel').value,
            message: document.getElementById('message').value,
            howFound: document.getElementById('howFound').value
        };
    }

    // Show loading state
    function showLoadingState() {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loading-spinner"></span><span>Enviando...</span>';

        // Clear previous status
        formStatus.textContent = '';
        formStatus.className = 'form-status';
    }

    // Send email using mailto protocol
    function sendEmail(formData) {
        // Format the email subject and body
        const subject = encodeURIComponent('Solicitação de Demonstração - MarcWeb');
        const body = encodeURIComponent(formatEmailBody(formData));

        // Create mailto link
        const mailtoLink = `mailto:techhub.financeiro@gmail.com?subject=${subject}&body=${body}`;

        // Open mail client after a short delay to show loading state
        setTimeout(() => {
            window.location.href = mailtoLink;

            // Show success message
            showStatus('Sua solicitação foi preparada! Por favor, confirme o envio no seu cliente de e-mail.', 'success');

            // Reset form
            financialForm.reset();

            // Reset button
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span class="button-text">Enviar Solicitação</span><span class="button-icon">→</span>';
        }, 1500);
    }

    // Format email body
    function formatEmailBody(data) {
        return `Solicitação de Demonstração - MarcWeb

Informações da Empresa:
- Nome da Empresa: ${data.companyName}
- Tamanho da Empresa: ${data.companySize}
- Número de Funcionários: ${data.employeeCount}

Informações de Contato:
- Nome do Responsável: ${data.contactName}
- E-mail: ${data.contactEmail}
- Telefone: ${data.contactPhone}

Detalhes Adicionais:
- Nível de Interesse: ${data.interestLevel}
- Como nos encontrou: ${data.howFound || 'Não especificado'}
- Mensagem: ${data.message || 'Nenhuma mensagem adicional'}

Esta é uma solicitação automática enviada através do formulário de contato do site MarcWeb.`;
    }

    // Show status message
    function showStatus(message, type) {
        formStatus.textContent = message;
        formStatus.className = `form-status ${type}`;

        // Auto-hide after 5 seconds
        setTimeout(() => {
            formStatus.className = 'form-status';
            formStatus.textContent = '';
        }, 5000);
    }

    // Add real-time input validation
    function addInputValidation() {
        const inputs = financialForm.querySelectorAll('input, select, textarea');

        inputs.forEach(input => {
            input.addEventListener('input', function() {
                if (this.hasAttribute('required') && this.value.trim()) {
                    this.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                }
            });

            input.addEventListener('blur', function() {
                if (this.hasAttribute('required') && !this.value.trim()) {
                    this.style.borderColor = '#ff6b6b';
                }
            });
        });
    }

    // Phone input formatting
    const phoneInput = document.getElementById('contactPhone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');

            // Format as Brazilian phone number
            if (value.length > 0) {
                if (value.length <= 2) {
                    value = `(${value}`;
                } else if (value.length <= 7) {
                    value = `(${value.substring(0, 2)}) ${value.substring(2)}`;
                } else {
                    value = `(${value.substring(0, 2)}) ${value.substring(2, 7)}-${value.substring(7, 11)}`;
                }
            }

            e.target.value = value;
        });
    }

    // Employee count validation
    const employeeCountInput = document.getElementById('employeeCount');
    if (employeeCountInput) {
        employeeCountInput.addEventListener('input', function() {
            if (this.value < 1) {
                this.value = 1;
            }
        });
    }

    // Form animations
    function animateForm() {
        const formGroups = financialForm.querySelectorAll('.form-group');
        formGroups.forEach((group, index) => {
            group.style.opacity = '0';
            group.style.transform = 'translateY(20px)';
            group.style.transition = 'all 0.5s ease-out';

            setTimeout(() => {
                group.style.opacity = '1';
                group.style.transform = 'translateY(0)';
            }, index * 100);
        });
    }

    // Initialize form animations
    animateForm();
});
