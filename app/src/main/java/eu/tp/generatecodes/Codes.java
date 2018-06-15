package eu.tp.generatecodes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import eu.tp.codes.generatecodes.R;

public class Codes {
    private final String toBeCheckedCodeListFilename = "codes";
    private final String reCheckCodeListFilename = "alreadyTestedCodes";
    private final String lastGeneratedFilename = "lastGenerated";

    // Context
    private static Codes theCodes = null;
    private static Context theContext = null;

    private List<String> toBeCheckedCodeList = null;
    private List<String> reCheckCodeList = null;

    private String lastGeneratedCode;

    public List<String> getToBeCheckedCodeList() {
        return toBeCheckedCodeList;
    }

    public synchronized static  Codes singletonFactory(Context context){
        theContext = context;
        if(theCodes == null) {
            theCodes = new Codes();
        }
        return theCodes;
    }

    private Codes() {
        readLastGeneratedCode();
        generateAllCodes();
        readCodesList();
    }

    private void readLastGeneratedCode(){
        File file = new File(theContext.getFilesDir(), lastGeneratedFilename);
        if(file.exists()){
            byte[] bytes = new byte[4];
            try (FileInputStream fin = theContext.openFileInput(lastGeneratedFilename)) {
                int iRead = fin.read(bytes, 0, 4);
                if(iRead != 4) Log.e(this.getClass().getSimpleName(), "readLastGeneratedCode(): should Read 4 bytes, but was = " + iRead);
                lastGeneratedCode = new String(bytes, Charset.defaultCharset());
                Log.i(this.getClass().getSimpleName(), "readLastGeneratedCode(): lastGeneratedCode READ is " + lastGeneratedCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lastGeneratedCode = theContext.getString(R.string.empty_code);
        }
    }

    private void saveLastGeneratedCode() throws Exception {
        try (FileOutputStream outputStream = theContext.openFileOutput(lastGeneratedFilename, Context.MODE_PRIVATE)) {
            outputStream.write(lastGeneratedCode.getBytes());
        }
    }

    public String getLastGeneratedCode() {
        return lastGeneratedCode;
    }

    private CodeRemovedFromList saveFiles(CodeRemovedFromList codeRemovedFrom){
        try {
            // if fails than rollback
            saveCodesList();
            saveLastGeneratedCode();
            codeRemovedFrom.setSuccess(true);
        } catch (Exception e) {
            if(codeRemovedFrom.getRemovedFromCodeList() == CodeLists.BOTH){
                toBeCheckedCodeList.add(lastGeneratedCode);
                reCheckCodeList.add(lastGeneratedCode);
            } else {
                List<String> listOfCodes = (codeRemovedFrom.getRemovedFromCodeList() == CodeLists.TO_BE_CHECKED) ? toBeCheckedCodeList : reCheckCodeList;
                listOfCodes.add(lastGeneratedCode);
            }
            lastGeneratedCode = codeRemovedFrom.getOldCode();
            codeRemovedFrom.setSuccess(false);
            Log.e("saveFiles()", "Faild to save modified files", e);
        }
        return codeRemovedFrom;
    }

    public CodeRemovedFromList generateCode(){
        //
        int toBeCheckedSize = toBeCheckedCodeList.size();
        int reCheckCodeSize = reCheckCodeList.size();

        if(toBeCheckedSize == 0 && reCheckCodeSize == 0){
            lastGeneratedCode = "#*-*#";
            return new CodeRemovedFromList("All codes already generated!", null);
        }

        int totalWeightedSize = toBeCheckedSize + reCheckCodeSize;

        CodeRemovedFromList codeRemovedFrom;
        if(reCheckCodeList.size() == 0
                || ThreadLocalRandom.current().nextInt(totalWeightedSize) < toBeCheckedSize){
            //
            codeRemovedFrom = randomlyRemoveFromList(CodeLists.TO_BE_CHECKED);
        } else {
            //
            codeRemovedFrom = randomlyRemoveFromList(CodeLists.ALREADY_CHECKED);
        }

        return saveFiles(codeRemovedFrom);
    }

    private CodeRemovedFromList randomlyRemoveFromList(CodeLists codeList) {
        if(codeList==CodeLists.BOTH) throw new RuntimeException("codeList should be here either TO_BE_CHECKED or ALREADY_CHECKED");
        CodeRemovedFromList codeRemovedFrom = new CodeRemovedFromList(lastGeneratedCode, codeList);
        List<String> listOfCodes = (codeList == CodeLists.TO_BE_CHECKED)? toBeCheckedCodeList: reCheckCodeList;

        int codePosition = ThreadLocalRandom.current().nextInt(listOfCodes.size());
        lastGeneratedCode = listOfCodes.remove(codePosition);

        return codeRemovedFrom;
    }

    public CodeRemovedFromList removeCode(String toBeRemoved){

        if(toBeRemoved.equals("123456789")){

            DecimalFormat df = new DecimalFormat("0000");
            df.setParseIntegerOnly(true);

            reCheckCodeList = new ArrayList<>();


            for(int i=0;i<10_000;i++){
                String code = df.format(Integer.valueOf(i));
                if(!toBeCheckedCodeList.contains(code)){
                    reCheckCodeList.add(code);
                }
            }

            String text = "Have generated "+ reCheckCodeList.size() +" codes to rechecked again";
            return new CodeRemovedFromList(text, null);
        }

        boolean success_codesToBeChecked, success_recheckedCodes;
        String oldLastGeneratedCode = lastGeneratedCode;

        success_codesToBeChecked = toBeCheckedCodeList.remove(toBeRemoved);
        success_recheckedCodes = reCheckCodeList.remove(toBeRemoved);

        if(success_codesToBeChecked && success_recheckedCodes){
            return genCodeRemovedFromList(toBeRemoved, oldLastGeneratedCode, CodeLists.BOTH);
        } else if(success_codesToBeChecked){
            return genCodeRemovedFromList(toBeRemoved, oldLastGeneratedCode, CodeLists.TO_BE_CHECKED);
        } else if(success_recheckedCodes) {
                return genCodeRemovedFromList(toBeRemoved, oldLastGeneratedCode, CodeLists.ALREADY_CHECKED);
        }

        return new CodeRemovedFromList(false);
    }

    @NonNull
    private CodeRemovedFromList genCodeRemovedFromList(String toBeRemoved, String oldLastGeneratedCode, CodeLists codeList) {
        lastGeneratedCode = toBeRemoved;
        CodeRemovedFromList codeRemovedFrom = new CodeRemovedFromList(oldLastGeneratedCode, codeList);
        return saveFiles(codeRemovedFrom);
    }

    private void saveCodesList() throws Exception {
        try (FileOutputStream outputStream = theContext.openFileOutput(toBeCheckedCodeListFilename, Context.MODE_PRIVATE)) {
            for(String code : toBeCheckedCodeList) {
                outputStream.write(code.getBytes());
            }
        }

        try (FileOutputStream outputStream = theContext.openFileOutput(reCheckCodeListFilename, Context.MODE_PRIVATE)) {
            for (String code : reCheckCodeList) {
                outputStream.write(code.getBytes());
            }
        }

    }

    private void readCodesList(){
        toBeCheckedCodeList = new ArrayList<>();
        reCheckCodeList = new ArrayList<>();
        readList(toBeCheckedCodeList, toBeCheckedCodeListFilename);
        readList(reCheckCodeList, reCheckCodeListFilename);
    }

    private void readList(List<String> listToBeRead, String fromFile) {
        File file = new File(theContext.getFilesDir(), fromFile);
        if(!file.exists()) return;

        if(listToBeRead!=null){
            listToBeRead.clear();
        } else {
            listToBeRead = new ArrayList<>();
        }

        byte[] bytes = new byte[4];
        try (FileInputStream fin = theContext.openFileInput(fromFile)) {
            while(fin.read(bytes, 0, 4) == 4){
                String readCode = new String(bytes, Charset.defaultCharset());
                listToBeRead.add(readCode);
            }
            Log.i("READ", ": from "+fromFile + " #=" + listToBeRead.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateAllCodes() {

        File file = new File(theContext.getFilesDir(), toBeCheckedCodeListFilename);
        if(file.exists()){
            Log.i("generateAllCodes()", toBeCheckedCodeListFilename + " already EXISTS");
            return;
        }
        DecimalFormat df = new DecimalFormat("0000");
        df.setParseIntegerOnly(true);

        try (FileOutputStream outputStream = theContext.openFileOutput(toBeCheckedCodeListFilename, Context.MODE_PRIVATE)) {
            for(int cnt=0; cnt<10_000; cnt++) {
                String code = df.format(cnt);
                outputStream.write(code.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
