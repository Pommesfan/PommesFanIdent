import java.util.Date;

public class Personal_ID {
    public final String ID_number;
    public final String name;
    public final String surname;
    public final int birthdate_day;
    public final int birthdate_month;
    public final int birthdate_year;
    public final String address;

    public Personal_ID(String idNumber, String name, String surname, Date birthDate, String address) {
        ID_number = idNumber;
        this.name = name;
        this.surname = surname;
        birthdate_day = birthDate.getDay();
        birthdate_month = birthDate.getMonth();
        birthdate_year = birthDate.getYear();
        this.address = address;
    }
}
