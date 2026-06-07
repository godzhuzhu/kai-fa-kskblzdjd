package cn.edu.whut.sept.zuul.game.user.controller;

import cn.edu.whut.sept.zuul.game.user.Result;
import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.service.IUserService;
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
}
