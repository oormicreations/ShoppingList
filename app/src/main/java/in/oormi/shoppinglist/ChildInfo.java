package in.oormi.shoppinglist;

public class ChildInfo {

    private int sequence = 0;
    private String quantity = "";
    private String cost = "";
    private String notes = "";
    boolean hasError = false;
    boolean isNew = false;


    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String q) {
        this.quantity = q;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String c) {
        this.cost = c;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String n) {
        this.notes = n;
    }

}