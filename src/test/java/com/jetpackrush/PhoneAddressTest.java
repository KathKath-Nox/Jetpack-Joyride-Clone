package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class PhoneAddressTest {

    @Test
    void addressIsNotADockerBridge() {
        String address = PhoneServer.findLanAddress();
        assertFalse(address.startsWith("172.17.") || address.startsWith("172.18.")
            || address.startsWith("172.19.") || address.startsWith("172.20."), address);
    }
}
