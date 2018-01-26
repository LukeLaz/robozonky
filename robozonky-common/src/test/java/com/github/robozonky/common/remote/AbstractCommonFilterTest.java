/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.common.remote;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;

import com.github.robozonky.internal.api.Defaults;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

abstract class AbstractCommonFilterTest {

    protected abstract RoboZonkyFilter getTestedFilter();

    @Test
    void wasUserAgentHeaderAdded() throws IOException {
        final ClientRequestContext crc = mock(ClientRequestContext.class);
        when(crc.getHeaders()).thenReturn(new MultivaluedHashMap<>());

        this.getTestedFilter().filter(crc);
        assertThat(crc.getHeaders().getFirst("User-Agent")).isEqualTo(Defaults.ROBOZONKY_USER_AGENT);
    }
}
