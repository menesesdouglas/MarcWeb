package com.marcweb.dto.user;

import java.util.List;

public record PageUserDTO(List<UserDTO> usuarios, int totalPages) {

}
