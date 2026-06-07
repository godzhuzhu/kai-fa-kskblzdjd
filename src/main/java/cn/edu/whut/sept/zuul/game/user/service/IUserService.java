package cn.edu.whut.sept.zuul.game.user.service;

import cn.edu.whut.sept.zuul.game.user.dto.LoginDTO;
import cn.edu.whut.sept.zuul.game.user.dto.RegisterDTO;
import cn.edu.whut.sept.zuul.game.user.vo.LoginVO;

public interface IUserService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);
}
