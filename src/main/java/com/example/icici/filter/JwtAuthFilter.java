package com.example.icici.filter;

//import com.example.hdfc.service.TokenService;

import com.example.icici.service.IciciService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final IciciService iciciService;

    public JwtAuthFilter(IciciService iciciService) {
        this.iciciService = iciciService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request);

        String path = wrappedRequest.getRequestURI();

        System.out.println("Entered Into filter : "+path);

        if (path.startsWith("/health") ||
                path.startsWith("/internal/icici/npci/register-public-key") ||
                path.startsWith("/api/bank/account/create")) {
            filterChain.doFilter(wrappedRequest, response);
            System.out.println("Passed through filter : "+path);
            return;
        }

        String base64Signature = wrappedRequest.getHeader("X-SIGNATURE");
        String npciId = wrappedRequest.getHeader("X-NPCI-ID");
        if(npciId==null || base64Signature==null){
            System.out.println("npci id is null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        wrappedRequest.getInputStream().readAllBytes();
//        byte[] body = wrappedRequest.getCachedBody();

        String payload = new String(
                wrappedRequest.getCachedBody(),
                StandardCharsets.UTF_8
        );

        String public_key;
        try {
            public_key = iciciService.loadPublicKeyFromdb(npciId);
        } catch (Exception e) {
            System.out.println("NPCI ID not registered in ICICI: " + npciId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        boolean valid;
        try {
            PublicKey publicKey = iciciService.convertToPublicKey(public_key);
            valid = iciciService.verify(payload, base64Signature, publicKey);
        } catch (Exception e) {
            System.out.println("Signature verification error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        System.out.println("Valid : "+valid);

        if(!valid){
            System.out.println("request rejected by icici bank");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        npciId,        // principal (NPCI identity)
                        null,         // no credentials
                        List.of()     // no roles (or add ROLE_NPCI)
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(wrappedRequest, response);
        System.out.println("Completed Filter : "+path);
    }

    private String extractToken(HttpServletRequest request) {
        // Prefer Token in Cookie (HttpOnly Cookie Strategy)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("token")) {
                    return c.getValue();
                }
            }
        }

        // fallback if token passed in header (Postman or mobile app)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
