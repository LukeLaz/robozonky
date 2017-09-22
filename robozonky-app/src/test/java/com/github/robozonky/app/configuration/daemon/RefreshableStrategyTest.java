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

package com.github.robozonky.app.configuration.daemon;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.github.robozonky.api.Refreshable;
import com.github.robozonky.api.strategies.InvestmentStrategy;
import com.github.robozonky.api.strategies.PurchaseStrategy;
import com.github.robozonky.api.strategies.SellStrategy;
import com.github.robozonky.internal.api.Defaults;
import com.google.common.io.Files;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class RefreshableStrategyTest {

    private final File strategy;

    public RefreshableStrategyTest() throws IOException {
        strategy = File.createTempFile("robozonky-strategy", ".cfg");
        Files.write("Robot má udržovat konzervativní portfolio.", strategy, Defaults.CHARSET);
    }

    @Test
    public void loadStrategyAsFile() throws InterruptedException {
        final Refreshable<InvestmentStrategy> r = RefreshableInvestmentStrategy.create(strategy.getAbsolutePath());
        Assertions.assertThat(r.getLatest()).isPresent();
    }

    @Test
    public void loadStrategyAsUrl() throws MalformedURLException, InterruptedException {
        final String url = strategy.toURI().toURL().toString();
        final Refreshable<InvestmentStrategy> r = RefreshableInvestmentStrategy.create(url);
        Assertions.assertThat(r.getLatest()).isPresent();
    }

    @Test
    public void loadPurchaseStrategy() {
        final Refreshable<PurchaseStrategy> r = RefreshablePurchaseStrategy.create(strategy.getAbsolutePath());
        Assertions.assertThat(r.getLatest()).isPresent();
    }

    @Test
    public void loadSellStrategy() {
        final Refreshable<SellStrategy> r = RefreshableSellStrategy.create(strategy.getAbsolutePath());
        Assertions.assertThat(r.getLatest()).isPresent();
    }
}
