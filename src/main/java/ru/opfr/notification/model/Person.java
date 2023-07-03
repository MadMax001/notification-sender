package ru.opfr.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

import static ru.opfr.notification.ValidationMessages.*;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class Person {
    @Size(max=255, message = MAX_LENGTH_USER)
    private String user;
    @Pattern(regexp = "^$|(^10\\.73\\.(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$)", message = WRONG_IP)
    private String ip;
    @Size(max=255, message = MAX_LENGTH_EMAIL)
    @Pattern(regexp = "^$|(^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$)", message = WRONG_EMAIL)
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

    @Override
    public String toString() {
        return "Person{" +
                "user='" + user + '\'' +
                ", ip='" + ip + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
