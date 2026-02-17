package com.github.diogocerqueiralima.presentation.filters;

import com.github.diogocerqueiralima.application.commands.MarkBootstrapCertificateAsUsedCommand;
import com.github.diogocerqueiralima.application.exceptions.CertificateNotFoundException;
import com.github.diogocerqueiralima.domain.exceptions.BootstrapCertificateUsedException;
import com.github.diogocerqueiralima.domain.exceptions.CertificateRevokedException;
import com.github.diogocerqueiralima.domain.ports.inbound.BootstrapCertificateUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *     Filter that processes client certificate information from HTTP headers
 *     and performs authentication based on certificates.
 * </p>
 * <p>
 *     NGINX should validate the client certificate and extract the
 *     Common Name (CN) and Serial Number, passing them as headers.
 * </p>
 * <p>
 *     If the certificate is a valid bootstrap certificate, it is marked as used,
 *     and the user is authenticated with the CN as the principal.
 *     If the certificate is a revoked or already used bootstrap certificate,
 *     an unauthorized error is returned.
 * </p>
 * <p>
 *     If the certificate is not a bootstrap certificate, the user is authenticated.
 * </p>
 * <p>
 *     Finally, if no certificate headers are found, the request proceeds without authentication.
 * </p>
 */
@Component
public class CertificateFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CertificateFilter.class);
    private static final String CERTIFICATE_CN_HEADER = "X-Client-Cert-CN";
    private static final String CERTIFICATE_SERIAL_HEADER = "X-Client-Cert-Serial";

    private final BootstrapCertificateUseCase bootstrapCertificateUseCase;

    public CertificateFilter(BootstrapCertificateUseCase bootstrapCertificateUseCase) {
        this.bootstrapCertificateUseCase = bootstrapCertificateUseCase;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract certificate information from headers

        String commonName = request.getHeader(CERTIFICATE_CN_HEADER);
        String serialNumber = request.getHeader(CERTIFICATE_SERIAL_HEADER);

        if (commonName != null && serialNumber != null) {

            try {

                // 2. Attempt to mark bootstrap certificate as used

                bootstrapCertificateUseCase.markAsUsed(new MarkBootstrapCertificateAsUsedCommand(serialNumber));

            } catch (CertificateNotFoundException e) {

                // 2.1 Certificate is not a bootstrap certificate, proceed with authentication

                log.info("The certificate is not a bootstrap certificate: {}", serialNumber);
            } catch (CertificateRevokedException | BootstrapCertificateUsedException e) {

                // 2.2 Certificate is revoked or already used, reject the request

                log.warn("Bootstrap certificate authentication failed", e);
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Certificate authentication failed: " + e.getMessage()
                );

                return;
            }

            // 3. Authenticate the user with the certificate's Common Name

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    commonName,
                    null,
                    List.of()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. No certificate headers found, proceed without authentication

        log.info("No client certificate headers found, proceeding without certificate authentication.");
        filterChain.doFilter(request, response);
    }

}
