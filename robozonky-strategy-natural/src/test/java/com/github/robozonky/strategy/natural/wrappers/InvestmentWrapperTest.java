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

package com.github.robozonky.strategy.natural.wrappers;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.LoanHealthStats;
import com.github.robozonky.api.remote.entities.SellFee;
import com.github.robozonky.api.remote.entities.SellInfo;
import com.github.robozonky.api.remote.entities.SellPriceInfo;
import com.github.robozonky.api.remote.enums.LoanHealth;
import com.github.robozonky.api.remote.enums.MainIncomeType;
import com.github.robozonky.api.remote.enums.Purpose;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.remote.enums.Region;
import com.github.robozonky.api.strategies.InvestmentDescriptor;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;
import com.github.robozonky.internal.remote.entities.LoanHealthStatsImpl;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.internal.remote.entities.SellFeeImpl;
import com.github.robozonky.internal.remote.entities.SellInfoImpl;
import com.github.robozonky.internal.remote.entities.SellPriceInfoImpl;
import com.github.robozonky.test.AbstractRoboZonkyTest;
import com.github.robozonky.test.mock.MockInvestmentBuilder;
import com.github.robozonky.test.mock.MockLoanBuilder;

class InvestmentWrapperTest extends AbstractRoboZonkyTest {

    private static final Loan LOAN = new MockLoanBuilder()
        .set(LoanImpl::setInsuranceActive, true)
        .set(LoanImpl::setAmount, Money.from(100_000))
        .set(LoanImpl::setRating, Rating.D)
        .set(LoanImpl::setInterestRate, Ratio.ONE)
        .set(LoanImpl::setMainIncomeType, MainIncomeType.EMPLOYMENT)
        .set(LoanImpl::setPurpose, Purpose.AUTO_MOTO)
        .set(LoanImpl::setRegion, Region.JIHOCESKY)
        .set(LoanImpl::setStory, UUID.randomUUID()
            .toString())
        .set(LoanImpl::setTermInMonths, 20)
        .build();
    private static final InvestmentImpl INVESTMENT = MockInvestmentBuilder.fresh(LOAN, 2_000)
        .set(InvestmentImpl::setLoanHealthInfo, LoanHealth.HEALTHY)
        .set(InvestmentImpl::setPurchasePrice, Money.from(BigDecimal.TEN))
        .set(InvestmentImpl::setSmpPrice, Money.from(BigDecimal.ONE))
        .build();
    private static final PortfolioOverview FOLIO = mockPortfolioOverview();

    @Test
    void fromInvestment() {
        final Loan loan = new MockLoanBuilder()
            .set(LoanImpl::setInsuranceActive, false)
            .set(LoanImpl::setAmount, Money.from(100_000))
            .set(LoanImpl::setRating, Rating.D)
            .set(LoanImpl::setInterestRate, Ratio.ONE)
            .set(LoanImpl::setMainIncomeType, MainIncomeType.EMPLOYMENT)
            .set(LoanImpl::setPurpose, Purpose.AUTO_MOTO)
            .set(LoanImpl::setRegion, Region.JIHOCESKY)
            .set(LoanImpl::setStory, UUID.randomUUID()
                .toString())
            .set(LoanImpl::setTermInMonths, 20)
            .set(LoanImpl::setAnnuity, Money.from(BigDecimal.ONE))
            .build();
        final int invested = 200;
        final Investment investment = MockInvestmentBuilder.fresh(loan, invested)
            .set(InvestmentImpl::setRemainingMonths, 10)
            .set(InvestmentImpl::setLoanHealthInfo, LoanHealth.HEALTHY)
            .set(InvestmentImpl::setPurchasePrice, Money.from(BigDecimal.TEN))
            .set(InvestmentImpl::setSmpPrice, Money.from(BigDecimal.ONE))
            .set(InvestmentImpl::setSmpFee, Money.from(BigDecimal.ONE))
            .build();
        final InvestmentDescriptor id = new InvestmentDescriptor(investment, () -> loan);
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(id, FOLIO);
        assertSoftly(softly -> {
            softly.assertThat(w.getId())
                .isEqualTo(investment.getId());
            softly.assertThat(w.isInsuranceActive())
                .isEqualTo(investment.isInsuranceActive());
            softly.assertThat(w.getInterestRate())
                .isEqualTo(Ratio.ONE);
            softly.assertThat(w.getRegion())
                .isEqualTo(loan.getRegion());
            softly.assertThat(w.getRating())
                .isEqualTo(investment.getRating());
            softly.assertThat(w.getMainIncomeType())
                .isEqualTo(loan.getMainIncomeType());
            softly.assertThat(w.getPurpose())
                .isEqualTo(loan.getPurpose());
            softly.assertThat(w.getOriginalAmount())
                .isEqualTo(loan.getAmount()
                    .getValue()
                    .intValue());
            softly.assertThat(w.getRemainingPrincipal())
                .isEqualTo(investment.getRemainingPrincipal()
                    .get()
                    .getValue());
            softly.assertThat(w.getOriginal())
                .isSameAs(id);
            softly.assertThat(w.getStory())
                .isEqualTo(loan.getStory());
            softly.assertThat(w.getOriginalTermInMonths())
                .isEqualTo(investment.getLoanTermInMonth());
            softly.assertThat(w.getRemainingTermInMonths())
                .isEqualTo(investment.getRemainingMonths());
            softly.assertThat(w.getHealth())
                .contains(LoanHealth.HEALTHY);
            softly.assertThat(w.getOriginalPurchasePrice())
                .contains(new BigDecimal("10.00"));
            softly.assertThat(w.getDiscount())
                .contains(BigDecimal.ZERO);
            softly.assertThat(w.getPrice())
                .contains(new BigDecimal("1.00"));
            softly.assertThat(w.getSellFee())
                .contains(new BigDecimal("1.00"));
            softly.assertThat(w.getReturns())
                .contains(BigDecimal.ZERO);
            softly.assertThat(w.getRevenueRate())
                .isEqualTo(Ratio.fromRaw("0.1499"));
            softly.assertThat(w.getOriginalAnnuity())
                .isEqualTo(loan.getAnnuity()
                    .getValue()
                    .intValue());
            softly.assertThat(w.getCurrentDpd())
                .hasValue(0);
            softly.assertThat(w.getLongestDpd())
                .hasValue(0);
            softly.assertThat(w.getDaysSinceDpd())
                .hasValue(0);
            softly.assertThat(w.toString())
                .isNotNull();
        });
    }

    @Test
    void fromInvestmentWithoutRevenueRate() {
        final Loan loan = new MockLoanBuilder()
            .set(LoanImpl::setRating, Rating.A)
            .build();
        final int invested = 200;
        final Investment investment = MockInvestmentBuilder.fresh(loan, invested)
            .set(InvestmentImpl::setSmpFee, Money.from(BigDecimal.ONE))
            .build();
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(new InvestmentDescriptor(investment, () -> loan), FOLIO);
        when(FOLIO.getInvested()).thenReturn(Money.ZERO);
        assertThat(w.getRevenueRate()).isEqualTo(Ratio.fromPercentage("7.99"));
    }

    @Test
    void values() {
        final InvestmentDescriptor original = new InvestmentDescriptor(INVESTMENT, () -> LOAN);
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(original, FOLIO);
        assertSoftly(softly -> {
            softly.assertThat(w.getId())
                .isEqualTo(INVESTMENT.getId());
            softly.assertThat(w.isInsuranceActive())
                .isEqualTo(INVESTMENT.isInsuranceActive());
            softly.assertThat(w.getInterestRate())
                .isEqualTo(Ratio.ONE);
            softly.assertThat(w.getRegion())
                .isEqualTo(LOAN.getRegion());
            softly.assertThat(w.getRating())
                .isEqualTo(INVESTMENT.getRating());
            softly.assertThat(w.getMainIncomeType())
                .isEqualTo(LOAN.getMainIncomeType());
            softly.assertThat(w.getPurpose())
                .isEqualTo(LOAN.getPurpose());
            softly.assertThat(w.getOriginalAmount())
                .isEqualTo(LOAN.getAmount()
                    .getValue()
                    .intValue());
            softly.assertThat(w.getRemainingPrincipal())
                .isEqualTo(INVESTMENT.getRemainingPrincipal()
                    .get()
                    .getValue());
            softly.assertThat(w.getOriginal())
                .isSameAs(original);
            softly.assertThat(w.getStory())
                .isEqualTo(LOAN.getStory());
            softly.assertThat(w.getOriginalTermInMonths())
                .isEqualTo(INVESTMENT.getLoanTermInMonth());
            softly.assertThat(w.getRemainingTermInMonths())
                .isEqualTo(INVESTMENT.getRemainingMonths());
            softly.assertThat(w.getHealth())
                .contains(LoanHealth.HEALTHY);
            softly.assertThat(w.getOriginalPurchasePrice())
                .contains(new BigDecimal("10.00"));
            softly.assertThat(w.getDiscount())
                .contains(BigDecimal.ZERO);
            softly.assertThat(w.getPrice())
                .contains(new BigDecimal("1.00"));
            softly.assertThat(w.getSellFee())
                .contains(BigDecimal.ZERO);
            softly.assertThat(w.getReturns())
                .contains(BigDecimal.ZERO);
            softly.assertThat(w.toString())
                .isNotNull();
        });
    }

    @Test
    void failsOnNoSellInfo() {
        final Investment investment = MockInvestmentBuilder.fresh(LOAN, 2_000)
            .set(InvestmentImpl::setSmpPrice, null)
            .set(InvestmentImpl::setSmpFee, null)
            .build();
        final InvestmentDescriptor original = new InvestmentDescriptor(investment, () -> LOAN);
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(original, FOLIO);
        assertSoftly(softly -> {
            softly.assertThatThrownBy(w::getHealth)
                .isInstanceOf(NoSuchElementException.class);
            softly.assertThatThrownBy(w::getPrice)
                .isInstanceOf(NoSuchElementException.class);
        });
    }

    @Test
    void sellInfoValues() {
        final Investment investment = MockInvestmentBuilder.fresh(LOAN, 200)
            .set(InvestmentImpl::setSmpPrice, null)
            .set(InvestmentImpl::setSmpFee, null)
            .build();
        final LoanHealthStats healthInfo = mock(LoanHealthStatsImpl.class);
        when(healthInfo.getLoanHealthInfo()).thenReturn(LoanHealth.HISTORICALLY_IN_DUE);
        final SellPriceInfo priceInfo = mock(SellPriceInfoImpl.class);
        when(priceInfo.getDiscount()).thenReturn(Ratio.fromPercentage(10));
        when(priceInfo.getSellPrice()).thenReturn(Money.from(10));
        final SellFee feeInfo = mock(SellFeeImpl.class);
        when(feeInfo.getValue()).thenReturn(Money.from(2));
        when(priceInfo.getFee()).thenReturn(feeInfo);
        final SellInfo sellInfo = mock(SellInfoImpl.class);
        when(sellInfo.getLoanHealthStats()).thenReturn(healthInfo);
        when(sellInfo.getPriceInfo()).thenReturn(priceInfo);
        final InvestmentDescriptor original = new InvestmentDescriptor(investment, () -> LOAN, () -> sellInfo);
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(original, FOLIO);
        assertSoftly(softly -> {
            softly.assertThat(w.getHealth())
                .contains(LoanHealth.HISTORICALLY_IN_DUE);
            softly.assertThat(w.getDiscount())
                .contains(new BigDecimal("20.00"));
            softly.assertThat(w.getPrice())
                .contains(new BigDecimal("10.00"));
            softly.assertThat(w.getSellFee())
                .contains(new BigDecimal("2.00"));
        });
    }

    @Test
    void equality() {
        final InvestmentDescriptor original = new InvestmentDescriptor(INVESTMENT, () -> LOAN);
        final Wrapper<InvestmentDescriptor> w = Wrapper.wrap(original, FOLIO);
        assertSoftly(softly -> {
            softly.assertThat(w)
                .isEqualTo(w);
            softly.assertThat(w)
                .isEqualTo(Wrapper.wrap(original, FOLIO));
            softly.assertThat(w)
                .isEqualTo(Wrapper.wrap(new InvestmentDescriptor(INVESTMENT, () -> LOAN), FOLIO));
            softly.assertThat(w)
                .isNotEqualTo(null);
        });
    }
}
