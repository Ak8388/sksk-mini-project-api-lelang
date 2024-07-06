package jawa.sinaukoding.sk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jawa.sinaukoding.sk.entity.User;
import jawa.sinaukoding.sk.model.Authentication;
import jawa.sinaukoding.sk.util.JwtUtils;
import jawa.sinaukoding.sk.util.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class AuthenticationFilter extends OncePerRequestFilter {

    private final byte[] jwtKey;

    public AuthenticationFilter(byte[] jwtKey) {
        this.jwtKey = jwtKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                final String token = authorization.substring(7);
                JwtUtils.Jwt jwt = JwtUtils.hs256Parse(token, jwtKey); 
                ObjectMapper mapper = new ObjectMapper();
                Map map = mapper.readValue(jwt.payload(), Map.class);
                Number id = (Number) map.get("sub");
                User.Role role = User.Role.fromString((String) map.get("role"));
                Number iat = (Long) map.get("iat");
                Number exp = (Number) map.get("exp");
                long expiration = iat.longValue() + exp.longValue();
                if (System.currentTimeMillis() < expiration) {
                    final Authentication authentication = new Authentication(id.longValue(), role, true);
                    SecurityContextHolder.setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clear();
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
        SecurityContextHolder.clear();
    }
}
