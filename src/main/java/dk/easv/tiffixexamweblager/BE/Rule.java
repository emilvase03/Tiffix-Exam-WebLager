package dk.easv.tiffixexamweblager.BE;

public class Rule {
    private int id;
    private String Name;
    private int amount;

    public Rule(int id, String name, int amount) {
        setId(id);
        setName(name);
        setAmount(amount);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    private void setName(String name) {
        Name = name;
    }

    public int getAmount() {
        return amount;
    }

    private void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Rule rule))
            return false;
        return this.getId() == rule.getId();
    }
}
