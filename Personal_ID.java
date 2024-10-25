import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Personal_ID {
    public final String ID_number;
    public final String publicProfile;
    public final String name;
    public final String surname;
    public final int birthdate_day;
    public final int birthdate_month;
    public final int birthdate_year;
    public final String address;

    public Personal_ID(String pIDnumber, String pPublicProfile, String pName, String pSurname, Date pBirthDate, String pAddress) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(pBirthDate);
        ID_number = pIDnumber;
        publicProfile = pPublicProfile;
        name = pName;
        surname = pSurname;
        birthdate_day = calendar.get(Calendar.DAY_OF_MONTH);
        birthdate_month = calendar.get(Calendar.MONTH) + 1;
        birthdate_year = calendar.get(Calendar.YEAR);
        address = pAddress;
    }

    public Personal_ID(String[] attributes) throws Exception {
        if(attributes.length != 8) {
            throw new Exception("number of attributes not suitable");
        }
        ID_number = attributes[0];
        publicProfile = attributes[1];
        name = attributes[2];
        surname = attributes[3];
        birthdate_day = Integer.parseInt(attributes[4]);
        birthdate_month = Integer.parseInt(attributes[5]);
        birthdate_year = Integer.parseInt(attributes[6]);
        address = attributes[7];
    }

    public byte[] toByte() {
        StringBuilder sb = new StringBuilder();
        sb.append(ID_number);
        sb.append('\n');
        sb.append(publicProfile);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ausweisnummer:\n");
        sb.append(ID_number);
        sb.append("\nÖffentliches Profil:\n");
        sb.append(publicProfile);
        sb.append("\nVorname:\n");
        sb.append(name);
        sb.append("\nNachname:\n");
        sb.append(surname);
        sb.append("\nGeburtsdatum\n");
        sb.append(birthdate_day);
        sb.append(".");
        sb.append(birthdate_month);
        sb.append(".");
        sb.append(birthdate_year);
        sb.append("\nAdresse:\n");
        sb.append(address);
        sb.append('\n');
        return sb.toString();
    }
}
