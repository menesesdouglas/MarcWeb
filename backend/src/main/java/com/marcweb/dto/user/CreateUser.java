package com.marcweb.dto.user;

import com.marcweb.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class CreateUser {

    @NotBlank(message = "Campo obrigatório")
    private String username;

    @NotBlank(message = "Campo obrigatório")
    private String nome;

    @NotBlank(message = "Campo obrigatório")
    private String password;

    @NotNull(message = "Campo obrigatório")
    private Role role;

    private Boolean ativo;

    private Boolean consentGeoloc;

    private String cargo;

    @CPF(message = "Informe o CPF do funcionário")
    private String cpf;




}
