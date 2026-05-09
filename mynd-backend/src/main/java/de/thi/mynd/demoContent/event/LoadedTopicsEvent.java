package de.thi.mynd.demoContent.event;

import de.thi.mynd.topic.entity.Topic;

import java.util.Map;

public record LoadedTopicsEvent(Map<String, Topic> mapping) {
}
