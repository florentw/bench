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
package io.amaze.bench.client.api;

/**
 * To be thrown by an Actor to notify the agent that it wishes to terminate gracefully.<br/>
 * To notify of irrecoverable exceptions, {@link IrrecoverableException} should be used instead.
 */
public final class TerminationException extends ReactorException {

    public TerminationException() {
        super();
    }

}
