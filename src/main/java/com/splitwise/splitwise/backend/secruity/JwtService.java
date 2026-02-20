package com.splitwise.splitwise.backend.secruity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private final String SECRET="mysupersecretkeymysupersecretkey12345";

    private Key getSignInKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email){

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000 *60)) //1 hour
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public  String extracyEmail(String token){

        Claims claims= Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
