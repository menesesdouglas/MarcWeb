package com.marcweb.dto.user;

import com.marcweb.entity.Role;
import lombok.Data;

/**
 * DTO for updating user profile.
 * All fields are optional since users should only update what they need.
 */
@Data
public class UpdateProfileRequest {

    private String nome;

    private String cargo;

    private String currentPassword;

    private String newPassword;

    private Role role;

}
