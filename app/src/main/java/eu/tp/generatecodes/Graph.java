package eu.tp.generatecodes;

import android.util.Log;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.text.ParseException;

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
            int x = df.parse(removedCode).intValue();
            x = nBins * x/nCodes;
            double y = graphDataTab[x].getY() + 1.;
            graphDataTab[x] = new DataPoint((x+0.5)*nCodes/nBins, y);
        } catch (ParseException e) {
            Log.e("updateGraphDataTab(" + removedCode +")", "thrown ParseException", e);
        }
    }

    public void updateGivenGraphData(int x){
        // update given graph DataPoint from Codes list
        double y = 0;
        for(int c = x*nCodes/nBins; c<(x+1)*nCodes/nBins; c++) {
            y += (auxCodes.getListCodesToCheck().contains(df.format(c))) ? 0. : 1.;
        }
        graphDataTab[x] = new DataPoint((x+0.5)*nCodes/nBins, y);
    }

}
