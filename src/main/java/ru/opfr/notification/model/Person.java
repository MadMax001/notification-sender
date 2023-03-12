package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class Person {
    private String user;
    private String ip;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(user, person.user) && Objects.equals(ip, person.ip) && Objects.equals(email, person.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, ip, email);
    }
}
