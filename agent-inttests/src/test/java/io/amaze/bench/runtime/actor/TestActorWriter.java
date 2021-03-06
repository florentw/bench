/*
 * Copyright 2016-2017 Florent Weber <florent.weber@gmail.com>
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
package io.amaze.bench.runtime.actor;

import com.google.common.base.Throwables;
import com.typesafe.config.Config;
import io.amaze.bench.api.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.amaze.bench.shared.util.Files.writeTo;

/**
 * Created on 3/15/16.
 */
@Actor
public final class TestActorWriter extends TestActor {

    static final String INIT_FILE_CONFIG = "init_file";
    static final String SUICIDE_AFTER_MS = "suicide_after_ms";
    static final String OK = "OK";

    private static final Logger log = LogManager.getLogger();
    private static final String MSG_CREATION_ERROR = "Could not create file %s";

    public TestActorWriter(final ActorKey actorKey, final Sender sender, final Config config) {
        super(actorKey, sender, config);

        if (config.hasPath(INIT_FILE_CONFIG)) {
            String initFileName = getConfig().getString(INIT_FILE_CONFIG);
            try {
                log.info("{} writing RDV file {} ms", this, initFileName);
                writeFile(initFileName, OK);
            } catch (IrrecoverableException e) {
                log.info("{} Error while init", this, e);
                Throwables.propagate(e);
            }
        }

        if (config.hasPath(SUICIDE_AFTER_MS)) {
            long sleepTime = getConfig().getLong(SUICIDE_AFTER_MS);
            sleepUninterruptibly(sleepTime, TimeUnit.MILLISECONDS);
            log.info("{} committing suicide after {} ms", this, sleepTime);
            System.exit(0);
        }
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message) throws ReactorException {
        super.onMessage(from, message);
    }

    @After
    @Override
    public void after() {
        super.after();
    }

    private void writeFile(final String fileName, final String content) throws IrrecoverableException {
        try {
            writeTo(new File(fileName), content);
            log.info("Wrote \"{}\" in file {} ", content, fileName);
        } catch (IOException e) {
            throw new IrrecoverableException(String.format(MSG_CREATION_ERROR, fileName), e);
        }
    }
}
