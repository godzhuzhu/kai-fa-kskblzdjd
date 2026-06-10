package cn.edu.whut.sept.zuul.game.user.controller;

import cn.edu.whut.sept.zuul.game.user.Result;
import cn.edu.whut.sept.zuul.game.user.dto.ChangeNameDTO;
import cn.edu.whut.sept.zuul.game.user.dto.ChangePasswordDTO;
import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        try {
            return Result.ok(userService.login(dto));
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        try {
            userService.register(dto);
            return Result.ok("Registration successful");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.fail("Not logged in");
            }
            userService.changePassword(userId, dto);
            return Result.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/change-name")
    public Result<?> changeName(@RequestBody ChangeNameDTO dto, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.fail("Not logged in");
            }
            userService.changeName(userId, dto);
            return Result.ok("Name changed successfully");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/reset-game")
    public Result<?> resetGame(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.fail("Not logged in");
            }
            userService.resetGame(userId);
            return Result.ok("Game reset successfully");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
