package com.finance.dashboard.dto;

import com.finance.dashboard.enums.Role;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRequest {
    private String email;
    private Role role;
    private Boolean active;
}
