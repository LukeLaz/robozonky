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

package com.github.robozonky.marketplaces;

import java.util.Collection;

import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.common.remote.ApiProvider;

class ZotifyMarketplaceApiProvider extends ApiProvider {

    private static final String ZOTIFY_URL = "https://zotify.cz";

    /**
     * Retrieve Zotify's marketplace cache.
     * @return New API instance.
     */
    @Override
    public Collection<Loan> marketplace() {
        return this.marketplace(ZotifyApi.class, ZotifyMarketplaceApiProvider.ZOTIFY_URL);
    }
}