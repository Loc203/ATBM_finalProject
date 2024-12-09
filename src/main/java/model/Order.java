package model;

public class Order {
    int id;
    int account_id;

    String key;

    public Order(int id, int account_id) {
        this.id = id;
        this.account_id = account_id;
    }

    public Order(int id, int account_id, String key) {
        this.id = id;
        this.account_id = account_id;
        this.key = key;
    }

    public Order() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", account_id=" + account_id +
                '}';
    }
}