package ir.ac.kntu.model;

import ir.ac.kntu.exception.InvalidInputException;

public class SupportTicket {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    private RequestSection section;
    private String description;
    private String status;
    private String response;

    public SupportTicket(RequestSection section, String description) {
        setSection(section);
        setDescription(description);
        this.status = STATUS_OPEN;
        this.response = "";
    }

    public RequestSection getSection() {
        return section;
    }

    public void setSection(RequestSection section) {
        if (section == null) {
            throw new InvalidInputException("Ticket section cannot be null");
        }
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidInputException("Ticket description cannot be empty");
        }
        this.description = description.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null) {
            throw new InvalidInputException("Status cannot be null");
        }
        String upperStatus = status.toUpperCase().trim();
        if (!upperStatus.equals(STATUS_OPEN) && !upperStatus.equals(STATUS_CLOSED)) {
            throw new InvalidInputException("Invalid ticket status");
        }
        this.status = upperStatus;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new InvalidInputException("Response cannot be empty");
        }
        this.response = response.trim();
        this.status = STATUS_CLOSED;
    }
}