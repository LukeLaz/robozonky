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

package com.github.triceo.robozonky.strategy.natural;

import java.util.Collection;
import java.util.stream.Stream;

import com.github.triceo.robozonky.api.strategies.InvestmentDescriptor;
import com.github.triceo.robozonky.api.strategies.PortfolioOverview;
import com.github.triceo.robozonky.api.strategies.RecommendedInvestment;
import com.github.triceo.robozonky.api.strategies.SellStrategy;

public class NaturalLanguageSellStrategy implements SellStrategy {

    private final ParsedStrategy strategy;

    public NaturalLanguageSellStrategy(final ParsedStrategy p) {
        this.strategy = p;
    }

    @Override
    public Stream<RecommendedInvestment> recommend(final Collection<InvestmentDescriptor> available,
                                                   final PortfolioOverview portfolio) {
        return strategy.getApplicableInvestments(available)
                .map(InvestmentDescriptor::recommend) // must do full amount; Zonky enforces
                .flatMap(r -> r.map(Stream::of).orElse(Stream.empty()));
    }
}
