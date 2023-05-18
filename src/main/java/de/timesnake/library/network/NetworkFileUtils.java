/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.library.network;

import de.timesnake.database.util.object.Type;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NetworkFileUtils {

  public static void createSymLinks(Path src, Path dest) throws IOException {
    String[] fileNames = src.toFile().list();
    createSymLinks(src, fileNames != null ? List.of(fileNames) : List.of(), dest);
  }

  public static void createSymLinks(Path src, List<String> files, Path dest) throws IOException {
    for (String fileName : files) {
      Files.createSymbolicLink(dest.resolve(fileName), src.resolve(fileName));
    }
  }

  public static Path resolveTemplatePath(Path base, Type.Server<?> type, String task,
      String defaultDir) {
    Path path = base.toAbsolutePath();
    if (path.resolve(type.getShortName()).toFile().exists()) {
      path = path.resolve(type.getShortName());
      if (task != null && path.resolve(task).toFile().exists()) {
        path = path.resolve(task);
        if (path.resolve(defaultDir).toFile().exists()) {
          path = path.resolve(defaultDir);
        }
      } else if (path.resolve(defaultDir).toFile().exists()) {
        path = path.resolve(defaultDir);
      } else {
        path = base.resolve(defaultDir);
      }
    } else {
      path = path.resolve(defaultDir);
    }
    return path;
  }

  public static Path resolveWorldTemplatePath(Path base, Type.Server<?> type, String task) {
    Path path = base.toAbsolutePath();
    if (path.resolve(type.getShortName()).toFile().exists()) {
      path = path.resolve(type.getShortName());
      if (task != null && path.resolve(task).toFile().exists()) {
        path = path.resolve(task);
      }
    }
    return path;
  }
}
