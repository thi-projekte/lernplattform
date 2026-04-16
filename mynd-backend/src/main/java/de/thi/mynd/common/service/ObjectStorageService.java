package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntity;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface ObjectStorageService {

  String uploadObject(BaseEntity entity, File file) throws IOException;

  URL getPresignedUrlForFile(String objectKey);

  void tryDeleteObject(String objectKey);
}
