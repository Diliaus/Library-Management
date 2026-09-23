package ir.ac.kntu.model.user;

import ir.ac.kntu.util.IdGenerator;

public class Professor extends RegularUser {

    public Professor(String password, String firstName, String lastName, String email, String phone) {
        super(firstName, lastName, password, email, phone, IdGenerator.generateMemberId("FAC"));
    }
}