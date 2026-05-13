package de.thi.mynd.common.service;

import de.thi.mynd.common.entity.BaseEntityWithId;
import java.io.File;
import java.net.URL;

public interface ObjectStorageService {

  /**
   * Uploads a file to object storage, deriving the storage key from the given entity. The original
   * filename is inferred from the {@link File} itself.
   *
   * @param entity the entity this file belongs to; used to namespace the storage key
   * @param file the file to upload
   * @return the storage object key under which the file was stored
   */
  String uploadObject(BaseEntityWithId entity, File file);

  /**
   * Uploads a file to object storage, deriving the storage key from the given entity and preserving
   * the provided original filename as part of the key or metadata.
   *
   * @param entity the entity this file belongs to; used to namespace the storage key
   * @param file the file to upload
   * @param originalFileName the original filename to associate with the stored object
   * @return the storage object key under which the file was stored
   */
  String uploadObject(BaseEntityWithId entity, File file, String originalFileName);

  /**
   * Generates a pre-signed URL granting temporary read access to the object at the given key.
   *
   * @param objectKey the storage key of the object to generate a URL for
   * @return a time-limited pre-signed {@link URL} for direct object access
   */
  URL getPresignedUrlForFile(String objectKey);

  /**
   * Attempts to delete the object at the given storage key. Failures are silently swallowed; use
   * this when deletion is best-effort and should not interrupt application flow.
   *
   * @param objectKey the storage key of the object to delete
   */
  void tryDeleteObject(String objectKey);
}
