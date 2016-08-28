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
package io.amaze.bench.client.runtime.actor;

import com.google.common.base.Throwables;
import com.typesafe.config.Config;
import io.amaze.bench.client.api.After;
import io.amaze.bench.client.api.IrrecoverableException;
import io.amaze.bench.client.api.Sender;
import io.amaze.bench.client.api.TerminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

import static io.amaze.bench.shared.helper.Files.writeTo;

/**
 * Created on 3/15/16.
 */
@io.amaze.bench.client.api.Actor
public final class TestActorWriter extends TestActor {

    static final String INIT_FILE_CONFIG = "init_file";
    static final String OK = "OK";

    private static final Logger LOG = LoggerFactory.getLogger(TestActorWriter.class);
    private static final String MSG_CREATION_ERROR = "Could not create file %s";

    public TestActorWriter(final Sender sender, final Config config) {
        super(sender, config);

        if (config.hasPath(INIT_FILE_CONFIG)) {
            String initFileName = getConfig().getString(INIT_FILE_CONFIG);
            try {
                writeFile(initFileName, OK);
            } catch (IrrecoverableException e) {
                Throwables.propagate(e);
            }
        }
    }

    @Override
    public void onMessage(@NotNull final String from, @NotNull final String message)
            throws IrrecoverableException, TerminationException {

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
            LOG.info("Wrote \"" + content + "\" in file " + fileName + " ");
        } catch (IOException e) {
            throw new IrrecoverableException(String.format(MSG_CREATION_ERROR, fileName), e);
        }
    }
}
