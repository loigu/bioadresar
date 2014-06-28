package cz.hnutiduha.bioadresar.view;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View.OnKeyListener;

public class SearchView extends LinearLayout implements View.OnClickListener, TextWatcher, OnKeyListener{

	EditText searchText;
	ImageButton searchButton;
	Context context;
	
	public SearchView(Context context, AttributeSet attrs){
		super(context, attrs);
		this.context = context;
		
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_view, this);

        searchText = (EditText)findViewById(R.id.searchText);
        searchButton = (ImageButton)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        
        searchText.setHint(context.getResources().getString(R.string.search_hint));
        searchText.setOnKeyListener(this);
        searchText.addTextChangedListener(this);
        
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
        	searchText.setText(query);
        }
        DataSource<LocationInfo> db = DataSourceFactory.getGlobalDataSource(activity);

        return db.getFilter(query);
	}
	
	private void fireSearch()
	{
		String query = searchText.getText().toString();
		if (query.equals("")) { return; }
		
		Intent search = new Intent(context, context.getClass());
		search.putExtra(SearchManager.QUERY, query);
		search.setAction(Intent.ACTION_SEARCH);
		context.startActivity(search);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(searchButton))
		{
			fireSearch();
		}
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
			fireSearch();
			return true;
		}
		
		return false;
	}
}
