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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.robozonky.api.Refreshable;
import com.github.robozonky.api.notifications.Event;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.Statistics;
import com.github.robozonky.api.remote.entities.Wallet;
import com.github.robozonky.api.strategies.InvestmentStrategy;
import com.github.robozonky.api.strategies.LoanDescriptor;
import com.github.robozonky.app.authentication.Authenticated;
import com.github.robozonky.common.remote.Zonky;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class StrategyExecutionTest extends AbstractInvestingTest {

    private static final InvestmentStrategy ALL_ACCEPTING_STRATEGY =
            (available, portfolio) -> available.stream().map(d -> d.recommend(200).get());
    private static final Refreshable<InvestmentStrategy> ALL_ACCEPTING_REFRESHABLE =
            new Refreshable<InvestmentStrategy>() {
                @Override
                protected Supplier<Optional<String>> getLatestSource() {
                    return () -> Optional.of("");
                }

                @Override
                protected Optional<InvestmentStrategy> transform(final String source) {
                    return Optional.of(ALL_ACCEPTING_STRATEGY);
                }
            };
    private static final InvestmentStrategy NONE_ACCEPTING_STRATEGY =
            (available, portfolio) -> Stream.empty();
    private static final Refreshable<InvestmentStrategy> NONE_ACCEPTING_REFRESHABLE =
            new Refreshable<InvestmentStrategy>() {
                @Override
                protected Supplier<Optional<String>> getLatestSource() {
                    return () -> Optional.of("");
                }

                @Override
                protected Optional<InvestmentStrategy> transform(final String source) {
                    return Optional.of(NONE_ACCEPTING_STRATEGY);
                }
            };

    static {
        ALL_ACCEPTING_REFRESHABLE.run();
        NONE_ACCEPTING_REFRESHABLE.run();
    }

    private static Zonky mockApi() {
        final Zonky zonky = Mockito.mock(Zonky.class);
        Mockito.when(zonky.getWallet()).thenReturn(new Wallet(BigDecimal.valueOf(10000), BigDecimal.valueOf(9000)));
        Mockito.when(zonky.getStatistics()).thenReturn(new Statistics());
        return zonky;
    }

    @Test
    public void noStrategy() {
        final Loan loan = new Loan(1, 2);
        final LoanDescriptor ld = new LoanDescriptor(loan);
        final Refreshable<InvestmentStrategy> r = Refreshable.createImmutable(null);
        r.run();
        final StrategyExecution exec = new StrategyExecution(null, r, null, Duration.ofMinutes(60));
        Assertions.assertThat(exec.apply(Collections.singletonList(ld))).isEmpty();
        // check events
        final List<Event> events = this.getNewEvents();
        Assertions.assertThat(events).isEmpty();
    }

    @Test
    public void someItems() {
        final Loan mock = new Loan(1, 200);
        final LoanDescriptor ld = new LoanDescriptor(mock);
        final Investor.Builder builder = new Investor.Builder().asDryRun();
        final Zonky zonky = mockApi();
        Mockito.when(zonky.getLoan(ArgumentMatchers.eq(mock.getId()))).thenReturn(mock);
        final Authenticated auth = Mockito.mock(Authenticated.class);
        Mockito.when(auth.call(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Function<Zonky, Collection<Investment>> f = invocation.getArgument(0);
            return f.apply(zonky);
        });
        final StrategyExecution exec = new StrategyExecution(builder, ALL_ACCEPTING_REFRESHABLE, auth,
                                                             Duration.ofMinutes(60));
        Assertions.assertThat(exec.apply(Collections.singletonList(ld))).isNotEmpty();
    }

    @Test
    public void noItems() {
        final Investor.Builder builder = new Investor.Builder().asDryRun();
        final Authenticated auth = Mockito.mock(Authenticated.class);
        Mockito.when(auth.call(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Function<Zonky, Collection<Investment>> f = invocation.getArgument(0);
            return f.apply(mockApi());
        });
        final StrategyExecution exec = new StrategyExecution(builder, ALL_ACCEPTING_REFRESHABLE, auth,
                                                             Duration.ofMinutes(60));
        Assertions.assertThat(exec.apply(Collections.emptyList())).isEmpty();
    }

    @Test
    public void noneAccepted() {
        final Investor.Builder builder = new Investor.Builder().asDryRun();
        final Authenticated auth = Mockito.mock(Authenticated.class);
        Mockito.when(auth.call(ArgumentMatchers.isNotNull())).thenAnswer(invocation -> {
            final Function<Zonky, Collection<Investment>> f = invocation.getArgument(0);
            return f.apply(mockApi());
        });
        final StrategyExecution exec = new StrategyExecution(builder, NONE_ACCEPTING_REFRESHABLE, auth,
                                                             Duration.ofMinutes(60));
        Assertions.assertThat(exec.apply(Collections.emptyList())).isEmpty();
    }
}