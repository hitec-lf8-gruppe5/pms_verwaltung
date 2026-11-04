package de.szut.pms.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.bean.override.convention.TestBean;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/**
 * Integrationstests für die Story "Zugriff nur mit gültigem Authentik-Token".
 * <p>
 * Die Tests schicken echte, signierte Token statt {@code @WithMockUser}. Weil im Test kein
 * Authentik läuft, prüft der JwtDecoder die Signatur hier gegen unseren Testschlüssel.
 * Signatur und Ablaufdatum prüft er mit denselben Standardregeln wie im echten Betrieb.
 */
class TokenSecurityTest extends AbstractIntegrationTest {

    private static final KeyPair TEST_KEY = newRsaKey();
    private static final KeyPair OTHER_KEY = newRsaKey();

    @TestBean
    private JwtDecoder jwtDecoder;

    /** Ersetzt im Test den JwtDecoder aus der AuthentikSecurityConfig. */
    static JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) TEST_KEY.getPublic()).build();
    }

    private static KeyPair newRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Baut einen signierten Token, der zum Zeitpunkt expiresAt abläuft. */
    private String signedToken(KeyPair key, Instant expiresAt) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("john")
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        token.sign(new RSASSASigner(key.getPrivate()));
        return "Bearer " + token.serialize();
    }

    private Instant inOneHour() {
        return Instant.now().plus(1, ChronoUnit.HOURS);
    }

    /** Vor einer Stunde: Der Decoder toleriert nur 60 Sekunden Abweichung. */
    private Instant oneHourAgo() {
        return Instant.now().minus(1, ChronoUnit.HOURS);
    }

    @Test
    void validTokenIsAccepted() throws Exception {
        mockMvc.perform(get("/projects").header(AUTHORIZATION, signedToken(TEST_KEY, inOneHour())))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/projects").header(AUTHORIZATION, "Bearer das-ist-kein-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        mockMvc.perform(get("/projects").header(AUTHORIZATION, signedToken(OTHER_KEY, inOneHour())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        mockMvc.perform(get("/projects").header(AUTHORIZATION, signedToken(TEST_KEY, oneHourAgo())))
                .andExpect(status().isUnauthorized());
    }

    /** Die Swagger-Dokumentation muss auch ohne Token erreichbar sein. */
    @Test
    void swaggerIsReachableWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    /** Die Absicherung gilt für alle Endpunkte, nicht nur für GET /projects. */
    @Test
    void everyEndpointRejectsAnExpiredToken() throws Exception {
        String expired = signedToken(TEST_KEY, oneHourAgo());

        mockMvc.perform(post("/projects").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/projects").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/projects/1").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/projects/1").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/projects/1").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/projects/1/employees").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/projects/1/employees").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/projects/1/employees/1").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/employees/1/projects").header(AUTHORIZATION, expired))
                .andExpect(status().isUnauthorized());
    }
}
