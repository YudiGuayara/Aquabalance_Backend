package com.AquaBalance.user.infrastructure.security;

import com.AquaBalance.user.application.ports.in.BuscarUsuarioUseCase;
import com.AquaBalance.user.domain.Usuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final BuscarUsuarioUseCase buscarUsuarioUseCase;

    public CustomUserDetailsService(BuscarUsuarioUseCase buscarUsuarioUseCase) {
        this.buscarUsuarioUseCase = buscarUsuarioUseCase;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario;
        try {
            usuario = buscarUsuarioUseCase.buscarPorEmail(email);
        } catch (Exception e) {
            throw new UsernameNotFoundException(
                    "Usuario no encontrado con email: " + email, e);
        }


        return new CustomUserDetails(usuario);
    }
}