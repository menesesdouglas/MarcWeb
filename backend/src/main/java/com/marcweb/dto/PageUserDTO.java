package com.marcweb.dto;

import java.util.List;

public record PageUserDTO(List<UserDTO> usuarios, int totalPages) {

}
