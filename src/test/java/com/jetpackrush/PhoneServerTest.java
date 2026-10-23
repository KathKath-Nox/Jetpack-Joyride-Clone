package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class PhoneServerTest {

    private static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient client, String url, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void servesThePageAndForwardsInput() throws Exception {
        PhoneInput phone = new PhoneInput();
        PhoneServer server = PhoneServer.start(phone);
        try {
            String local = server.getUrl().replaceFirst("//[^:/]+:", "//127.0.0.1:");
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> page = get(client, local);
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("fetch('input'"));

            assertEquals(204, post(client, local + "input", "1:down").statusCode());
            assertTrue(phone.isThrustPressed());
        } finally {
            server.stop();
        }
    }

    @Test
    void refusesRequestsWithoutTheSecretToken() throws Exception {
        PhoneInput phone = new PhoneInput();
        PhoneServer server = PhoneServer.start(phone);
        try {
            String local = server.getUrl().replaceFirst("//[^:/]+:", "//127.0.0.1:");
            String base = local.substring(0, local.indexOf('/', "http://".length()));
            HttpClient client = HttpClient.newHttpClient();
            assertEquals(404, get(client, base + "/").statusCode());
            assertEquals(404, get(client, base + "/0000000000000000/").statusCode());
            assertEquals(404, post(client, base + "/input", "1:down").statusCode());
            assertEquals(404, post(client, base + "/0000000000000000/input", "1:down").statusCode());
            assertFalse(phone.isThrustPressed());
        } finally {
            server.stop();
        }
    }

    @Test
    void findsTheTunnelAddressInSshOutput() {
        assertEquals("https://ab12cd.lhr.life",
            PhoneServer.parseTunnelUrl("ab12cd.lhr.life tunneled with tls termination, https://ab12cd.lhr.life"));
        assertNull(PhoneServer.parseTunnelUrl("To set up custom domains go to https://admin.localhost.run/"));
        assertNull(PhoneServer.parseTunnelUrl("https://localhost.run/docs/"));
        assertNull(PhoneServer.parseTunnelUrl("random line"));
    }
}
