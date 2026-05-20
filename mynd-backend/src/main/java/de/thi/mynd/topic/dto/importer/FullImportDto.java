package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@RegisterForReflection
@Getter
@Setter
public final class FullImportDto {

    private List<ImportTopicDto> topics;
    private Map<String, List<String>> associations;
}
