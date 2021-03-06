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

package com.github.robozonky.app.summaries;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.RiskPortfolio;
import com.github.robozonky.api.remote.entities.SellFee;
import com.github.robozonky.api.remote.entities.SellInfo;
import com.github.robozonky.api.remote.entities.SellPriceInfo;
import com.github.robozonky.api.remote.entities.Statistics;
import com.github.robozonky.api.remote.enums.LoanHealth;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import com.github.robozonky.internal.remote.Select;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;
import com.github.robozonky.internal.remote.entities.RiskPortfolioImpl;
import com.github.robozonky.internal.remote.entities.SellFeeImpl;
import com.github.robozonky.internal.remote.entities.SellInfoImpl;
import com.github.robozonky.internal.remote.entities.SellPriceInfoImpl;
import com.github.robozonky.internal.remote.entities.StatisticsImpl;
import com.github.robozonky.internal.tenant.Tenant;
import com.github.robozonky.internal.util.functional.Tuple2;
import com.github.robozonky.test.mock.MockInvestmentBuilder;

class UtilTest extends AbstractZonkyLeveragingTest {

    private static void mockSellInfo(Zonky zonky, final BigDecimal price, final BigDecimal fee) {
        SellFee sellFee = mock(SellFeeImpl.class);
        when(sellFee.getValue()).thenReturn(Money.from(fee));
        when(sellFee.getExpiresAt()).thenReturn(Optional.of(OffsetDateTime.now()));
        SellPriceInfo sellPriceInfo = mock(SellPriceInfoImpl.class);
        when(sellPriceInfo.getSellPrice()).thenReturn(Money.from(price));
        when(sellPriceInfo.getFee()).thenReturn(sellFee);
        SellInfo sellInfo = mock(SellInfoImpl.class);
        when(sellInfo.getPriceInfo()).thenReturn(sellPriceInfo);
        when(zonky.getSellInfo(anyLong())).thenReturn(sellInfo);
    }

    @Test
    void atRisk() {
        final Investment i = MockInvestmentBuilder.fresh()
            .set(InvestmentImpl::setRating, Rating.D)
            .set(InvestmentImpl::setRemainingPrincipal, Money.from(BigDecimal.TEN))
            .build();
        final Statistics stats = mock(StatisticsImpl.class);
        final RiskPortfolio r = new RiskPortfolioImpl(i.getRating(), Money.ZERO, Money.ZERO, i.getRemainingPrincipal()
            .orElseThrow());
        when(stats.getRiskPortfolio()).thenReturn(Collections.singletonList(r));
        final Zonky zonky = harmlessZonky();
        when(zonky.getStatistics()).thenReturn(stats);
        when(zonky.getDelinquentInvestments()).thenReturn(Stream.of(i));
        final Tenant tenant = mockTenant(zonky);
        final Map<Rating, Money> result = Util.getAmountsAtRisk(tenant);
        assertThat(result).containsOnlyKeys(Rating.D);
        assertThat(result.get(Rating.D)).isEqualTo(Money.from(10));
    }

    @Test
    void sellable() {
        final Investment i = MockInvestmentBuilder.fresh()
            .set(InvestmentImpl::setRating, Rating.D)
            .set(InvestmentImpl::setRemainingPrincipal, Money.from(BigDecimal.TEN))
            .set(InvestmentImpl::setLoanHealthInfo, LoanHealth.HEALTHY)
            .set(InvestmentImpl::setSmpFee, Money.from(BigDecimal.ONE))
            .build();
        final Investment i2 = MockInvestmentBuilder.fresh()
            .set(InvestmentImpl::setRating, Rating.A)
            .set(InvestmentImpl::setRemainingPrincipal, Money.from(BigDecimal.ONE))
            .set(InvestmentImpl::setLoanHealthInfo, LoanHealth.HISTORICALLY_IN_DUE)
            .build();
        final Investment i3 = MockInvestmentBuilder.fresh()
            .set(InvestmentImpl::setRating, Rating.C)
            .set(InvestmentImpl::setRemainingPrincipal, Money.from(BigDecimal.ZERO))
            .set(InvestmentImpl::setLoanHealthInfo, LoanHealth.HEALTHY)
            .build();
        final Zonky zonky = harmlessZonky();
        mockSellInfo(zonky, BigDecimal.TEN, BigDecimal.ZERO);
        when(zonky.getInvestments((Select) any())).thenReturn(Stream.of(i, i2, i3));
        final Tenant tenant = mockTenant(zonky);
        final Tuple2<Map<Rating, Money>, Map<Rating, Money>> result = Util.getAmountsSellable(tenant);
        assertThat(result._1).containsOnlyKeys(Rating.D, Rating.A);
        assertThat(result._2).containsOnlyKeys(Rating.A);
    }

}
