package de.thi.mynd.demoContent.models;

import de.thi.mynd.topic.entity.ContentElement;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@RegisterForReflection
@Getter
public final class TopicModel {

    public String identifier;
    public String title;
    public String creatorId;
    public String teaser;
    public List<String> categories;
    public int duration;
    public List<Map<String, Object>> contentElements;
}
