/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import java.nio.file.Path;

public interface WorldSyncResult {

  boolean isSuccessful();

  class Successful implements WorldSyncResult {

    private final Path worldPath;

    public Successful(Path worldPath) {
      this.worldPath = worldPath;
    }

    public Path getWorldPath() {
      return worldPath;
    }

    @Override
    public boolean isSuccessful() {
      return true;
    }
  }

  class Fail implements WorldSyncResult {

    private final String reason;

    public Fail(String reason) {
      this.reason = reason;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public boolean isSuccessful() {
      return false;
    }
  }
}
