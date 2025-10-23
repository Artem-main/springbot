package springbot.mybotg.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {

    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "send_user_text", nullable = false)
    private String sendUserText;

    @Column(name = "account_status")
    private String accountStatus;


    public Account(long id, String name, String sendUserText, String accountStatus) {
        this.id = id;
        this.name = name;
        this.sendUserText = sendUserText;
        this.accountStatus = accountStatus;
    }

    public Account(Long id, String name, String sendUserText) {
        this.id = id;
        this.name = name;
        this.sendUserText = sendUserText;
    }

    public Account() {
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sendUserText='" + sendUserText + '\'' +
                '}';
    }
}
