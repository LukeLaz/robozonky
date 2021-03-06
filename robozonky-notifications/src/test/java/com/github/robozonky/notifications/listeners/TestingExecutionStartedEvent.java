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

import java.time.OffsetDateTime;

import com.github.robozonky.api.notifications.ExecutionStartedEvent;
import com.github.robozonky.api.strategies.PortfolioOverview;

final class TestingExecutionStartedEvent implements ExecutionStartedEvent {

    private final PortfolioOverview p;

    public TestingExecutionStartedEvent(final PortfolioOverview p) {
        this.p = p;
    }

    @Override
    public PortfolioOverview getPortfolioOverview() {
        return p;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return null;
    }
}
