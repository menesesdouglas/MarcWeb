package com.marcweb.dto.aej;

import lombok.Data;
import java.util.List;

@Data
public class AEJDTO {
    private AEJHeaderDTO header; // Tipo 01
    private List<AEJRepDTO> reps; // Tipo 02
    private List<AEJVinculoDTO> vinculos; // Tipo 03
    private List<AEJHorarioContratualDTO> horariosContratuais; // Tipo 04
    private List<AEJMarcacaoDTO> marcacoes; // Tipo 05
    private List<AEJMatriculaESocialDTO> matriculasESocial; // Tipo 06
    private List<AEJAusenciaBancoHorasDTO> ausenciasBancoHoras; // Tipo 07
    private AEJPtrpDTO ptrp; // Tipo 08
    private AEJTrailerDTO trailer; // Tipo 99
    private AEJAssinaturaDigitalDTO assinaturaDigital; // Assinatura digital
}
