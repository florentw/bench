package io.amaze.bench.shared.metric;

import javax.validation.constraints.NotNull;

/**
 * Instantiates a populated {@link SystemInfo} object.<br/>
 * It contains details on the underlying OS / processor architecture / memory etc.
 * <p>
 * Created on 3/20/16.
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public interface SystemInfoFactory {

    /**
     * Generate a SystemInfo object (best effort).<br/>
     *
     * @return A new SystemInfo object.
     */
    @NotNull
    SystemInfo create();

}
