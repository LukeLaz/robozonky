/*
 * Copyright 2020 The RoboZonky Project
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

package com.github.robozonky.notifications.listeners;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.api.notifications.EventListener;
import com.github.robozonky.api.notifications.LoanDelinquent10DaysOrMoreEvent;
import com.github.robozonky.api.notifications.LoanDelinquentEvent;
import com.github.robozonky.api.notifications.LoanNoLongerDelinquentEvent;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.enums.MainIncomeType;
import com.github.robozonky.api.remote.enums.Purpose;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.remote.enums.Region;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.notifications.AbstractTargetHandler;
import com.github.robozonky.notifications.SupportedListener;
import com.github.robozonky.notifications.Target;
import com.github.robozonky.test.AbstractRoboZonkyTest;
import com.github.robozonky.test.mock.MockInvestmentBuilder;
import com.github.robozonky.test.mock.MockLoanBuilder;

class DelinquencyTrackerTest extends AbstractRoboZonkyTest {

    private static final Loan LOAN = new MockLoanBuilder()
        .set(LoanImpl::setAmount, Money.from(200))
        .set(LoanImpl::setAnnuity, Money.from(BigDecimal.TEN))
        .set(LoanImpl::setRating, Rating.D)
        .set(LoanImpl::setInterestRate, Ratio.fromPercentage(Rating.D.getCode()))
        .set(LoanImpl::setPurpose, Purpose.AUTO_MOTO)
        .set(LoanImpl::setRegion, Region.JIHOCESKY)
        .set(LoanImpl::setMainIncomeType, MainIncomeType.EMPLOYMENT)
        .set(LoanImpl::setName, "")
        .set(LoanImpl::setUrl, "http://localhost")
        .build();
    private static final Investment INVESTMENT = MockInvestmentBuilder.fresh(LOAN, 200)
        .set(InvestmentImpl::setExpectedInterest, Money.from(BigDecimal.TEN))
        .build();
    private static final Loan LOAN2 = new MockLoanBuilder()
        .set(LoanImpl::setAmount, Money.from(200))
        .set(LoanImpl::setAnnuity, Money.from(BigDecimal.TEN))
        .set(LoanImpl::setRating, Rating.A)
        .set(LoanImpl::setInterestRate, Ratio.fromPercentage(Rating.A.getCode()))
        .set(LoanImpl::setPurpose, Purpose.TRAVEL)
        .set(LoanImpl::setRegion, Region.JIHOMORAVSKY)
        .set(LoanImpl::setMainIncomeType, MainIncomeType.OTHERS_MAIN)
        .set(LoanImpl::setName, "")
        .set(LoanImpl::setUrl, "http://localhost")
        .build();
    private static final Investment INVESTMENT2 = MockInvestmentBuilder.fresh(LOAN2, 200)
        .build();
    private static SessionInfo SESSION = mockSessionInfo();

    @Test
    void standard() {
        final DelinquencyTracker t = new DelinquencyTracker(Target.EMAIL);
        assertThat(t.isDelinquent(SESSION, INVESTMENT)).isFalse();
        t.setDelinquent(SESSION, INVESTMENT);
        t.setDelinquent(SESSION, INVESTMENT2);
        assertThat(t.isDelinquent(SESSION, INVESTMENT)).isTrue();
        assertThat(t.isDelinquent(SESSION, INVESTMENT2)).isTrue();
        t.unsetDelinquent(SESSION, INVESTMENT);
        assertThat(t.isDelinquent(SESSION, INVESTMENT2)).isTrue();
        assertThat(t.isDelinquent(SESSION, INVESTMENT)).isFalse();
    }

    @Test
    void notifying() throws Exception {
        final AbstractTargetHandler h = AbstractListenerTest.getHandler();
        final EventListener<LoanDelinquentEvent> l = new LoanDelinquentEventListener(
                SupportedListener.LOAN_DELINQUENT_10_PLUS, h);
        final EventListener<LoanNoLongerDelinquentEvent> l2 = new LoanNoLongerDelinquentEventListener(
                SupportedListener.LOAN_NO_LONGER_DELINQUENT, h);
        final LoanNoLongerDelinquentEvent evt = new MyLoanNoLongerDelinquentEvent();
        l2.handle(evt, SESSION);
        verify(h, never()).send(any(), any(), any(), any()); // not delinquent before, not sending
        l.handle(new MyLoanDelinquent10DaysOrMoreEvent(), SESSION);
        verify(h).send(eq(SESSION), any(), any(), any());
        l2.handle(evt, SESSION);
        verify(h, times(2)).send(eq(SESSION), any(), any(), any()); // delinquency now registered, send
        l2.handle(evt, SESSION);
        verify(h, times(2)).send(eq(SESSION), any(), any(), any()); // already unregistered, send
    }

    private static class MyLoanDelinquent10DaysOrMoreEvent implements LoanDelinquent10DaysOrMoreEvent {

        @Override
        public LocalDate getDelinquentSince() {
            return LocalDate.now();
        }

        @Override
        public Investment getInvestment() {
            return INVESTMENT;
        }

        @Override
        public Loan getLoan() {
            return LOAN;
        }

        @Override
        public OffsetDateTime getCreatedOn() {
            return OffsetDateTime.now();
        }

        @Override
        public int getThresholdInDays() {
            return 10;
        }
    }

    private static class MyLoanNoLongerDelinquentEvent implements LoanNoLongerDelinquentEvent {

        @Override
        public Investment getInvestment() {
            return INVESTMENT;
        }

        @Override
        public Loan getLoan() {
            return LOAN;
        }

        @Override
        public OffsetDateTime getCreatedOn() {
            return OffsetDateTime.now();
        }
    }
}
