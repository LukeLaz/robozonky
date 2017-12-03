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

package com.github.robozonky.app.portfolio;

import java.util.function.BiFunction;

import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.common.remote.Zonky;

/**
 * Denotes a function that is capable of retrieving a {@link Loan} from some source, be it remote or local. To that end,
 * it will use {@link Integer} ID of the loan and, optionally, instance of the {@link Zonky} API to contact the remote
 * server.
 */
@FunctionalInterface
interface LoanProvider extends BiFunction<Integer, Zonky, Loan> {

}