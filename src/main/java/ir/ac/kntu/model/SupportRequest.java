package ir.ac.kntu.model;

import ir.ac.kntu.util.IdGenerator;

public class SupportRequest {
    private String id;
    private final String userEmail;
    private final RequestSection section;
    private final String message;
    private String response;
    private String status;

    public SupportRequest(String userEmail, RequestSection section, String message) {
        this.id = IdGenerator.generateMemberId("REQ");
        this.userEmail = userEmail;
        this.section = section;
        this.message = message;
        this.status = "OPEN";
        this.response = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public RequestSection getSection() {
        return section;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String base = "[" + id + "] Section: " + section + " | Status: " + status + "\nMsg: " + message;
        if (response != null && !response.isEmpty()) {
            base += "\nResponse: " + response;
        }
        return base;
    }
}