package com.yu.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCommentRejectDTO {

    @NotBlank(message = "rejectReason must not be blank")
    @Size(max = 255, message = "rejectReason length must be <= 255")
    private String rejectReason;
}
