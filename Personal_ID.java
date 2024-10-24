import java.util.Date;

public class Personal_ID {
    public final String ID_number;
    public final String name;
    public final String surname;
    public final int birthdate_day;
    public final int birthdate_month;
    public final int birthdate_year;
    public final String address;

    public Personal_ID(String pIDnumber, String pName, String pSurname, Date pBirthDate, String pAddress) {
        ID_number = pIDnumber;
        name = pName;
        surname = pSurname;
        birthdate_day = pBirthDate.getDay();
        birthdate_month = pBirthDate.getMonth();
        birthdate_year = pBirthDate.getYear();
        address = pAddress;
    }

    public byte[] toByte() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID_number);
        sb.append('\n');
        sb.append(name);
        sb.append('\n');
        sb.append(surname);
        sb.append('\n');
        sb.append(birthdate_day);
        sb.append('\n');
        sb.append(birthdate_month);
        sb.append('\n');
        sb.append(birthdate_year);
        sb.append('\n');
        sb.append(address);
        sb.append('\n');
        return sb.toString().getBytes();
    }
}
