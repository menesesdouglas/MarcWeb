package com.marcweb.dto.user;

import com.marcweb.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class CreateUser {

    @NotBlank(message = "Crie um nome de usuário (Campo único)")
    private String username;

    @NotBlank(message = "Informe o nome do funcionário")
    private String nome;

    @NotBlank(message = "Crie uma senha (Campo obrigatório)")
    private String password;

    @NotNull(message = "Informe o perfil do usuário")
    private Role role;

    private Boolean ativo;

    private Boolean consentGeoloc;

    private String cargo;

    @CPF(message = "Informe o CPF do funcionário")
    private String cpf;


}
