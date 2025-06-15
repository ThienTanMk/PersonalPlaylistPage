package com.example.Playlist.configuration;

import java.util.Date;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
@Component  
public class JwtTokenProvider {
    private final String  secret_key = "phuong";
    private final long expiration = 604800000L;

    public String generateToken(String username){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512,secret_key)
                .compact();
        }
    public String getUserNameFromJWT(String token){
        Claims claims = Jwts.parser()
                            .setSigningKey(secret_key)
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }
    public boolean validateToken(String authToken){
        try{
            Jwts.parser().setSigningKey(secret_key).parseClaimsJws(authToken);
            return true;
        }
        catch (MalformedJwtException ex) {
           System.out.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
          System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty.");
        }
        return false;
    }
}
