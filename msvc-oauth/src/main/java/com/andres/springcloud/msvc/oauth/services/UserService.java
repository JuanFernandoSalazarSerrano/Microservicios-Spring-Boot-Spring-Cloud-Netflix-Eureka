package com.andres.springcloud.msvc.oauth.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.http.MediaType; // ✅ correct MediaType

import com.andres.springcloud.msvc.oauth.models.User;



@Service
public class UserService implements UserDetailsService {

    private WebClient.Builder client;

    public UserService(Builder client) {
        this.client = client;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        
        try {
            
            User user = client.build().get().uri("/username/{username}", params)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(User.class)
                            .block();

            List<GrantedAuthority> roles = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
             user.getPassword(),
              user.isEnabled(),
               true,
                true,
                 true,
                  roles);

        } catch (WebClientResponseException e) {
            throw new UsernameNotFoundException("Login error, user doesnt exist" + username + "in the system");
        }
    }

}
