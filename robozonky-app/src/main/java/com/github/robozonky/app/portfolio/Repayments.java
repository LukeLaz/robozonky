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

package com.github.robozonky.app.portfolio;

import java.time.LocalDate;
import java.util.Optional;

import com.github.robozonky.api.notifications.Event;
import com.github.robozonky.api.notifications.LoanRepaidEvent;
import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.remote.enums.PaymentStatus;
import com.github.robozonky.api.remote.enums.TransactionCategory;
import com.github.robozonky.api.remote.enums.TransactionOrientation;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.app.authentication.Tenant;
import com.github.robozonky.app.configuration.daemon.TransactionalPortfolio;
import com.github.robozonky.app.util.LoanCache;
import com.github.robozonky.common.remote.Select;
import com.github.robozonky.common.state.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Repayments implements PortfolioDependant {

    private static final Logger LOGGER = LoggerFactory.getLogger(Repayments.class);

    @Override
    public void accept(final TransactionalPortfolio transactionalPortfolio) {
        final Tenant tenant = transactionalPortfolio.getTenant();
        final InstanceState<Repayments> state = tenant.getState(Repayments.class);
        if (state.isInitialized()) {
            final Portfolio portfolio = transactionalPortfolio.getPortfolio();
            final PortfolioOverview portfolioOverview = portfolio.calculateOverview();
            // Zonky payment processing happens at the end of each day, payments are dated to the beginning of that day
            final LocalDate lastZonkyUpdate = portfolio.getStatistics().getTimestamp().toLocalDate();
            final LocalDate lastRepaymentUpdate = state.getLastUpdated().get().toLocalDate();
            // read all payment notifications that happened since last checked
            final Select sinceLastChecked = new Select()
                    .lessThan("transaction.transactionDate", lastZonkyUpdate)
                    .greaterThanOrEquals("transaction.transactionDate", lastRepaymentUpdate);
            tenant.call(z -> z.getTransactions(sinceLastChecked))
                    .parallel()
                    .filter(t -> t.getCategory() == TransactionCategory.PAYMENT)
                    .filter(t -> t.getOrientation() == TransactionOrientation.IN)
                    .forEach(t -> {
                        LOGGER.debug("Processing transaction: {}.", t);
                        final Loan l = tenant.call(zonky -> LoanCache.INSTANCE.getLoan(t.getLoanId(), zonky));
                        final Optional<Investment> i = tenant.call(z -> z.getInvestment(l));
                        if (!i.isPresent()) {
                            LOGGER.debug("Investment for loan #{} not found, probably sold since.", l.getId());
                            return;
                        }
                        final Investment actual = i.get();
                        actual.getPaymentStatus().ifPresent(s -> {
                            if (s != PaymentStatus.PAID) {
                                return;
                            }
                            final Event e = new LoanRepaidEvent(actual, l, portfolioOverview);
                            transactionalPortfolio.fire(e);
                        });
                    });
        }
        state.reset(); // initialize state with today's update date
    }
}
