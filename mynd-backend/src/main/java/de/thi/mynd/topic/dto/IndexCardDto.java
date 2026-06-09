package de.thi.mynd.topic.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public final class IndexCardDto {
    public UUID id;
    public String question;
    public String answer;
}
