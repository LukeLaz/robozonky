/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.app.tenant;

import java.math.BigDecimal;

import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.tenant.RemotePortfolio;
import com.github.robozonky.internal.tenant.Tenant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

class RemotePortfolioImplTest extends AbstractZonkyLeveragingTest {

    @Test
    void throwsWhenRemoteFails() {
        final Zonky zonky = harmlessZonky();
        final Tenant tenant = mockTenant(zonky);
        doThrow(IllegalStateException.class).when(zonky).getStatistics();
        final RemotePortfolio p = new RemotePortfolioImpl(tenant);
        assertThatThrownBy(p::getOverview)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void chargesAffectAmounts() {
        final Zonky zonky = harmlessZonky();
        final Tenant tenant = mockTenant(zonky);
        final RemotePortfolio p = new RemotePortfolioImpl(tenant);
        assertThat(p.getTotal()).isEmpty();
        assertThat(p.getOverview().getCzkInvested()).isEqualTo(BigDecimal.ZERO);
        p.simulateCharge(1, Rating.D, BigDecimal.TEN);
        assertThat(p.getTotal()).containsOnlyKeys(Rating.D)
                .containsValues(BigDecimal.TEN);
        p.simulateCharge(2, Rating.A, BigDecimal.ONE);
        assertThat(p.getTotal()).containsOnlyKeys(Rating.A, Rating.D)
                .containsValues(BigDecimal.ONE, BigDecimal.TEN);
        p.simulateCharge(3, Rating.A, BigDecimal.ONE);
        assertThat(p.getTotal()).containsOnlyKeys(Rating.A, Rating.D)
                .containsValues(BigDecimal.valueOf(2), BigDecimal.TEN);
    }

}
