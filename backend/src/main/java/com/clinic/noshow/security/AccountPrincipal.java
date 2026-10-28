package com.clinic.noshow.security;

public record AccountPrincipal(Long accountId, Long actorId, String email, String name, String role) {}
