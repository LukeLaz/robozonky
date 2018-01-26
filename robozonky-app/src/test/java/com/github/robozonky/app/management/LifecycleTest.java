/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.app.management;

import java.util.Optional;

import com.github.robozonky.app.runtime.Lifecycle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LifecycleTest {

    @Test
    void version() {
        final Lifecycle l = mock(Lifecycle.class);
        when(l.getZonkyApiVersion()).thenReturn(Optional.empty());
        final Runtime r = new Runtime(l);
        assertThat(r.getZonkyApiVersion()).isEqualTo("N/A");
    }

    @Test
    void shutdown() {
        final Lifecycle l = mock(Lifecycle.class);
        final Runtime r = new Runtime(l);
        r.stopDaemon();
        verify(l).resumeToShutdown();
    }
}
