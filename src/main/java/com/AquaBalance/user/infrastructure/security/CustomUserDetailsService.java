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

        Usuario usuario = buscarUsuarioUseCase.buscarPorEmail(email);

        System.out.println("================================");
        System.out.println("EMAIL: " + usuario.getEmail());
        System.out.println("ROL: " + usuario.getRol());
        System.out.println("ACTIVO: " + usuario.isActivo());
        System.out.println("PASSWORD_BD: " + usuario.getPassword());
        System.out.println("================================");

        return new CustomUserDetails(usuario);
    }
}
