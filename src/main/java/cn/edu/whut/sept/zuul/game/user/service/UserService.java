package cn.edu.whut.sept.zuul.game.user.service;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.game.Player;
import cn.edu.whut.sept.zuul.game.user.dto.ChangeNameDTO;
import cn.edu.whut.sept.zuul.game.user.dto.ChangePasswordDTO;
import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.entity.User;
import cn.edu.whut.sept.zuul.game.user.repository.UserRepository;
import cn.edu.whut.sept.zuul.game.user.security.JwtUtil;
import cn.edu.whut.sept.zuul.game.user.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Game game;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, Game game) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.game = game;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
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

    @Override
    public void changePassword(int userId, ChangePasswordDTO dto) {
        User user = userRepository.findById((long) userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void changeName(int userId, ChangeNameDTO dto) {
        if (dto.getPlayerName() == null || dto.getPlayerName().trim().isEmpty()) {
            throw new RuntimeException("Player name cannot be empty");
        }

        Player player = game.getOrCreatePlayer(userId);
        player.setPlayerName(dto.getPlayerName().trim());
    }

    @Override
    public void resetGame(int userId) {
        Player player = game.getOrCreatePlayer(userId);

        // Clear bag
        player.getBag().clear();

        // Reset stats
        player.setAttack(10);
        player.setDefense(5);
        player.setCurrentHealth(player.getMaxHealth());

        // Clear movement history and move to start
        player.getPreviousRooms().clear();
        player.moveTo(game.getStartingRoom());
        player.getPreviousRooms().clear();
    }
}
