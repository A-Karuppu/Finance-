package com.finance.dashboard.enums;

public enum Role {
    VIEWER,    // Read-only access to dashboard
    ANALYST,   // Read records + access summaries/insights
    ADMIN      // Full access: manage records and users
}
