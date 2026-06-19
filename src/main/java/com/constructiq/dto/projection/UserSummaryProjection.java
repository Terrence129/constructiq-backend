package com.constructiq.dto.projection;

import com.constructiq.enums.UserRole;

public interface UserSummaryProjection {

    Long getId();

    String getName();

    String getEmail();

    UserRole getRole();
}
