import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public final String personalImagePath;

    public Personal_ID(String pIDnumber, String pPublicProfile, String pName, String pSurname, Date pBirthDate, String pAddress, String pPersonalImagePath) {
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
        personalImagePath = pPersonalImagePath;
    }

    public Personal_ID(String[] attributes) throws Exception {
        if(attributes.length != 9) {
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
        personalImagePath = attributes[8];
    }

    public byte[] toByte(boolean withPaths) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ID_number.getBytes());
        baos.write('\n');
        baos.write(publicProfile.getBytes());
        baos.write('\n');
        baos.write(name.getBytes());
        baos.write('\n');
        baos.write(surname.getBytes());
        baos.write('\n');
        baos.write(Integer.toString(birthdate_day).getBytes());
        baos.write('\n');
        baos.write(Integer.toString(birthdate_month).getBytes());
        baos.write('\n');
        baos.write(Integer.toString(birthdate_year).getBytes());
        baos.write('\n');
        baos.write(address.getBytes());
        baos.write('\n');
        if(withPaths) {
            baos.write(personalImagePath.getBytes());
            baos.write('\n');
        }
        return baos.toByteArray();
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
        sb.append("\nPfad Passbild:\n");
        sb.append(personalImagePath);
        sb.append('\n');
        return sb.toString();
    }
}
