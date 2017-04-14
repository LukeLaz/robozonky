/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.strategy.rules;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.github.triceo.robozonky.api.remote.entities.Loan;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class ProcessedLoanTest {

    @Test
    public void compareByPriority() {
        final Loan l = Mockito.mock(Loan.class);
        final ProcessedLoan p1 = new ProcessedLoan(l, 1);
        final ProcessedLoan p2 = new ProcessedLoan(l, 2);
        Assertions.assertThat(p1).isGreaterThan(p2);
        Assertions.assertThat(p2).isLessThan(p1);
    }

    @Test
    public void compareByDatePublishedAfterPriority() {
        final int priority = 1;
        final Loan later = Mockito.mock(Loan.class);
        Mockito.when(later.getDatePublished()).thenReturn(OffsetDateTime.now());
        final Loan earlier = Mockito.mock(Loan.class);
        Mockito.when(earlier.getDatePublished()).thenReturn(OffsetDateTime.now().minus(Duration.ofSeconds(1)));
        final ProcessedLoan p1 = new ProcessedLoan(later, priority);
        final ProcessedLoan p2 = new ProcessedLoan(earlier, priority);
        Assertions.assertThat(p1).isGreaterThan(p2);
        Assertions.assertThat(p2).isLessThan(p1);
    }

}