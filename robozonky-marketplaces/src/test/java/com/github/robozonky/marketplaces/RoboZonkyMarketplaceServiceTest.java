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

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RoboZonkyMarketplaceServiceTest {

    @Test
    void zotifyRetrieval() {
        assertThat(new RobozonkyMarketplaceService().find("zotify"))
                .isPresent().containsInstanceOf(ZotifyMarketplace.class);
    }

    @Test
    void zonkyRetrieval() {
        assertThat(new RobozonkyMarketplaceService().find("zonky"))
                .isPresent().containsInstanceOf(ZonkyMarketplace.class);
    }

    @Test
    void nonexistent() {
        assertThat(new RobozonkyMarketplaceService().find(UUID.randomUUID().toString())).isEmpty();
    }
}
