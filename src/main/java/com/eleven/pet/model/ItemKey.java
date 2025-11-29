package com.eleven.pet.model;

public record ItemKey(
        Class<? extends Item> type,
        String name
) {}
