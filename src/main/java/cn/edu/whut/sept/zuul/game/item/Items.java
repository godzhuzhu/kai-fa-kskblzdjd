package cn.edu.whut.sept.zuul.game.item;

import java.util.Random;

/**
 * 物品工厂类 — 提供随机生成和按名创建物品的静态方法。
 *
 * @author gmy
 * @since 1.0
 */
public class Items {
    private static final Random RANDOM = new Random();

    @SuppressWarnings("unchecked")
    private static final Class<? extends AbstractItem>[] ITEM_TYPES = new Class[]{
            Sword.class,
            DragonscaleBulwark.class,
            StormCleaver.class,
            MagicCookie.class,
            BloodVial.class,
            StonehideElixir.class
    };

    /**
     * 从 6 种物品中随机生成一件。
     *
     * @return 随机物品实例
     */
    public static AbstractItem generateRandomItem() {
        try {
            int index = RANDOM.nextInt(ITEM_TYPES.length);
            return ITEM_TYPES[index].newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return new Sword();
        }
    }

    /**
     * 按名称创建物品。
     *
     * @param name 物品名称（大小写敏感）
     * @return 对应的物品实例，未知名称返回 null
     */
    public static AbstractItem createItem(String name) {
        switch (name) {
            case "Sword":
                return new Sword();
            case "DragonscaleBulwark":
                return new DragonscaleBulwark();
            case "StormCleaver":
                return new StormCleaver();
            case "MagicCookie":
                return new MagicCookie();
            case "BloodVial":
                return new BloodVial();
            case "StonehideElixir":
                return new StonehideElixir();
            default:
                return null;
        }
    }
}
