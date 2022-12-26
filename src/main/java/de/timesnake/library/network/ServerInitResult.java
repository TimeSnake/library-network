/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.library.network;

import java.nio.file.Path;

public interface ServerInitResult {

    boolean isSuccessful();

    class Successful implements ServerInitResult {

        private final Path templatePath;

        public Successful(Path templatePath) {
            this.templatePath = templatePath;
        }

        public Path getTemplatePath() {
            return templatePath;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }
    }

    class Fail implements ServerInitResult {

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
