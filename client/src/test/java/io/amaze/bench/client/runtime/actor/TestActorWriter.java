package io.amaze.bench.client.runtime.actor;

import com.google.common.base.Throwables;
import com.typesafe.config.Config;
import io.amaze.bench.client.api.ReactorException;
import io.amaze.bench.client.api.actor.After;
import io.amaze.bench.client.api.actor.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static io.amaze.bench.shared.helper.FileHelper.writeToFile;

/**
 * Created on 3/15/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
@io.amaze.bench.client.api.actor.Actor
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
            } catch (ReactorException e) {
                Throwables.propagate(e);
            }
        }
    }

    @Override
    public void onMessage(final String from, final String message) throws ReactorException {
        super.onMessage(from, message);
    }

    @After
    @Override
    public void after() {
        super.after();
    }

    private void writeFile(final String fileName, final String content) throws ReactorException {
        try {
            writeToFile(new File(fileName), content);
            LOG.info("Wrote \"" + content + "\" in file " + fileName + " ");
        } catch (IOException e) {
            throw new ReactorException(String.format(MSG_CREATION_ERROR, fileName), e);
        }
    }
}
