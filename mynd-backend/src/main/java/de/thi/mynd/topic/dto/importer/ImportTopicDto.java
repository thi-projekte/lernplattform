package de.thi.mynd.topic.dto.importer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public final class ImportTopicDto {

  public String identifier;
  public String title;
  public String teaser;
  public List<String> categories;
  public int duration;
  public List<Map<String, Object>> contentElements;
}
