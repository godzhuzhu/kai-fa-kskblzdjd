package cn.edu.whut.sept.zuul.game.user.service;

import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.entity.User;
import cn.edu.whut.sept.zuul.game.user.repository.UserRepository;
import cn.edu.whut.sept.zuul.game.user.security.JwtUtil;
import cn.edu.whut.sept.zuul.game.user.vo.LoginVO;
import cn.edu.whut.sept.zuul.game.websocket.GameWebSocketHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GameWebSocketHandler webSocketHandler;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       @Lazy GameWebSocketHandler webSocketHandler) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (webSocketHandler.isPlayerOnline(user.getId().intValue())) {
            throw new RuntimeException("该账号已在其他地方登录");
        }

        String token = jwtUtil.generateToken(user.getId().intValue());
        return new LoginVO(user.getId().intValue(), token);
    }

    @Override
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(dto.getUsername(), passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }
}
