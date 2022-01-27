package com.gui;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public class mainForm {
    private JPanel panel1;
    private JButton btConvert;
    private JComboBox<String> cmbUploadType;
    private JTextField tfDebitAccount;
    private JTextField tfAccountDescription;
    private JComboBox cmbInstructionAt;
    private JDateChooser fieldIntrustionDate;
    private JComboBox cmbChargeTo;
    private JComboBox cmbChargeType;
    private JLabel logo;
    private JLabel dcc;
    private static JFrame parent;

    private static Properties prop;

    private static SimpleDateFormat format;

    private static String debitAccount;
    private static String descriptionAccount;
    private static String instructionAt="";
    private static String instructionDate="";
    private static String chargeType;
    private static String chargeTo;

    private static String productCode;

    private static Long startTime;
    private static DecimalFormat df2 = new DecimalFormat("###.###");

    public static void main(String[] args){
        prop= new Properties();
        String properties= "app.config";

        InputStream config = null;
        try {
            config = new FileInputStream(properties);
            prop.load(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!checkConfigFile()){
            System.exit(0);
        }

        deleteFileOut(new File(prop.getProperty("app.folder_out")));
        showGUI();
    }

    private static boolean checkConfigFile(){
        File folderIN= new File(prop.getProperty("app.folder_in"));
        File folderOUT= new File(prop.getProperty("app.folder_out"));

        boolean status= true;

        if (!folderIN.exists()){
            JOptionPane.showMessageDialog(parent, "Check config file\nfolder in not found ");
            status= false;
        }

        if(!folderOUT.exists()){
            JOptionPane.showMessageDialog(parent, "Check config file\nfolder out not found");
            status= false;
        }

//        System.out.println(prop.getProperty("app.instruction_at").length());
        if(prop.getProperty("app.debit_account_no").length()>12){
            JOptionPane.showMessageDialog(parent, "Check config file\nCredit Account No couldn't be more than 34");
            status= false;
        }

        if(prop.getProperty("app.account_description").length()>60){
            JOptionPane.showMessageDialog(parent, "Check config file\nCredit Account No couldn't be more than 50");
            status= false;
        }

//        if(prop.getProperty("app.credit_account_currency_code").length()>3 ||
//                prop.getProperty("app.credit_account_currency_code").length()<3){
//            JOptionPane.showMessageDialog(parent, "Check config file\nCredit Account No couldn't be more or less than 3");
//            status= false;
//        }

        if(prop.getProperty("app.instruction_at").length()>4 ||
                prop.getProperty("app.instruction_at").length()<4){
            JOptionPane.showMessageDialog(parent, "Check config file\nInstruction at couldn't be more or less than 4");
            status= false;
        }

        return status;
    }

    private static void deleteFileOut(File file) {
        for(File del: Objects.requireNonNull(file.listFiles())){
            del.delete();
        }
    }

    private void createUIComponents() {
        fieldIntrustionDate = new JDateChooser();
    }

    private mainForm(){
        parent = new JFrame();

        logo.setIcon(new ImageIcon(ClassLoader.getSystemResource("res/danamon.jpg")));
        logo.validate();

        dcc.setIcon(new ImageIcon(ClassLoader.getSystemResource("res/dcc.png")));
        dcc.validate();

        /**
         * LAC= SKN
         * RAC= RTGS
         * */
        cmbUploadType.addItem("SKN");
        cmbUploadType.addItem("RTGS");
        String uploadType=prop.getProperty("app.bulk_upload");
        if(checkingUploadTypeConfig(uploadType)!=null){
            cmbUploadType.setSelectedItem(checkingUploadTypeConfig(uploadType));
        }else{
            JOptionPane.showMessageDialog(parent, "Upload type not match.\n Please set correct upload type in config file.");
            System.exit(1);
        }

        /**
         * OUR= REM
         * BEN= BEN
         * */
        cmbChargeTo.addItem("OUR");
        cmbChargeTo.addItem("BEN");
        String chargeToTemp=prop.getProperty("app.charge_to");
        if (checkingChargeToConfig(chargeToTemp)!=null){
            cmbChargeTo.setSelectedItem(checkingChargeToConfig(chargeToTemp));
        }else{
            JOptionPane.showMessageDialog(parent, "Charge To not match.\n Please set correct upload type in config file.");
            System.exit(1);
        }

        cmbChargeType.addItem("Split");
        cmbChargeType.addItem("Combine");
        String chargeTypeTemp=prop.getProperty("app.charge_type");
        if (checkingChargeTypeConfig(chargeTypeTemp)!=null){
            cmbChargeType.setSelectedItem(checkingChargeTypeConfig(chargeTypeTemp));
        }else{
            JOptionPane.showMessageDialog(parent, "Charge type not match.\n Please set correct upload type in config file.");
            System.exit(1);
        }

        tfDebitAccount.setText(prop.getProperty("app.debit_account_no"));
        tfDebitAccount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String value = tfDebitAccount.getText();
                int l = value.length();
                if (e.getKeyChar() >= '0' && e.getKeyChar() <= '9' || e.getKeyCode()==8) {
                    tfDebitAccount.setEditable(true);
                } else {
                    tfDebitAccount.setEditable(false);
                }

                if (l<12 || e.getKeyCode()==8){
                    tfDebitAccount.setEditable(true);
                }else{
                    tfDebitAccount.setEditable(false);
                }
            }
        });

        tfAccountDescription.setText(prop.getProperty("app.account_description"));
        tfAccountDescription.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String value = tfAccountDescription.getText();
                int l = value.length();
                if (l<=60 || e.getKeyCode()==8){
                    tfAccountDescription.setEditable(true);
                }else{
                    tfAccountDescription.setEditable(false);
                }
            }
        });

        fieldIntrustionDate.setDateFormatString("yyyy/MM/dd");
        JTextFieldDateEditor editor = (JTextFieldDateEditor) fieldIntrustionDate.getDateEditor();
        editor.setEditable(false);
        format = new SimpleDateFormat("yyyyMMdd");

        cmbInstructionAt.addItem("0700");
        cmbInstructionAt.addItem("1000");
        cmbInstructionAt.addItem("1300");
        cmbInstructionAt.setSelectedItem(prop.getProperty("app.instruction_at"));

        btConvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btConvert.setEnabled(false);

                boolean status = true;
                debitAccount= tfDebitAccount.getText();
                if (debitAccount.equals("")){
                    status= false;
                    JOptionPane.showMessageDialog(parent, "Please fill debit account");
                }
                descriptionAccount= tfAccountDescription.getText();
                instructionAt = (String) cmbInstructionAt.getSelectedItem();
                if(fieldIntrustionDate.getDate()!=null) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
                    DateTimeFormatter dtfTime= DateTimeFormat.forPattern("HHmm");
                    LocalDateTime now = LocalDateTime.now();
                    String timeNow= now.toString(dtfTime);
                    if (format.format(fieldIntrustionDate.getDate()).compareTo(now.toString(dtf))==0){
                        if (timeNow.compareTo(instructionAt)>0){
                            status= false;
                            JOptionPane.showMessageDialog(parent, "Instruction At (set in configuration file) has been passed");
                        }
                    }
                    if (format.format(fieldIntrustionDate.getDate()).compareTo(now.toString(dtf))<0){
                        status= false;
                        JOptionPane.showMessageDialog(parent, "Instruction Date must be empty or equal or greater than today.");
                    }
                    instructionDate = format.format(fieldIntrustionDate.getDate());
                }else{
                    instructionAt="";
                }

                productCode= convertUploadType(cmbUploadType.getSelectedItem());

                chargeType= convertChargeType(cmbChargeType.getSelectedItem());

                chargeTo= convertChargeTo(cmbChargeTo.getSelectedItem());
                
                if (status) {
                    convert();
                }
                btConvert.setEnabled(true);
            }
        });
    }

    private static String checkingChargeTypeConfig(String property) {
        if (property.equals("S")){
            return "Split";
        }else if(property.equals("C")){
            return "Combine";
        }
        return null;
    }
    private static String checkingUploadTypeConfig(String property){
        if (property.equals("RTGS")){
            return "RTGS";
        }else if (property.equals("SKN")){
            return "SKN";
        }
        return null;
    }
    private static String checkingChargeToConfig(String property){
        if (property.equals("OUR")){
            return "REM";
        }else if(property.equals("BEN")){
            return "BEN";
        }
        return null;
    }

    private static String convertChargeTo(Object selectedItem){
        if (selectedItem.equals("OUR")){
            return "REM";
        }
        return "BEN";

    }
    private static String convertChargeType(Object selectedItem){
        if (selectedItem.equals("Split")){
            return "S";
        }
        return "C";

    }
    private static String convertUploadType(Object selectedItem){
        if (selectedItem.equals("RTGS")){
            return "RAC";
        }
        return "LAC";

    }

    private static void showGUI(){
        JFrame frame = new JFrame("Converter Auto-Credit LLG-RTGS");//manggilframe
        frame.setContentPane(new mainForm().panel1);//manggil guiform
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//biar bisa di close
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);//biar tampil
    }

    private static void convert(){
        File dir= new File(prop.getProperty("app.folder_in"));
        String[] fileList= dir.list();
        String emailAddress = "username@domai";
      if (patternMatches(emailAddress)){
      }

        if (fileList.length==0){
            JOptionPane.showMessageDialog(parent, "File conversion error!! Please check the input file.");
        }
        int recordCount;
        for (String nameFile: fileList){
            recordCount=0;
            startTime= null;
            startTime = System.nanoTime();
            try {
                FileReader fileReader = new FileReader(prop.getProperty("app.folder_in")+nameFile);
                CSVReader csvReader = new CSVReader(fileReader);
                List<String[]> records = csvReader.readAll();
                if (!checkingFileStructure(records, nameFile)){
                    continue;
                }

                String[] out= nameFile.split("\\.");
                File file = new File(prop.getProperty("app.folder_out")+ out[0]+ ".csv");
                //menghasilkan output di folder OUT sesuai nama folder IN
                FileWriter outputfile = new FileWriter(file);
                CSVWriter writer = new CSVWriter(outputfile, ',', '\u0000', '\u0000', "\n");

                writer.writeNext(new String[]{"H", debitAccount, descriptionAccount, "S", "Y", "", instructionDate, instructionAt, ""});

                for (String[] record:records){
                    recordCount+=1;
                    int length= record.length-1;
                    String email="";
                    String name=record[4];
                    String bank=record[6];
                    String code8=record[8];
                    String code9=record[9];

                    if(length == 12){
                        email= record[10]+";"+record[11]+";"+record[12];
                    }else if(length==11){
                        email= record[10]+";"+record[11];
                    }else if(length==10){
                        if (patternMatches(record[10])){
                            email= record[10];
                        }
                        else {
                        name=record[4]+" "+record[5];
                        bank=record[7];
                        code8=record[9];
                        code9=record[10];

                        }
                    }

                    if(name.contains(",")){
                        name='"'+name+'"';
                    }
//                    System.out.println("sss"+length);
//                    System.out.println("sss"+record[8]);
                    String bankCode= findBankCode(bank);
                    writer.writeNext(new String[]{"D", productCode, code8, "", record[2], "", "", bankCode, "", record[0],name, "IDR",
                            "X", "", "",email, "", "", record[3], "", "", record[1], "", "", chargeTo, chargeType, "", "", "", "",
                            code9, "", "", "", "", "", "", "", ""});
                }

                long endTime   = System.nanoTime();
                long totalTime = endTime - startTime;
                double sec= (double) totalTime/1000000000;
                int seconds= (int)sec;
                JOptionPane.showMessageDialog(parent, "Successfully converting "+recordCount+" record(s) \n in "+df2.format(sec)+" seconds");

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, "File conversion error!! Please check the input file.");
            }

        }
    }

    private static String findBankCode(String bank){
        String fileBank= "Bank Table.txt";
        try {
            FileReader fileReader = new FileReader(fileBank);;
            CSVReader csvReader = new CSVReader(fileReader);
            List<String[]> records = csvReader.readAll();
            for (String[] record:records){
                if(bank.toUpperCase().equals(record[0].toUpperCase())){
                    return record[1];
                }
            }
        } catch (RuntimeException | IOException e) {
            JOptionPane.showMessageDialog(parent, "Bank Table.txt not found");
            System.exit(0);
            e.printStackTrace();
        }
        return "";
    }

    private static boolean checkingFileStructure(List<String[]> records, String nameFile) {
        int rowCount=0;
        String[] rowError = new String[records.size()];
        String[] delimiterError = new String[records.size()];
        String[] amountError = new String[records.size()];
        int count=0;
        int delimiterCount= 0;
        int amountCount= 0;

        boolean status= true;

        boolean statusRow= true;
        boolean statusDelimiter= true;
        boolean statusAmmount= true;

        for (String[] record:records){
            rowCount+=1;
            int length= record.length-1;
            if (length<9 || length>12){
                statusRow= false;
                rowError[count]= String.valueOf(rowCount);
                count+=1;
            }

            if(Arrays.toString(record).contains(";")){
                statusDelimiter= false;
                delimiterError[delimiterCount]= String.valueOf(rowCount);
                delimiterCount+=1;
            }

            try{
                String[] amount= record[1].split("\\.");
//                System.out.println(amount[1].length());
                if (amount[1].length()>2){
                    statusAmmount= false;
                    amountError[amountCount]= String.valueOf(rowCount);
                    amountCount+=1;
                }
            }catch (Exception e){
//                statusAmmount= false;
//                amountError[amountCount]= String.valueOf(rowCount);
//                System.out.println("error");
//                amountCount+=1;
            }
        }
        if (!statusRow){
            String[] errorCount= new String[count];
            System.arraycopy(rowError, 0, errorCount, 0, count);
//            JOptionPane.showMessageDialog(parent, "Error file "+ nameFile+" row "+ Arrays.toString(errorCount).replace("[","").replace("]", ""));
            JOptionPane.showMessageDialog(parent, "Error file "+ nameFile+" row "+ Arrays.toString(errorCount)
                    .replace("[","").replace("]", "")
                    .replaceAll("(.15)", "$0\n"));
            status= false;
        }

        if (!statusAmmount){
            String[] errorCount= new String[amountCount];
            System.arraycopy(amountError, 0, errorCount, 0, amountCount);
//            JOptionPane.showMessageDialog(parent, "Error file "+ nameFile+" row "+ Arrays.toString(errorCount).replace("[","").replace("]", ""));
            JOptionPane.showMessageDialog(parent, "Check amount \nError file "+ nameFile+" row "+ Arrays.toString(errorCount)
                    .replace("[","").replace("]", "")
                    .replaceAll("(.15)", "$0\n"));
            status= false;
        }

        if (!statusDelimiter){
            String[] errorCount= new String[delimiterCount];
            System.arraycopy(delimiterError, 0, errorCount, 0, delimiterCount);
//            JOptionPane.showMessageDialog(parent, "Error file "+ nameFile+" row "+ Arrays.toString(errorCount).replace("[","").replace("]", ""));
            JOptionPane.showMessageDialog(parent, "Delimiter should be ','\nCheck delimiter file "+ nameFile+" row "+ Arrays.toString(errorCount)
                    .replace("[","").replace("]", "")
                    .replaceAll("(.15)", "$0\n"));
            status= false;
        }

        return status;

    }
    public static boolean patternMatches(String emailAddress) {
      String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}
