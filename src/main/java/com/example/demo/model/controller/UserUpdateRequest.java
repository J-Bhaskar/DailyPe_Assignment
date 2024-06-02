package com.example.demo.controller;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    private List<UUID> userIds;
    private Map<String, String> updateData;
}
