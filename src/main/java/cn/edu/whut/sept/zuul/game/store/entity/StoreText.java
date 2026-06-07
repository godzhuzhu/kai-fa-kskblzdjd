package cn.edu.whut.sept.zuul.game.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "store_text")
public class StoreText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String data;

    public StoreText() {
    }

    public StoreText(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
