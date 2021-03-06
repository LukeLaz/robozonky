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

package com.github.robozonky.app.version;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.robozonky.internal.util.functional.Either;
import com.github.robozonky.test.AbstractRoboZonkyTest;

@ExtendWith(MockServerExtension.class)
class MavenMetadataParserTest extends AbstractRoboZonkyTest {

    private final ClientAndServer server;
    private final String serverUrl;

    public MavenMetadataParserTest(ClientAndServer server) {
        this.server = server;
        this.serverUrl = "http://127.0.0.1:" + server.getLocalPort();
    }

    @Test
    void checkNullVersion() {
        final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky", "robozonky");
        final Either<Throwable, Response> result = parser.apply(null);
        assertThat(result.get()).isEqualTo(Response.noMoreRecentVersion());
    }

    @Test
    void checkEmptyVersion() {
        final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky", "robozonky");
        final Either<Throwable, Response> result = parser.apply("");
        assertThat(result.get()).isEqualTo(Response.noMoreRecentVersion());
    }

    @Test
    void checkNonExistentUrl() {
        server.when(HttpRequest.request())
            .respond(HttpResponse.notFoundResponse());
        final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                "robozonky-nonexistent");
        final Either<Throwable, Response> result = parser.apply(UUID.randomUUID()
            .toString());
        assertThat(result.getLeft()).isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void parseSingleNodeList() {
        final String version = "1.2.3";
        final Node n = mock(Node.class);
        when(n.getTextContent()).thenReturn(version);
        final NodeList l = mock(NodeList.class);
        when(l.getLength()).thenReturn(1);
        when(l.item(eq(0))).thenReturn(n);
        final List<String> actual = MavenMetadataParser.extractItems(l);
        assertThat(actual).containsExactly(version);
    }

    @Test
    void parseLongerNodeList() {
        final String version = "1.2.3", version2 = "1.2.4-SNAPSHOT";
        final Node n1 = mock(Node.class);
        when(n1.getTextContent()).thenReturn(version);
        final Node n2 = mock(Node.class);
        when(n2.getTextContent()).thenReturn(version2);
        final NodeList l = mock(NodeList.class);
        when(l.getLength()).thenReturn(2);
        when(l.item(eq(0))).thenReturn(n1);
        when(l.item(eq(1))).thenReturn(n2);
        final List<String> actual = MavenMetadataParser.extractItems(l);
        assertThat(actual).containsExactly(version, version2);
    }

    @Nested
    class ValidMetadata {

        @BeforeEach
        void setupMetadata() { // This is a mocked response of the Central Search REST API.
            server.when(HttpRequest.request()
                .withPath("/solrsearch/select"))
                .respond(HttpResponse.response()
                    .withBody("{\"responseHeader\":{\"status\":0,\"QTime\":1,\"params\":{\"q\":\"g:\\\"com.github" +
                            ".robozonky\\\" AND a:\\\"robozonky\\\"\",\"core\":\"gav\",\"indent\":\"off\"," +
                            "\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"start\":\"\",\"sort\":\"score desc," +
                            "timestamp desc,g asc,a asc,v desc\",\"rows\":\"100\",\"wt\":\"json\"," +
                            "\"version\":\"2.2\"}},\"response\":{\"numFound\":5,\"start\":0," +
                            "\"docs\":[" +
                            "{\"id\":\"com.github.robozonky:robozonky:4.8" +
                            ".0-cr-2\",\"g\":\"com.github.robozonky\",\"a\":\"robozonky\",\"v\":\"4.8" +
                            ".0-cr-2\",\"p\":\"pom\",\"timestamp\":1535571473000,\"ec\":[\".pom\"]," +
                            "\"tags\":[\"define\",\"profit\",\"automated\",\"strategy\",\"zonky\"," +
                            "\"investment\",\"investing\"]}," +
                            "{\"id\":\"com.github.robozonky:robozonky:4.8" +
                            ".0-cr-1\",\"g\":\"com.github.robozonky\",\"a\":\"robozonky\",\"v\":\"4.8" +
                            ".0-cr-1\",\"p\":\"pom\",\"timestamp\":1535483325000,\"ec\":[\".pom\"]," +
                            "\"tags\":[\"define\",\"profit\",\"automated\",\"strategy\",\"zonky\"," +
                            "\"investment\",\"investing\"]}," +
                            "{\"id\":\"com.github.robozonky:robozonky:4.7.7" +
                            ".7\",\"g\":\"com.github.robozonky\",\"a\":\"robozonky\",\"v\":\"4.7.7\"," +
                            "\"p\":\"pom\",\"timestamp\":1536400436000,\"ec\":[\".pom\"]," +
                            "\"tags\":[\"define\",\"profit\",\"automated\",\"strategy\",\"zonky\"," +
                            "\"investment\",\"investing\"]}," +
                            "{\"id\":\"com.github.robozonky:robozonky:4.7" +
                            ".6\",\"g\":\"com.github.robozonky\",\"a\":\"robozonky\",\"v\":\"4.7.6\"," +
                            "\"p\":\"pom\",\"timestamp\":1533972934000,\"ec\":[\".pom\"]," +
                            "\"tags\":[\"define\",\"profit\",\"automated\",\"strategy\",\"zonky\"," +
                            "\"investment\",\"investing\"]}," +
                            "{\"id\":\"com.github.robozonky:robozonky:4.7" +
                            ".6-cr-1\",\"g\":\"com.github.robozonky\",\"a\":\"robozonky\",\"v\":\"4.7.6-cr-1\"," +
                            "\"p\":\"pom\",\"timestamp\":1533972934000,\"ec\":[\".pom\"]," +
                            "\"tags\":[\"define\",\"profit\",\"automated\",\"strategy\",\"zonky\"," +
                            "\"investment\",\"investing\"]}]}}"));
        }

        @Test
        void checkUnknownVersion() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply(UUID.randomUUID()
                .toString());
            assertThat(result.getLeft()).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void checkLatestVersionBothOutdated() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply("4.7.6");
            assertThat(result.get()).isEqualTo(Response.moreRecent("4.7.7", "4.8.0-cr-2"));
        }

        @Test
        void checkLatestVersionExperimentalMoreRecent() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply("4.7.7");
            assertThat(result.get()).isEqualTo(Response.moreRecentExperimental("4.8.0-cr-2"));
        }

        @Test
        void checkLatestVersionExperimentalEvenMoreRecent() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply("4.7.6-cr-1");
            assertThat(result.get()).isEqualTo(Response.moreRecent("4.7.7", "4.8.0-cr-2"));
        }

        @Test
        void checkLatestVersionExperimentalYetMoreRecent() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply("4.8.0-cr-1");
            assertThat(result.get()).isEqualTo(Response.moreRecentExperimental("4.8.0-cr-2"));
        }

        @Test
        void checkLatestVersionNoneMoreRecent() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply("4.8.0-cr-2");
            assertThat(result.get()).isEqualTo(Response.noMoreRecentVersion());
        }
    }

    @Nested
    class FailingMetadata {

        @BeforeEach
        void setupMetadata() {
            server.when(HttpRequest.request()
                .withPath("/maven2/com/github/robozonky/robozonky/maven-metadata.xml"))
                .respond(HttpResponse.notFoundResponse());
        }

        @Test
        void checkUnknownVersion() {
            final MavenMetadataParser parser = new MavenMetadataParser(serverUrl, "com.github.robozonky",
                    "robozonky");
            final Either<Throwable, Response> result = parser.apply(UUID.randomUUID()
                .toString());
            assertThat(result.getLeft())
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(FileNotFoundException.class);
        }

    }
}
