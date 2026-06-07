package cn.edu.whut.sept.zuul.game.store.service;

import cn.edu.whut.sept.zuul.game.store.entity.StoreText;
import cn.edu.whut.sept.zuul.game.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreService implements IStoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public void save(String name, String jsonString) {
        StoreText store = storeRepository.findByName(name).orElse(new StoreText());
        store.setName(name);
        store.setData(jsonString);
        storeRepository.save(store);
    }

    @Override
    public String load(String name) {
        return storeRepository.findByName(name)
                .map(StoreText::getData)
                .orElse(null);
    }
}
