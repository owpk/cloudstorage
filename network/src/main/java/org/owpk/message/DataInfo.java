package org.owpk.message;

import lombok.Data;

/**
 * Metadata of package
 */
@Data
public class DataInfo extends Message<byte[]> {
  private final int chunkCount;
  private final int chunkIndex;
  private final String file;

  public DataInfo(MessageType type, int chunkCount, int packageIndex, String file, byte[] payload) {
    super(type, payload);
    this.chunkCount = chunkCount;
    this.chunkIndex = packageIndex;
    this.file = file;
  }
}
