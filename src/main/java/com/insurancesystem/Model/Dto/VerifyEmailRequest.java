package com.insurancesystem.Model.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {

    private String email;
    private String code;
}
