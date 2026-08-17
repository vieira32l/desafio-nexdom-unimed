package com.nexdom.beneficiarios.auth;

import com.nexdom.beneficiarios.auth.dto.LoginRequest;
import com.nexdom.beneficiarios.auth.dto.LoginResponse;
import com.nexdom.beneficiarios.auth.dto.MeResponse;
import com.nexdom.beneficiarios.security.AuthenticatedUser;
import com.nexdom.beneficiarios.security.JwtService;
import com.nexdom.beneficiarios.tenant.TenantContext;
import com.nexdom.beneficiarios.usuario.Usuario;
import com.nexdom.beneficiarios.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLogin(request.login())
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos"));

        return new LoginResponse(jwtService.generateToken(usuario));
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return new MeResponse(user.login(), user.tenantId(), user.tenantNome(), TenantContext.get());
    }

}
