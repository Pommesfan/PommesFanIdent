package model;

import controller.Controller;
import java.io.*;
import java.util.*;

import static controller.Controller.*;

public class AttachmentRelation {
    private final List<String[]> data; //
    public final String filePath;

    public AttachmentRelation(String filePath, List<String[]> data) {
        this.filePath = filePath;
        this.data = data;
    }

    public void insertRelation(String imageFileName, String originalFileName, String idNumber) {
        data.add(new String[]{imageFileName, originalFileName, idNumber});
    }

    public String getImageFileName(String idNumber) {
        for(String[] attributes: data) {
            if(attributes[2].equals(idNumber))
                return attributes[0];
        }
        throw new NoSuchElementException("idNumber not found");
    }

    public Set<String> getSameFileNames(String originalFileName) {
        TreeSet<String> res = new TreeSet<>();
        for(String[] attributes: data) {
            if(attributes[1].equals(originalFileName))
                res.add(attributes[0]);
        }
        return res;
    }

    public void save() throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        for (String[]arguments: data) {
            bw.write(arguments[0] + ":" + arguments[1] + ":" + arguments[2]);
            bw.newLine();
        }
        bw.close();
        osw.close();
        fos.close();
    }

    public String removeID(String idNumber) {
        for (String[]arguments: data) {
            if(arguments[2].equals(idNumber)) {
                data.remove(arguments);
                return arguments[0];
            }
        }
        throw new NoSuchElementException("no such id number");
    }

    public boolean hasImage(String imageID) {
        for (String[]arguments: data) {
            if(arguments[0].equals(imageID))
                return true;
        }
        return false;
    }

    public boolean hasID(String idNumber) {
        for (String[]arguments: data) {
            if(arguments[2].equals(idNumber))
                return true;
        }
        return false;
    }

    public static AttachmentRelation getRelation(Controller controller, int attachmentMode) throws IOException {
        String filePath;
        if(attachmentMode == ATTACHMENT_PERSONAL_IMAGE) {
            filePath = controller.appDataLocation + strPersonalImageRelations;
        } else if(attachmentMode == ATTACHMENT_HAND_SIGNATURE) {
            filePath = controller.appDataLocation + strHandSignaturesRelations;
        } else {
            throw new IllegalArgumentException("not such relation type");
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        List<String[]> data = new LinkedList<>();
        String line;
        while ((line = br.readLine()) != null)   {
            String[]arguments = line.split(":");
            if(arguments.length != 3)
                continue;
            data.add(arguments);
        }
        return new AttachmentRelation(filePath, data);
    }

    public static String attachmentPath(Controller controller, int attachmentMode) {
        if(attachmentMode == ATTACHMENT_PERSONAL_IMAGE)
            return controller.appDataLocation + strPersonalImages;
        else if(attachmentMode == ATTACHMENT_HAND_SIGNATURE)
            return controller.appDataLocation + strHandSignatures;
        else
            throw new IllegalArgumentException("not such relation type");
    }
}
