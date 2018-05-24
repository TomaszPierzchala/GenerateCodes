package eu.tp.generatecodes;

import android.content.Context;
import android.os.AsyncTask;
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
import com.jjoe64.graphview.series.LineGraphSeries;

import eu.tp.codes.generatecodes.R;

import static eu.tp.codes.generatecodes.R.color.colorBackground;
import static eu.tp.codes.generatecodes.R.color.colorBorder;
import static eu.tp.codes.generatecodes.R.id.navigation_generate;


public class MainActivity extends AppCompatActivity {

    private TextView mTextCode;
    private Codes auxCodes = null;
    private Graph auxGraph = null;
    private static int navigationId = R.id.navigation_generate;

    private final float SMALL = 20f;
    private final float BIG = 36f;


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
                        mTextCode.setText(auxCodes.getLastGeneratedCode());
                        return true;
                    case R.id.navigation_add_checked:
                        EditText insertCode = findViewById(R.id.insertCode);
                        insertCode.setText(R.string.insert_code_text);
                        insertCode.setTextSize(SMALL);
                        insertCode.setTextColor(getResources().getColor(colorBackground, null));

                        mTextCode.setText(R.string.title_add_checked);
                        return true;
                }
                return false;
            }
        };
    }

    protected void generateCode() {
        String lastGenrateCode = auxCodes.generateCode();
        updateInfo(lastGenrateCode);
    }

    protected boolean removeCode(String toBeRemoved) {
        boolean wasRemoved = auxCodes.removeCode(toBeRemoved);
        if(wasRemoved) updateInfo(toBeRemoved);
        return wasRemoved;
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

        mTextCode = findViewById(R.id.code);
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

        updateInfo(null);
    }

    public void removeCode(View v) {
        // onButton clik at Remove (Remoce subview)
        EditText insertCode = findViewById(R.id.insertCode);
        String toBeRemoved = insertCode.getText().toString();
        boolean wasRemoved = removeCode(toBeRemoved);

        Context context = getApplicationContext();
        CharSequence text = toBeRemoved + ((wasRemoved) ? " was REMOVED" : " was Not removed");
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        insertCode.setTextSize(SMALL);
        insertCode.setTextColor(getResources().getColor(colorBackground, null));
        insertCode.setText(getText(R.string.insert_code_text));

    }

    private void updateInfo(String removedCode){
        if(removedCode!=null) {
            auxGraph.updateGraphDataTab(removedCode);
        }
        updateCodesToCheckTextView();
        new MakeGraph().execute((Void) null);
    }
    private void updateCodesToCheckTextView() {
        TextView lastCodes = findViewById(R.id.lastCodes);
        lastCodes.setText(Integer.toString(auxCodes.getListCodesToCheck().size()));
    }

    private class MakeGraph extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            GraphView graph = findViewById(R.id.graph);
            // activate horizontal zooming and scrolling
            graph.getViewport().setScalable(true);

            // activate horizontal scrolling
            graph.getViewport().setScrollable(true);

            // set manual X bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(10_000);
            // set manual Y bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(1);

            if (Graph.series == null) {
                Graph.series = new LineGraphSeries<>();
                auxGraph.generateGraph();
                /*while (Graph.graphDataTab[9_999] == null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.w("generateGraph", "Exception while waited for all graph data to be generated", e);
                    }
                }*/
                Graph.series = new LineGraphSeries<>(Graph.graphDataTab);
                Graph.series.setThickness(1);
                graph.addSeries(Graph.series);
            } else {
                Graph.series.resetData(Graph.graphDataTab);
                graph.addSeries(Graph.series);
            }
            return null;
        }
    }

}
