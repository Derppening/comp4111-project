package comp4111.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TokenManagerTest {

    private static String getUsername(int i) {
        return String.format("%3s", i);
    }

    private Map<String, String> backingMap;
    private TokenManager tokenMgr;

    @BeforeEach
    void setUp() {
        backingMap = TokenManager.DEFAULT_MAP_SUPPLIER.get();
        tokenMgr = new TokenManager(backingMap);
    }

    @Test
    void tryCreateToken() {
        final var token = tokenMgr.newToken(getUsername(1));

        assertNotNull(token);
        assertEquals(1, backingMap.size());
    }

    @Test
    void givenUserLoggedIn_tryCreateToken() {
        assumeTrue(tokenMgr.newToken(getUsername(1)) != null);

        assertNull(tokenMgr.newToken(getUsername(1)));
        assertEquals(1, backingMap.size());
    }

    @Test
    void givenUserLoggedIn_tryLoginAnotherUser() {
        assumeTrue(tokenMgr.newToken(getUsername(1)) != null);

        assertNotNull(tokenMgr.newToken(getUsername(2)));
        assertEquals(2, backingMap.size());
    }

    @Test
    void givenTokenExists_assertContainsToken() {
        final var token = tokenMgr.newToken(getUsername(1));
        assumeTrue(token != null);

        assertTrue(tokenMgr.containsToken(token));
    }

    @Test
    void givenTokenNotExists_assertNotContainsToken() {
        assertFalse(tokenMgr.containsToken(""));
    }

    @Test
    void givenTokenExists_assertContainsUser() {
        final var user = getUsername(1);

        assumeTrue(tokenMgr.newToken(user) != null);

        assertTrue(tokenMgr.containsUser(user));
    }

    @Test
    void givenTokenNotExists_assertNotContainsUser() {
        assertFalse(tokenMgr.containsUser(getUsername(1)));
    }

    @Test
    void givenTokenExists_tryRemoveToken() {
        final var token = tokenMgr.newToken(getUsername(1));
        assumeTrue(token != null);

        assertTrue(tokenMgr.removeToken(token));
        assertEquals(0, backingMap.size());
    }

    @Test
    void givenTokenNotExists_tryRemoveToken() {
        assertFalse(tokenMgr.removeToken(""));
    }

    @Test
    void givenMultiTokenExists_whenRemoveToken_oneTokenRemoved() {
        final var token = tokenMgr.newToken(getUsername(1));
        assumeTrue(token != null);

        assumeTrue(tokenMgr.newToken(getUsername(2)) != null);
        assumeTrue(tokenMgr.newToken(getUsername(3)) != null);

        assertTrue(tokenMgr.removeToken(token));
        assertEquals(2, backingMap.size());
    }

    @AfterEach
    void tearDown() {
        tokenMgr = null;
        backingMap = null;
    }
}
