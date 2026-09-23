package ir.ac.kntu.services;

import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.RequestSection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportRequestHelper {
    private final List<SupportRequest> supportRequests;

    public SupportRequestHelper(List<SupportRequest> supportRequests) {
        this.supportRequests = supportRequests;
    }

    public void addRequest(SupportRequest request) {
        if (request == null) {
            throw new InvalidInputException("Request cannot be null");
        }
        supportRequests.add(request);
    }

    public List<SupportRequest> getRequests() {
        return Collections.unmodifiableList(supportRequests);
    }

    public List<SupportRequest> getRequestsBySection(RequestSection section) {
        List<SupportRequest> filtered = new ArrayList<>();
        for (SupportRequest req : supportRequests) {
            if (req.getSection() == section) {
                filtered.add(req);
            }
        }
        return filtered;
    }
}