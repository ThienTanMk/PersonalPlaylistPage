package com.example.Playlist.configuration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.Playlist.entity.WebUser;
import com.example.Playlist.repository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService{

    @Autowired private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        WebUser webUser = userRepository.findByEmail(username);
        if(webUser==null) throw new UsernameNotFoundException("Not found");
        else {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("user"));
            return new User(webUser.getEmail(), webUser.getPassword(), authorities);
        }
    }
}
