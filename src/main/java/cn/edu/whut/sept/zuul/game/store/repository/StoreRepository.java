package cn.edu.whut.sept.zuul.game.store.repository;

import cn.edu.whut.sept.zuul.game.store.entity.StoreText;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreText, Long> {

    Optional<StoreText> findByName(String name);

    void deleteByName(String name);
}
