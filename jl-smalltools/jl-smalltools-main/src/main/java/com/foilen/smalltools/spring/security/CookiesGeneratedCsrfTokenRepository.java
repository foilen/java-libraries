/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.spring.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.hash.HashSha256;
import com.foilen.smalltools.tools.AssertTools;

/**
 * To generate a CSRF token based on some cookie values. The goal is to be able to generate the same CSRF token on different machines that runs the same application without having to share the
 * generated token (e.g. session propagation or in the database).
 *
 * <br>
 * <br>
 *
 * To be secure, there must be at least a cookie that the content changes frequently. Must not use a cookie that is machine specific (like a non-shared session id) since each machine would generate a
 * different token (which goes against the goal).
 *
 * <pre>
 * Usage:
 *
 * import org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import org.springframework.security.config.annotation.web.builders.WebSecurity;
 * import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
 * import org.springframework.security.core.userdetails.UserDetailsService;
 * import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
 * import org.springframework.security.web.csrf.CsrfTokenRepository;
 *
 * import com.foilen.smalltools.spring.security.CookiesGeneratedCsrfTokenRepository;
 *
 * public class CustomWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
 *
 *     &#064;Override
 *     protected void configure(HttpSecurity http) throws Exception {
 *
 *         http.authorizeRequests().anyRequest().fullyAuthenticated();
 *
 *         // Set the CSRF token
 *         CsrfTokenRepository csrfTokenRepository = new CookiesGeneratedCsrfTokenRepository()
 *              .setSalt(csrfSalt)
 *              .addCookieNames(&quot;foilen_user_id&quot;, &quot;foilen_date&quot;, &quot;foilen_signature&quot;);
 *         http.csrf().csrfTokenRepository(csrfTokenRepository);
 *     }
 *
 * }
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'javax.servlet:javax.servlet-api:3.1.0'
 * compile 'org.springframework.security:spring-security-web:4.2.3.RELEASE'
 * </pre>
 */
public class CookiesGeneratedCsrfTokenRepository implements CsrfTokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(CookiesGeneratedCsrfTokenRepository.class);

    private static final String HEADER_NAME = "X-CSRF-TOKEN";
    private static final String PARAMETER_NAME = "_csrf";

    private String salt;
    private List<String> cookieNames = new ArrayList<>();

    /**
     * Add the name of the cookie to use its value.
     *
     * @param cookieName
     *            the name of the cookie
     * @return this
     */
    public CookiesGeneratedCsrfTokenRepository addCookieName(String cookieName) {
        cookieNames.add(cookieName);
        return this;
    }

    /**
     * Add all the name of the cookies to use their values.
     *
     * @param cookieNames
     *            the name of the cookies
     * @return this
     */
    public CookiesGeneratedCsrfTokenRepository addCookieNames(String... cookieNames) {
        for (String cookieName : cookieNames) {
            this.cookieNames.add(cookieName);
        }
        return this;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        AssertTools.assertNotNull(salt, "You must set the salt");
        AssertTools.assertFalse(cookieNames.isEmpty(), "You must set at least one cookie");

        // Search all the cookies
        Map<String, String> valuesByName = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieNames.contains(cookie.getName())) {
                    String previousValue = valuesByName.put(cookie.getName(), cookie.getValue());
                    if (previousValue != null) {
                        throw new SmallToolsException("The cookie with name " + cookie.getName() + " contains more than one value");
                    }
                }
            }
        }

        // Generate the token
        StringBuilder allValues = new StringBuilder(salt);
        for (String cookieName : cookieNames) {
            String cookieValue = valuesByName.get(cookieName);
            logger.debug("Adding cookie {} with value {}", cookieName, cookieValue);
            allValues.append(cookieName).append(cookieValue);
        }

        String token = HashSha256.hashString(allValues.toString());
        logger.debug("Token is {}", token);
        return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, token);
    }

    public List<String> getCookieNames() {
        return cookieNames;
    }

    public String getSalt() {
        return salt;
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
    }

    /**
     * Set the names of the cookie to use their values.
     *
     * @param cookieNames
     *            the name of the cookies
     * @return this
     */
    public CookiesGeneratedCsrfTokenRepository setCookieNames(List<String> cookieNames) {
        this.cookieNames = cookieNames;
        return this;
    }

    /**
     * Set a unique salt for your application (to make sure that no one can generate the token by himself).
     *
     * @param salt
     *            the salt
     * @return this
     */
    public CookiesGeneratedCsrfTokenRepository setSalt(String salt) {
        this.salt = salt;
        return this;
    }

}
