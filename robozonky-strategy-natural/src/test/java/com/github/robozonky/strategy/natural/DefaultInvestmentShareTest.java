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

package com.github.robozonky.strategy.natural;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class DefaultInvestmentShareTest {

    @Test
    void shareBoundaries() {
        assertThatThrownBy(() -> new DefaultInvestmentShare(-1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DefaultInvestmentShare(101))
            .isInstanceOf(IllegalArgumentException.class);
        final DefaultInvestmentShare s = new DefaultInvestmentShare(0);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(s.getMinimumShareInPercent())
                .isEqualTo(0);
            softly.assertThat(s.getMaximumShareInPercent())
                .isEqualTo(0);
        });
        final DefaultInvestmentShare s2 = new DefaultInvestmentShare(100);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(s2.getMinimumShareInPercent())
                .isEqualTo(0);
            softly.assertThat(s2.getMaximumShareInPercent())
                .isEqualTo(100);
        });
    }

}
