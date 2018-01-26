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

package com.github.robozonky.app.investing;

import java.util.Arrays;
import java.util.Collection;

import com.github.robozonky.api.strategies.LoanDescriptor;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SessionStateTest extends AbstractZonkyLeveragingTest {

    @Test
    void discardPersistence() {
        final LoanDescriptor ld = AbstractZonkyLeveragingTest.mockLoanDescriptor();
        final Collection<LoanDescriptor> lds = Arrays.asList(ld, AbstractZonkyLeveragingTest.mockLoanDescriptor());
        // ignore the loan and persist
        final SessionState it = new SessionState(lds);
        it.discard(ld);
        assertThat(it.getSeenLoans()).isEmpty();
        assertThat(it.getDiscardedLoans()).isNotEmpty().contains(ld);
        // load again and check that persisted
        final SessionState it2 = new SessionState(lds);
        assertThat(it.getSeenLoans()).isEmpty();
        assertThat(it2.getDiscardedLoans()).isNotEmpty().contains(ld);
    }

    @Test
    void skipPersistence() {
        final LoanDescriptor ld = AbstractZonkyLeveragingTest.mockLoanDescriptor();
        final Collection<LoanDescriptor> lds = Arrays.asList(ld, AbstractZonkyLeveragingTest.mockLoanDescriptor());
        // skip the loan and persist
        final SessionState it = new SessionState(lds);
        it.skip(ld);
        assertThat(it.getDiscardedLoans()).isEmpty();
        assertThat(it.getSeenLoans()).isNotEmpty().contains(ld);
        // load again and check that persisted
        final SessionState it2 = new SessionState(lds);
        assertThat(it.getDiscardedLoans()).isEmpty();
        assertThat(it2.getSeenLoans()).isNotEmpty().contains(ld);
    }
}
