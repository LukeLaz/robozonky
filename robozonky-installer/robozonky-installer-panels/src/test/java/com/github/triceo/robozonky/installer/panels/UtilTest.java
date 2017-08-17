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

package com.github.triceo.robozonky.installer.panels;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UtilTest {

    @Test
    public void testCopyOptions() {
        final String key = UUID.randomUUID().toString();
        final String[] values = new String[]{"a", "b", "c"};
        final CommandLinePart source = new CommandLinePart();
        source.setOption(key, values);
        final CommandLinePart target = new CommandLinePart();
        Util.copyOptions(source, target);
        Assertions.assertThat(target.getOptions().get(key)).containsExactly(values);
    }
}
