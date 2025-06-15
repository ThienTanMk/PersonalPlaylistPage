package com.example.Playlist.configuration;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException{
        
        
        String path = request.getRequestURI();

        // Skip JWT check for public endpoints
        if (path.equals("/api/v1/users/register") || path.equals("/api/v1/users/login")||path.contains("/api/v1/tracks/images/")||path.contains("/api/v1/tracks/audios/")) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            System.out.println(request.getRequestURI());
            String jwt = getJwtFromRequest(request);
            if(!jwtTokenProvider.validateToken(jwt)){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden: Invalid or missing token\"}");
                return;
            }

            String username = jwtTokenProvider.getUserNameFromJWT(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if(userDetails!=null){
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
        }
        catch(Exception ex){
           return;
        }
        filterChain.doFilter(request, response);
    }
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem header Authorization có chứa thông tin jwt không
        if (bearerToken!=null &&bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
