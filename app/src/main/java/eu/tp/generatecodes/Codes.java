package eu.tp.generatecodes;

import android.content.Context;
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
    private final String listFilename = "codes";
    private final String lastGeneratedFilename = "lastGenerated";

    // Context
    private static Codes theCodes = null;
    private static Context theContext = null;

    private List<String> listCodesToCheck = null;

    private String lastGeneratedCode;

    public List<String> getListCodesToCheck() {
        return listCodesToCheck;
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

    private String saveFiles(String oldLastGeneratedCode){
        String _lastGeneratedCode = null;
        try {
            // if fails than rollback
            saveCodesList();
            saveLastGeneratedCode();
            _lastGeneratedCode = lastGeneratedCode;
        } catch (Exception e) {
            listCodesToCheck.add(lastGeneratedCode);
            lastGeneratedCode = oldLastGeneratedCode;
            Log.e("saveFiles()", "Faild to save modified files", e);
            throw new RuntimeException("saveFiles() : Faild to save modified files");
        }
        return _lastGeneratedCode;
    }

    public String generateCode(){
        //
        String oldLastGeneratedCode = lastGeneratedCode;
        int codePosition = ThreadLocalRandom.current().nextInt(listCodesToCheck.size());
        lastGeneratedCode = listCodesToCheck.get(codePosition);
        listCodesToCheck.remove(codePosition);

        return saveFiles(oldLastGeneratedCode);
    }

    public boolean removeCode(String toBeRemoved){
        boolean success = false;
        String oldLastGeneratedCode = lastGeneratedCode;
        String _lastGeneratedCode = null;

        success = listCodesToCheck.remove(toBeRemoved);

        if(success){
            lastGeneratedCode = toBeRemoved;
            _lastGeneratedCode = saveFiles(oldLastGeneratedCode);
            success = lastGeneratedCode.equals(_lastGeneratedCode);
        }

        Log.i("Codes.removeCode()", " " + toBeRemoved + " was (" + success + ") removed.");
        return success;
    }

    private void saveCodesList() throws Exception {
        File file = new File(theContext.getFilesDir(), listFilename);
        long t0 = file.lastModified();
        long fileLength0 = file.length();

        try (FileOutputStream outputStream = theContext.openFileOutput(listFilename, Context.MODE_PRIVATE)) {
            for(int cnt=0; cnt<listCodesToCheck.size(); cnt++) {
                String code = listCodesToCheck.get(cnt);
                outputStream.write(code.getBytes());
            }
        }
        long now = System.currentTimeMillis();
        long t1 = file.lastModified();
        if(!(t1 > t0)) Log.e(this.getClass().getSimpleName(), "saveCodesList(): NOT recently modified");

        long fileLength1 = file.length();
        if(fileLength1 - fileLength0 != -4) Log.e(this.getClass().getSimpleName(), "saveCodesList(): NOT File is shorter by 4 bytes but : " +
                (fileLength1 - fileLength0));
    }

    private void readCodesList(){
        if(listCodesToCheck!=null){
            listCodesToCheck.clear();
        } else {
            listCodesToCheck = new ArrayList<>();
        }
        if(listCodesToCheck.size() != 0) Log.e(this.getClass().getSimpleName(), "readCodesList(): List of Codes NOT empty, but is " +
                listCodesToCheck.size());
        Log.i("readCodesList()", "listCodesToCheck.size() = " + listCodesToCheck.size());

        byte[] bytes = new byte[4];
        try (FileInputStream fin = theContext.openFileInput(listFilename)) {
            while(fin.read(bytes, 0, 4) == 4){
                String readCode = new String(bytes, Charset.defaultCharset());
                listCodesToCheck.add(readCode);
            }
            Log.i("readCodeList()", ": list.size()=" + listCodesToCheck.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateAllCodes() {

        File file = new File(theContext.getFilesDir(), listFilename);
        if(file.exists()){
            Log.i("generateAllCodes()", listFilename + " already EXISTS");
            return;
        }
        DecimalFormat df = new DecimalFormat("0000");
        df.setParseIntegerOnly(true);

        try (FileOutputStream outputStream = theContext.openFileOutput(listFilename, Context.MODE_PRIVATE)) {
            for(int cnt=0; cnt<10_000; cnt++) {
                String code = df.format(cnt);
                outputStream.write(code.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
