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

package com.github.robozonky.strategy.natural.conditions;

import static org.mockito.Mockito.*;

import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.strategies.LoanDescriptor;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.strategy.natural.wrappers.Wrapper;
import com.github.robozonky.test.mock.MockLoanBuilder;

class LoanRatingEnumeratedConditionSpec implements AbstractEnumeratedConditionTest.ConditionSpec<Rating> {

    private static final PortfolioOverview FOLIO = mock(PortfolioOverview.class);

    @Override
    public AbstractEnumeratedCondition<Rating> getImplementation() {
        return new LoanRatingEnumeratedCondition();
    }

    @Override
    public Wrapper<?> getMocked() {
        final LoanImpl loan = new MockLoanBuilder()
            .set(LoanImpl::setRating, this.getTriggerItem())
            .build();
        return Wrapper.wrap(new LoanDescriptor(loan), FOLIO);
    }

    @Override
    public Rating getTriggerItem() {
        return Rating.A;
    }

    @Override
    public Rating getNotTriggerItem() {
        return Rating.D;
    }
}
