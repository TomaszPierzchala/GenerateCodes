package eu.tp.generatecodes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import eu.tp.codes.generatecodes.R;

import static eu.tp.codes.generatecodes.R.color.colorBackground;
import static eu.tp.codes.generatecodes.R.color.colorBorder;
import static eu.tp.codes.generatecodes.R.id.navigation_generate;


public class MainActivity extends AppCompatActivity {

    private Codes auxCodes = null;
    private Graph auxGraph = null;
    private static int navigationId = R.id.navigation_generate;

    private final float SMALL = 20f;
    private final float BIG = 36f;
    private final long WAIT_BEFORE_REGENERATE = 5000; // wait 5s to avoid accidental regeneration

    static Long lastGeneratedTime;

    public MainActivity() {
        if(lastGeneratedTime==null){
            lastGeneratedTime = System.currentTimeMillis() - 2000;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    {
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

//            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                navigationId = item.getItemId();
                setVisibility(item.getItemId());

                switch (item.getItemId()) {
                    case R.id.navigation_generate:
                        generateCode();
                        TextView mTextCode = findViewById(R.id.code);
                        mTextCode.setText(auxCodes.getLastGeneratedCode());
                        return true;
                    case R.id.navigation_add_checked:
                        EditText insertCode = findViewById(R.id.insertCode);
                        insertCode.setText(R.string.insert_code_text);
                        insertCode.setTextSize(SMALL);
                        insertCode.setTextColor(getResources().getColor(colorBackground, null));

                        return true;
                }
                return false;
            }
        };
    }

    protected void generateCode() {
        if(System.currentTimeMillis() - lastGeneratedTime < WAIT_BEFORE_REGENERATE){
            Context context = getApplicationContext();
            CharSequence text = "To avoid miss clicks, please wait few more seconds (<5s)";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        lastGeneratedTime = System.currentTimeMillis();

        CodeRemovedFromList codeRemovedFromList = auxCodes.generateCode();
        Boolean success = codeRemovedFromList.getSuccess();

        Context context = getApplicationContext();
        CharSequence text;
        int duration = Toast.LENGTH_LONG;

        if(success == null){
            text = codeRemovedFromList.getOldCode();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if(success) {
            auxGraph.updateGraphDataTab(auxCodes.getLastGeneratedCode());
            text = "Code was removed from " + codeRemovedFromList.getRemovedFromCodeList();
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        updateInfoAndGraph();
    }

    protected CodeRemovedFromList removeCode(String toBeRemoved) {
        CodeRemovedFromList CodeRemovedFromList = auxCodes.removeCode(toBeRemoved);
        if(CodeRemovedFromList.getSuccess()!=null && CodeRemovedFromList.getSuccess()){
            auxGraph.updateGraphDataTab(toBeRemoved);
        }
        updateInfoAndGraph();
        return CodeRemovedFromList;
    }

    private void setVisibility(int itemId) {
        switch (itemId) {
            case navigation_generate:
                findViewById(R.id.label).setVisibility(View.VISIBLE);
                findViewById(R.id.code).setVisibility(View.VISIBLE);
                findViewById(R.id.insertCode).setVisibility(View.GONE);
                findViewById(R.id.addButton).setVisibility(View.GONE);
                break;
            case R.id.navigation_add_checked:
                findViewById(R.id.label).setVisibility(View.GONE);
                findViewById(R.id.code).setVisibility(View.GONE);
                findViewById(R.id.insertCode).setVisibility(View.VISIBLE);
                findViewById(R.id.addButton).setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auxCodes = Codes.singletonFactory(this);
        auxGraph = Graph.singletonFactory(auxCodes);

        TextView mTextCode = findViewById(R.id.code);
        mTextCode.setText(auxCodes.getLastGeneratedCode());

        final EditText insertCode = findViewById(R.id.insertCode);
        insertCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCode.setText("");
                insertCode.setTextSize(BIG);
                insertCode.setTextColor(getResources().getColor(colorBorder, null));
            }
        });
        if (insertCode.getText().toString().equals(getString(R.string.insert_code_text))) {
            insertCode.setTextSize(SMALL);
            insertCode.setTextColor(getResources().getColor(colorBackground, null));
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setVisibility(navigationId);

        updateInfoAndGraph();
    }

    public void removeCode(View v) {
        // onButton clik at Remove (Remoce subview)
        EditText insertCode = findViewById(R.id.insertCode);
        String toBeRemoved = insertCode.getText().toString();
        CodeRemovedFromList CodeRemovedFromList = removeCode(toBeRemoved);

        Context context = getApplicationContext();
        CharSequence text;
        if(CodeRemovedFromList.getSuccess() == null){
            text = CodeRemovedFromList.getOldCode();
        } else {
            boolean wasRemoved = CodeRemovedFromList.getSuccess();
            text = "\""+ toBeRemoved + "\"" + ((wasRemoved) ? " was REMOVED" : " was Not removed")
            + ((CodeRemovedFromList.getRemovedFromCodeList()!=null)? " from " + CodeRemovedFromList.getRemovedFromCodeList(): "");
        }

        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        insertCode.setTextColor(getResources().getColor(colorBackground, null));
    }

    private void updateInfoAndGraph(){
        updateCodesToCheckTextView();
        createOrUpdateGraph();
    }
    private void updateCodesToCheckTextView() {
        TextView lastCodes = findViewById(R.id.lastCodes);
        lastCodes.setText(Integer.toString(auxCodes.getToBeCheckedCodeList().size()));
    }

    private void createOrUpdateGraph() {

            GraphView graph = findViewById(R.id.graph);
            // activate horizontal zooming and scrolling
            graph.getViewport().setScalable(true);

            // activate horizontal scrolling
            graph.getViewport().setScrollable(true);

            // set manual X bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(Graph.getNCodes());
            // set manual Y bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(1);

            if (Graph.series == null) {
                auxGraph.createGraphDataTab();
            }
            Graph.series = new BarGraphSeries<>(Graph.graphDataTab);
            Graph.series.setDataWidth(Graph.getNCodes()/Graph.getNBins());
            Graph.series.setSpacing(0);
            graph.addSeries(Graph.series);

            Graph.series.setValueDependentColor(new ValueDependentColor<DataPoint>() {

                @Override
                public int get(DataPoint data) {
                    return Color.argb((int) data.getY()*255 * Graph.getNBins()/Graph.getNCodes(), 0, 0, 255 );
                }
            });
        }

}
