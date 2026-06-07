package cn.edu.whut.sept.zuul.game.store.service;

public interface IStoreService {

    void save(String name, String jsonString);

    String load(String name);
}
