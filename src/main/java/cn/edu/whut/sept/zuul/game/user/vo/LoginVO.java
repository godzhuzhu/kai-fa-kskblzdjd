package cn.edu.whut.sept.zuul.game.user.vo;

public class LoginVO {

    private int userId;
    private String token;

    public LoginVO() {
    }

    public LoginVO(int userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
