package cn.edu.whut.sept.zuul.game.user.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("test-secret-key-for-junit-testing-purposes-12345678", 60000);

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(42);
        assertEquals(42, jwtUtil.validateToken(token));
    }

    @Test
    void shouldFailOnExpiredToken() throws InterruptedException {
        JwtUtil shortLived = new JwtUtil("test-secret-key-for-expired-token-test-1234567890", 1);
        String token = shortLived.generateToken(1);
        Thread.sleep(10);
        assertThrows(Exception.class, () -> shortLived.validateToken(token));
    }

    @Test
    void shouldFailOnInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void tokensForDifferentUsersShouldDiffer() {
        String t1 = jwtUtil.generateToken(1);
        String t2 = jwtUtil.generateToken(2);
        assertNotEquals(t1, t2);
    }

    @Test
    void shouldRoundTripMultipleUsers() {
        for (int i = 1; i <= 10; i++) {
            assertEquals(i, jwtUtil.validateToken(jwtUtil.generateToken(i)));
        }
    }
}
