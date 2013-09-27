package cz.hnutiduha.bioadresar.view;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchView extends LinearLayout implements View.OnClickListener{

	TextView searchText;
	ImageButton searchButton;
	Context context;
	
	public SearchView(Context context, AttributeSet attrs){
		super(context, attrs);
		this.context = context;
		
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_view, this);

        searchText = (TextView)findViewById(R.id.searchText);
        searchButton = (ImageButton)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        
        searchText.setHint(context.getResources().getString(R.string.search_hint));
	}
	
	public DataFilter handleQuery(Activity activity)
	{
        // Get the intent, verify the action and get the query
		Intent intent = activity.getIntent();
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	return null;
        }
        
        String query = intent.getStringExtra(SearchManager.QUERY);
        Log.d("List", "got query " + query);
        if (query != null)
        {
        	((TextView)findViewById(R.id.searchText)).setHint(query);
        }
        HnutiduhaFarmDb db = HnutiduhaFarmDb.getDefaultDb(activity);

        return db.getFilter(query);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(searchButton))
		{
			String query = searchText.getText().toString();
			if (query.equals("")) { return; }
			
			Intent search = new Intent(context, context.getClass());
			search.putExtra(SearchManager.QUERY, query);
			search.setAction(Intent.ACTION_SEARCH);
			context.startActivity(search);
		}
		
	}
}
