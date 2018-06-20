package eu.tp.generatecodes;

import android.util.Log;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;

public class Graph {
    private static Codes auxCodes = null;
    private static Graph theGraph = null;


    public synchronized static Graph singletonFactory(Codes codes){
        if(theGraph == null) {
            auxCodes = codes;
            theGraph = new Graph();
        }
        return theGraph;
    }

    private static final int nBins = 50;
    private static final int nCodes = 10_000;
    
    static BarGraphSeries<DataPoint> series = null;
    static DataPoint[] graphDataTab = new DataPoint[nBins];

    private DecimalFormat df = new DecimalFormat("0000");

    private Graph(){
        df.setParseIntegerOnly(true);
    }

    public static int getNBins() {
        return nBins;
    }

    public static int getNCodes() {
        return nCodes;
    }

    public void updateGraphDataTab(String removedCode){
        try {
            int ncode = df.parse(removedCode).intValue();
            int binx = nBins * ncode/nCodes;
            double y = graphDataTab[binx].getY() + 1.;
            graphDataTab[binx] = new DataPoint((binx+0.5)*nCodes/nBins, y);
        } catch (ParseException e) {
            Log.e("updateGraphDataTab(" + removedCode +")", "thrown ParseException", e);
        }
    }

    public void createGraphDataTab(MainActivity.MakeGraph makeGraph) {
        Iterator<String> iToBeChecked = auxCodes.getToBeCheckedCodeList().iterator();

        int dataTab[] = new int[nBins];

        for(short s=0; s<nBins; s++){
            dataTab[s] = nCodes/nBins;
        }

        while(iToBeChecked.hasNext()){
            int ncode = parseCode(iToBeChecked.next());
            int binx = nBins * ncode/nCodes;
            dataTab[binx]--;
        }

        for(int binx=0; binx< dataTab.length; binx++){
            makeGraph.updateProgress(100 * binx/Graph.getNBins());
            graphDataTab[binx] = new DataPoint((binx+0.5)*nCodes/nBins, dataTab[binx]);
        }
    }

    private int parseCode(String code) {
        int ncode = -1;
        try {
            ncode = df.parse(code).intValue();
        } catch (ParseException e) {
            Log.e("parseCode to int", "Parse exception - " + e.getMessage());
        }
        return ncode;
    }

}
