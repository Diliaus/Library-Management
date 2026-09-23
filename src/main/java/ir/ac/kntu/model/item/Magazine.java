package ir.ac.kntu.model.item;

import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;

public class Magazine extends PhysicalItem {

    private String issn;
    private String cycle;

    public Magazine(String title, int publishYear, String category,
                    int availableCopies, String issn, String cycle) {
        super(title, publishYear, category, availableCopies);
        setIssn(issn);
        setCycle(cycle);
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        if (!Validator.isValidISSN(issn)) {
            throw new InvalidInputException("Invalid ISSN format");
        }
        this.issn = issn.trim();
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        if (cycle == null || cycle.trim().isEmpty()) {
            throw new InvalidInputException("Cycle cannot be empty");
        }
        this.cycle = cycle.trim();
    }

    @Override
    public boolean isReservable() {
        return true;
    }

    @Override
    public String getItemType() {
        return "MAGAZINE";
    }

    @Override
    protected String getItemTypePrefix() {
        return "MAG";
    }

    @Override
    public String toString() {
        return "Type: MAGAZINE | ID: " + getId() + " | Title: " + getTitle() +
                " | ISSN: " + issn + " | Cycle: " + cycle +
                " | Year: " + getPublishYear() + " | Category: " + getCategory() +
                " | Copies: " + getAvailableCopies();
    }
}