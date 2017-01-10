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

package io.amaze.bench.cluster;

import io.amaze.bench.shared.test.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created on 1/11/17.
 */
@RunWith(MockitoJUnitRunner.class)
public final class DummyEndpointTest {

    @Test
    public void toString_yields_valid_json() {
        DummyEndpoint endpoint = new DummyEndpoint();

        Json.isValid(endpoint.toString());
    }

}