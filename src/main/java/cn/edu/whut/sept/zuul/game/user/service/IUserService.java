package cn.edu.whut.sept.zuul.game.user.service;

import cn.edu.whut.sept.zuul.game.user.dto.ChangeNameDTO;
import cn.edu.whut.sept.zuul.game.user.dto.ChangePasswordDTO;
import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.vo.LoginVO;

public interface IUserService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    void changePassword(int userId, ChangePasswordDTO dto);

    void changeName(int userId, ChangeNameDTO dto);

    void resetGame(int userId);
}
