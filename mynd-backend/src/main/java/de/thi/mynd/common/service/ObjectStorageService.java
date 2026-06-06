package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntityWithId;
import java.io.File;
import java.net.URL;

public interface ObjectStorageService {

  String uploadObject(BaseEntityWithId entity, File file);

  String uploadObject(BaseEntityWithId entity, File file, String originalFileName);

  String uploadObject(String objectKey, File file);

  URL getPresignedUrlForFile(String objectKey);

  void tryDeleteObject(String objectKey);
}
