package eu.tp.generatecodes;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

    static LineGraphSeries<DataPoint> series = null;
    static DataPoint[] graphDataTab = new DataPoint[10_000];

    private DecimalFormat df = new DecimalFormat("0000");

    private Graph(){
        df.setParseIntegerOnly(true);
    }

    public void updateGraphDataTab(String removedCode){
        try {
            int x = df.parse(removedCode).intValue();
            graphDataTab[x] = new DataPoint(x,0);
        } catch (ParseException e) {
            Log.e("updateGraphDataTab(" + removedCode +")", "thrown ParseException", e);
        }
    }

    public void generateGraph(){
        // create graph data series from Codes list
        for (int x = 0; x < 10_000; x++) {
            double y = (auxCodes.getListCodesToCheck().contains(df.format(x))) ? 1. : 0.;
            graphDataTab[x] = new DataPoint(x, y);
        }
    }

}
