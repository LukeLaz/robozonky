/*
 * Copyright 2016 Lukáš Petrovický
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
package com.github.triceo.robozonky;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.triceo.robozonky.events.EventRegistry;
import com.github.triceo.robozonky.events.InvestmentMadeEvent;
import com.github.triceo.robozonky.events.InvestmentRequestedEvent;
import com.github.triceo.robozonky.events.LoanEvaluationEvent;
import com.github.triceo.robozonky.events.StrategyCompleteEvent;
import com.github.triceo.robozonky.events.StrategyStartedEvent;
import com.github.triceo.robozonky.remote.BlockedAmount;
import com.github.triceo.robozonky.remote.InvestingZonkyApi;
import com.github.triceo.robozonky.remote.Investment;
import com.github.triceo.robozonky.remote.Loan;
import com.github.triceo.robozonky.remote.Statistics;
import com.github.triceo.robozonky.remote.ZonkyApi;
import com.github.triceo.robozonky.strategy.InvestmentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the investments based on the strategy, user portfolio and balance.
 */
public class Investor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Investor.class);

    /**
     * Determine whether or not a given loan is present among existing investments.
     *
     * @param loan Loan in question.
     * @param investments Known investments.
     * @return True if present.
     */
    private static boolean isLoanPresent(final Loan loan, final Collection<Investment> investments) {
        return investments.stream().filter(i -> loan.getId() == i.getLoanId()).findFirst().isPresent();
    }

    /**
     * The core investing call. Receives a particular loan, checks if the user has enough money to invest, and sends the
     * command to the Zonky API.
     *
     * @param api Authenticated Zonky API, ready for investing.
     * @param loanId Loan to invest into. Will be fetched fresh to make sure it is really available.
     * @param amount Amount to invest into the loan.
     * @param balance How much usable cash the user has in the wallet.
     * @return Present if input valid and operation succeeded, empty otherwise.
     */
    private static Optional<Investment> invest(final ZonkyApi api, final int loanId, final int amount,
                                               final int balance) {
        if (amount < InvestmentStrategy.MINIMAL_INVESTMENT_ALLOWED) {
            Investor.LOGGER.info("Not investing into loan #{}, since investment ({} CZK) less than bare minimum.",
                    loanId, amount);
            return Optional.empty();
        } else if (amount > balance) { // strategy should not allow this
            Investor.LOGGER.info("Not investing into loan #{}, {} CZK to invest is more than {} CZK balance.", loanId,
                    amount, balance);
            return Optional.empty();
        }
        final Loan l = api.getLoan(loanId);
        if (amount > l.getRemainingInvestment()) {
            Investor.LOGGER.info("Not investing into loan '{}', {} CZK to invest is more than {} CZK loan remaining.",
                    l, amount, l.getRemainingInvestment());
            return Optional.empty();
        }
        final Investment investment = new Investment(l, amount);
        EventRegistry.fire(new InvestmentRequestedEvent(investment));
        if (api instanceof InvestingZonkyApi) {
            ((InvestingZonkyApi)api).invest(investment);
            Investor.LOGGER.info("Invested {} CZK into loan {}.", investment.getAmount(), investment.getLoanId());
        } else {
            Investor.LOGGER.info("Dry run. Otherwise would have invested {} CZK into loan {}.", investment.getAmount(),
                    investment.getLoanId());
        }
        EventRegistry.fire(new InvestmentMadeEvent(investment));
        return Optional.of(investment);
    }

    /**
     * Blocked amounts represent loans in various stages. Either the user has invested and the loan has not yet been
     * funded to 100 % ("na tržišti"), or the user invested and the loan has been funded ("na cestě"). In the latter
     * case, the loan has already disappeared from the marketplace, which means that it will not be available for
     * investing any more. As far as I know, the next stage is "v pořádku", the blocked amount is cleared and the loan
     * becomes an active investment.
     *
     * Based on that, this method deals with the first case - when the loan is still available for investing, but we've
     * already invested as evidenced by the blocked amount. It also unnecessarily deals with the second case, since
     * that is represented by a blocked amount as well. But that does no harm.
     *
     * In case user has made repeated investments into a particular loan, this will show up as multiple blocked amounts.
     * The method needs to handle this as well.
     *
     * @param api Authenticated API that will be used to retrieve the user's blocked amounts from the wallet.
     * @return Every blocked amount represents a future investment. This method returns such investments.
     */
    static List<Investment> retrieveInvestmentsRepresentedByBlockedAmounts(final ZonkyApi api) {
        // first group all blocked amounts by the loan ID and sum them
        final Map<Integer, Integer> amountsBlockedByLoans = api.getBlockedAmounts(99, 0).stream()
                .filter(blocked -> blocked.getLoanId() > 0) // 0 == Zonky investors' fee
                .collect(Collectors.groupingBy(BlockedAmount::getLoanId,
                        Collectors.summingInt(BlockedAmount::getAmount)));
        // and then fetch all the loans in parallel, converting them into investments
        return Collections.unmodifiableList(amountsBlockedByLoans.entrySet().parallelStream()
                .map(entry -> {
                    final Loan l = LoanRetriever.getLoan(api, entry.getKey()).orElseThrow(
                            () -> new RuntimeException("Loan retrieval failed.")
                    );
                    return new Investment(l, entry.getValue());
                })
                .collect(Collectors.toList()));
    }

    /**
     * Zonky API may return {@link ZonkyApi#getStatistics()} as null if the account has no previous investments.
     *
     * @param api API to execute the operation.
     * @return Either what the API returns, or an empty object.
     */
    private static Statistics retrieveStatistics(final ZonkyApi api) {
        final Statistics returned = api.getStatistics();
        return returned == null ? new Statistics() : returned;
    }

    private final ZonkyApi zonkyApi;
    private final BigDecimal initialBalance;

    /**
     * Standard constructor.
     *  @param zonky Authenticated API ready to retrieve user information.
     * @param initialBalance How much available cash the user has in their wallet.
     */
    public Investor(final ZonkyApi zonky, final BigDecimal initialBalance) {
        this.zonkyApi = zonky;
        this.initialBalance = initialBalance;
        Investor.LOGGER.info("RoboZonky starting account balance is {} CZK.", this.initialBalance);
    }

    /**
     * Prepares a list of loans that are suitable for investment by asking the strategy. Then goes over that list one
     * by one, in the order prescribed by the strategy, and attempts to invest into these loans. The first such
     * investment operation that succeeds will return.
     *
     * @param strategy The investment strategy to pick loans from the list.
     * @param availableLoans Loans available to be chosen from.
     * @param portfolio User's investment portfolio overview.
     * @return The first {@link #invest(ZonkyApi, int, int, int)} which succeeds, or empty if none have.
     */
    Optional<Investment> investOnce(final InvestmentStrategy strategy, final List<Loan> availableLoans,
                                    final PortfolioOverview portfolio) {
        Investor.LOGGER.debug("Current share of unpaid loans with a given rating is: {}.",
                portfolio.getSharesOnInvestment());
        return strategy.getMatchingLoans(availableLoans, portfolio).stream()
                .map(l -> {
                    EventRegistry.fire(new LoanEvaluationEvent(l));
                    return Investor.invest(this.zonkyApi, l.getId(), strategy.recommendInvestmentAmount(l, portfolio),
                            portfolio.getCzkAvailable());
                })
                .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
                .findFirst();
    }

    private static <T> String collectionToString(final Collection<T> collection, final Function<T, String> reduction) {
        return collection.stream().map(reduction).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * One of the two entry points to the investment API. This takes the strategy, determines suitable loans, and tries
     * to invest in as many of them as the balance allows.
     *
     * @param strategy The investment strategy to use.
     * @param loans Loans that are available on the marketplace. These must not include CAPTCHA-protected loans.
     * @return Investments made in the session.
     */
    public Collection<Investment> invest(final InvestmentStrategy strategy, final List<Loan> loans) {
        Investor.LOGGER.debug("The following loans are available for robotic investing: {}.",
                Investor.collectionToString(loans, l -> String.valueOf(l.getId())));
        // make sure we have enough money to invest
        final BigDecimal minimumInvestmentAmount = BigDecimal.valueOf(InvestmentStrategy.MINIMAL_INVESTMENT_ALLOWED);
        BigDecimal balance = this.initialBalance;
        EventRegistry.fire(new StrategyStartedEvent(strategy, loans, balance));
        if (balance.compareTo(minimumInvestmentAmount) < 0) {
            EventRegistry.fire(new StrategyCompleteEvent(strategy, Collections.emptyList(), balance));
            return Collections.emptyList(); // no need to do anything else
        }
        // read our investment statistics
        final Statistics stats = Investor.retrieveStatistics(this.zonkyApi);
        Investor.LOGGER.debug("The sum total of principal remaining on active loans is {} CZK.",
                stats.getCurrentOverview().getPrincipalLeft());
        // figure out which loans we can still put money into
        final Collection<Investment> preexistingInvestments =
                Investor.retrieveInvestmentsRepresentedByBlockedAmounts(this.zonkyApi);
        final List<Loan> uninvestedLoans = loans.stream()
                .filter(l -> !Investor.isLoanPresent(l, preexistingInvestments))
                .collect(Collectors.toList());
        Investor.LOGGER.info("The following available loans have not yet been invested into: {}.",
                Investor.collectionToString(uninvestedLoans, l -> String.valueOf(l.getId())));
        // and start investing
        final Collection<Investment> allInvestments = new ArrayList<>(preexistingInvestments);
        do {
            final PortfolioOverview portfolio = PortfolioOverview.calculate(balance, stats, allInvestments);
            final Optional<Investment> investment = this.investOnce(strategy, uninvestedLoans, portfolio);
            if (!investment.isPresent()) { // there is nothing to invest into; RoboZonky is finished now
                break;
            }
            final Investment i = investment.get();
            // make sure the loan is removed from the uninvested loans
            uninvestedLoans.remove(uninvestedLoans.stream()
                    .filter(l -> i.getLoanId() == l.getId())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Impossible.")));
            // make sure internal counters are updated
            allInvestments.add(i);
            balance = balance.subtract(BigDecimal.valueOf(i.getAmount()));
            Investor.LOGGER.info("New account balance is {} CZK.", balance);
        } while (balance.compareTo(minimumInvestmentAmount) >= 0);
        final Collection<Investment> result = allInvestments.stream()
                .filter(i -> !preexistingInvestments.contains(i))
                .collect(Collectors.toList());
        EventRegistry.fire(new StrategyCompleteEvent(strategy, result, balance));
        return Collections.unmodifiableCollection(result);
    }

    /**
     * One of the two entry points to the investment API. Takes a particular loan and invests a given amount into it.
     *
     * @param loanId ID of the loan that should be invested into.
     * @param loanAmount Amount in CZK to be invested.
     * @return Present if investment succeeded, empty otherwise.
     */
    public Optional<Investment> invest(final int loanId, final int loanAmount) {
        return Investor.invest(this.zonkyApi, loanId, loanAmount, this.initialBalance.intValue());
    }

}
