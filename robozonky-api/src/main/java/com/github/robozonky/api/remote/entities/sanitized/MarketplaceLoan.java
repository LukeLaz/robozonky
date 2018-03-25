/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.api.remote.entities.sanitized;

import java.math.BigDecimal;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import com.github.robozonky.api.remote.entities.MyInvestment;
import com.github.robozonky.api.remote.entities.Photo;
import com.github.robozonky.api.remote.entities.RawLoan;
import com.github.robozonky.api.remote.enums.MainIncomeType;
import com.github.robozonky.api.remote.enums.Purpose;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.remote.enums.Region;

public interface MarketplaceLoan {

    static MarketplaceLoan sanitized(final RawLoan original) {
        return MarketplaceLoan.sanitize(original).build();
    }

    static MarketplaceLoanBuilder custom() {
        return new MutableMarketplaceLoanImpl();
    }

    static MarketplaceLoanBuilder sanitize(final RawLoan original) {
        return new MutableMarketplaceLoanImpl(original);
    }

    MainIncomeType getMainIncomeType();

    BigDecimal getInvestmentRate();

    Region getRegion();

    Purpose getPurpose();

    int getId();

    String getName();

    String getStory();

    String getNickName();

    int getTermInMonths();

    BigDecimal getInterestRate();

    Rating getRating();

    boolean isTopped();

    int getAmount();

    int getRemainingInvestment();

    boolean isCovered();

    boolean isPublished();

    OffsetDateTime getDatePublished();

    OffsetDateTime getDeadline();

    int getInvestmentsCount();

    int getActiveLoansCount();

    int getQuestionsCount();

    boolean isQuestionsAllowed();

    Collection<Photo> getPhotos();

    boolean isInsuranceActive();

    int getUserId();

    URL getUrl();

    Optional<MyInvestment> getMyInvestment();
}