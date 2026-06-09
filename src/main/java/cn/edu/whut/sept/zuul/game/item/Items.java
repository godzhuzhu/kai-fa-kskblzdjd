package cn.edu.whut.sept.zuul.game.item;

import java.util.Random;

public class Items {
    private static final Random RANDOM = new Random();

    @SuppressWarnings("unchecked")
    private static final Class<? extends AbstractItem>[] ITEM_TYPES = new Class[]{
            Sword.class,
            DragonscaleBulwark.class,
            StormCleaver.class,
            FrostBow.class,
            WarHammer.class,
            VampireFang.class,
            ThornArmor.class,
            SpeedBoots.class,
            MagicCookie.class,
            BloodVial.class,
            HealthPotion.class,
            StonehideElixir.class,
            BerserkerTotem.class,
            BloodDagger.class,
            ImmortalCore.class,
            PhoenixFeather.class,
            ShadowbaneBallista.class
    };

    /** 稀有物品权重映射 (物品名 -> 0-100稀有度, 越低越常见) */
    private static final java.util.Map<String, Integer> RARITY = new java.util.HashMap<>();
    static {
        RARITY.put("Sword", 10);
        RARITY.put("DragonscaleBulwark", 15);
        RARITY.put("StormCleaver", 30);
        RARITY.put("FrostBow", 35);
        RARITY.put("WarHammer", 40);
        RARITY.put("VampireFang", 45);
        RARITY.put("ThornArmor", 50);
        RARITY.put("SpeedBoots", 25);
        RARITY.put("MagicCookie", 20);
        RARITY.put("BloodVial", 10);
        RARITY.put("HealthPotion", 15);
        RARITY.put("StonehideElixir", 20);
        RARITY.put("BerserkerTotem", 55);
        RARITY.put("BloodDagger", 40);
        RARITY.put("ImmortalCore", 60);
        RARITY.put("PhoenixFeather", 65);
        RARITY.put("ShadowbaneBallista", 50);
    }

    public static AbstractItem generateRandomItem() {
        try {
            int index = RANDOM.nextInt(ITEM_TYPES.length);
            return ITEM_TYPES[index].getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new Sword();
        }
    }

    public static AbstractItem createItem(String name) {
        switch (name) {
            case "Sword": return new Sword();
            case "DragonscaleBulwark": return new DragonscaleBulwark();
            case "StormCleaver": return new StormCleaver();
            case "FrostBow": return new FrostBow();
            case "WarHammer": return new WarHammer();
            case "VampireFang": return new VampireFang();
            case "ThornArmor": return new ThornArmor();
            case "SpeedBoots": return new SpeedBoots();
            case "MagicCookie": return new MagicCookie();
            case "BloodVial": return new BloodVial();
            case "HealthPotion": return new HealthPotion();
            case "StonehideElixir": return new StonehideElixir();
            case "BerserkerTotem": return new BerserkerTotem();
            case "BloodDagger": return new BloodDagger();
            case "ImmortalCore": return new ImmortalCore();
            case "PhoenixFeather": return new PhoenixFeather();
            case "ShadowbaneBallista": return new ShadowbaneBallista();
            default: return null;
        }
    }

    public static int getRarity(String itemName) {
        return RARITY.getOrDefault(itemName, 30);
    }
}
