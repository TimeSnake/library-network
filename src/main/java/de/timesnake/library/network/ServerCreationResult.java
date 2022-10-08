/*
 * library-network.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.library.network;

import java.nio.file.Path;

public interface ServerCreationResult {

    boolean isSuccessful();

    class Successful implements ServerCreationResult {

        private final Path serverPath;

        public Successful(Path serverPath) {
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
