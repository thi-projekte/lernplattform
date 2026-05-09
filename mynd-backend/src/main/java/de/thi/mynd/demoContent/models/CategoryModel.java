package de.thi.mynd.demoContent.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;

@RegisterForReflection
@Getter
public final class CategoryModel {
    public String identifier;
    public String creatorId;
    public String title;
    public String color;
}