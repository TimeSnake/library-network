/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import java.nio.file.Path;

public interface ServerCreationResult {

  boolean isSuccessful();

  class Success implements ServerCreationResult {

    private final Path serverPath;

    public Success(Path serverPath) {
      this.serverPath = serverPath;
    }

    public Path getServerPath() {
      return serverPath;
    }

    @Override
    public boolean isSuccessful() {
      return true;
    }
  }

  class Fail implements ServerCreationResult {

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
