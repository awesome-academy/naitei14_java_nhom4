package com.group4.expense_manager.config;

import com.group4.expense_manager.service.CustomUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final CustomUserDetailsService userDetailsService;

    public CustomJwtAuthenticationConverter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // subject của token chính là email (lúc bạn createToken đã set .subject(authentication.getName()))
        String email = jwt.getSubject();

        // Load User từ DB
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Trả về Authentication với principal = UserDetails (chính là entity User của bạn)
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "N/A",
                userDetails.getAuthorities()
        );
    }
}
