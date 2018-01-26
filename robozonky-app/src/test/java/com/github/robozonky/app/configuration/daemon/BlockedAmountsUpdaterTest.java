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

package com.github.robozonky.app.configuration.daemon;

import java.util.Optional;

import com.github.robozonky.app.authentication.Authenticated;
import com.github.robozonky.app.portfolio.BlockedAmounts;
import com.github.robozonky.app.portfolio.Portfolio;
import com.github.robozonky.app.portfolio.PortfolioDependant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockedAmountsUpdaterTest {

    @Test
    void hasDependant() {
        final BlockedAmountsUpdater bau = new BlockedAmountsUpdater(mock(Authenticated.class), Optional::empty);
        assertThat(bau.getDependant()).isInstanceOf(BlockedAmounts.class);
    }

    @Test
    void noDependant() {
        final BlockedAmountsUpdater bau = new BlockedAmountsUpdater(mock(Authenticated.class), Optional::empty,
                                                                    null);
        bau.run(); // quietly ignored
    }

    @Test
    void customDependant() {
        final Portfolio p = mock(Portfolio.class);
        final PortfolioDependant d = mock(PortfolioDependant.class);
        final Authenticated auth = mock(Authenticated.class);
        final BlockedAmountsUpdater bau = new BlockedAmountsUpdater(auth, () -> Optional.of(p), d);
        bau.run();
        verify(d).accept(eq(p), eq(auth));
    }
}

