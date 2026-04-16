package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntity;

import java.net.URL;

public interface ObjectStorageService {

    URL getPresignedCreationUrlForEntityFile(BaseEntity entity, String filename, String contentType);

    URL getPresignedUrlForEntityFile(BaseEntity entity, String filename);
}
