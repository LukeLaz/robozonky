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

package com.github.robozonky.test.mock;

import com.github.robozonky.api.remote.entities.Reservation;
import com.github.robozonky.internal.remote.entities.ReservationImpl;

public class MockReservationBuilder extends BaseLoanMockBuilder<ReservationImpl, MockReservationBuilder> {

    public static Reservation fresh() {
        return new MockReservationBuilder().build();
    }

    public MockReservationBuilder() {
        super(ReservationImpl.class);
        set(ReservationImpl::setId, RANDOM.nextInt());
    }

}
