package com.group4.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private long id;
    private int userId;
    private String action;
    private String targetEntity;
    private Integer targetId;
    private String description;
    private Instant createdAt;
}
