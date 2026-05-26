package de.thi.mynd.topic.dto;

import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
public class CategoryDto {
    public UUID id;
    public String title;
    public String color;
}
