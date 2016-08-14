package io.amaze.bench.shared.metric;

import javax.validation.constraints.NotNull;

/**
 * Instantiates a populated {@link SystemConfig} object.<br/>
 * It contains details on the underlying OS / processor architecture / memory etc.
 * <p>
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface SystemConfigFactory {

    /**
     * Generate a SystemConfig object (best effort).<br/>
     *
     * @return A new SystemConfig object.
     */
    @NotNull
    SystemConfig create();

}
