/*
 * Copyright 2016 Florent Weber <florent.weber@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.amaze.bench.shared.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created on 8/28/16.
 */
public final class Json {

    private static final Logger log = LogManager.getLogger();
    private static final Gson gson = new Gson();

    private Json() {
        // Helper class for tests only
    }

    public static boolean isValid(String jsonToCheck) {
        try {
            gson.fromJson(jsonToCheck, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            log.error("Invalid syntax for {}", jsonToCheck, e);
            return false;
        }
    }

}
