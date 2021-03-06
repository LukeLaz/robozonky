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

import com.github.robozonky.strategy.natural.wrappers.Wrapper;

public class LoanTermCondition extends AbstractRangeCondition<Integer> {

    private LoanTermCondition(final RangeCondition<Integer> condition) {
        super(condition, false);
    }

    public static LoanTermCondition lessThan(final int threshold) {
        final RangeCondition<Integer> c = RangeCondition.lessThan(Wrapper::getRemainingTermInMonths,
                LOAN_TERM_IN_MONTHS_DOMAIN, threshold);
        return new LoanTermCondition(c);
    }

    public static LoanTermCondition moreThan(final int threshold) {
        final RangeCondition<Integer> c = RangeCondition.moreThan(Wrapper::getRemainingTermInMonths,
                LOAN_TERM_IN_MONTHS_DOMAIN, threshold);
        return new LoanTermCondition(c);
    }

    public static LoanTermCondition exact(final int minimumThreshold, final int maximumThreshold) {
        final RangeCondition<Integer> c = RangeCondition.exact(Wrapper::getRemainingTermInMonths,
                LOAN_TERM_IN_MONTHS_DOMAIN, minimumThreshold,
                maximumThreshold);
        return new LoanTermCondition(c);
    }
}
