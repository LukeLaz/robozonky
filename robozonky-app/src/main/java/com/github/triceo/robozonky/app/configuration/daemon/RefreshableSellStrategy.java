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

package com.github.triceo.robozonky.app.configuration.daemon;

import java.net.URL;
import java.util.Optional;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.api.strategies.SellStrategy;
import com.github.triceo.robozonky.common.extensions.StrategyLoader;
import com.github.triceo.robozonky.util.Scheduler;

final class RefreshableSellStrategy extends RefreshableStrategy<SellStrategy> {

    public static Refreshable<SellStrategy> create(final String maybeUrl) {
        final Refreshable<SellStrategy> r = new RefreshableSellStrategy(RefreshableStrategy.convertToUrl(maybeUrl));
        Scheduler.BACKGROUND_SCHEDULER.submit(r);
        return r;
    }

    private RefreshableSellStrategy(final URL target) {
        super(target);
    }

    @Override
    protected Optional<SellStrategy> transform(final String source) {
        return StrategyLoader.toSell(source);
    }
}
