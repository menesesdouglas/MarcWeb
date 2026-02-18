package com.marcweb.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTenantRequest {
    
    @NotBlank(message = "O tenantId é obrigatório")
    @Size(min = 3, max = 50, message = "O tenantId deve ter entre 3 e 50 caracteres")
    private String tenantId;
    
    @NotBlank(message = "O nome da empresa é obrigatório")
    @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
    private String nome;
    
    @NotBlank(message = "O CNPJ é obrigatório")
    private String cnpj;
    
    @Size(max = 100, message = "O domínio deve ter no máximo 100 caracteres")
    private String dominio;
}
