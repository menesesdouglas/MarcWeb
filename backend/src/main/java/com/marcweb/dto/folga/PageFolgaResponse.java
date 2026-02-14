package com.marcweb.dto.folga;

import lombok.Data;

import java.util.List;

@Data
public class PageFolgaResponse {
    private List<FolgaResponse> folgas;
    private int totalPages;
}
