package com.pacmac.betshare;

import org.w3c.dom.Text;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pacmac.betshare.R;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class LiveScoreFragment extends Fragment {

	public static WebView webView;
	private Button soccer;
	private Button hockey;
	private Button tennis;
	private Button basketball;
	private Button handball;
	private Button volleyball;
	private Button amFootball;

	private TextView sport;
	
	public LiveScoreFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.live_score, null);

        AdView mAdView = (AdView) v.findViewById(R.id.adLiveScore);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
		
		webView = (WebView) v.findViewById(R.id.webView1);
		webView.setPadding(0, 0, 0, 0);
		webView.setInitialScale(100);

		WebSettings webViewSettings = webView.getSettings();
		webViewSettings.setJavaScriptEnabled(true);

	//	webView.setWebViewClient(new WebViewClient());
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {

				webView.loadUrl("javascript:(function() { "
						+ "document.getElementById(\'div-gpt-ad-1378402944072-3\').style.display=\"none\"; "
						+ "document.getElementsById(\'window-size\').style.display=\"none\"; "
						+ "})()");

			}
		});

		soccer = (Button) v.findViewById(R.id.soccerBtn);
		hockey = (Button) v.findViewById(R.id.hockeyBtn);
		tennis = (Button) v.findViewById(R.id.tennisBtn);
		basketball = (Button) v.findViewById(R.id.basketBtn);
		handball = (Button) v.findViewById(R.id.handballBtn);
		volleyball = (Button) v.findViewById(R.id.voleyballBtn);
		amFootball = (Button) v.findViewById(R.id.amFootballBtn);
		sport = (TextView) v.findViewById(R.id.sportTxtLive);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		webView.loadUrl("http://free.scorespro.com/soccer2.php?style=ffffff,5B5A58,0F0F0F,A4FFFF,FFFFFF,A63535,C02424,tahoma,8,468,0F0F0F");
		
		soccer.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				Log.d("TAG", "progress: " + webView.getProgress());
				webView.loadUrl("http://free.scorespro.com/soccer2.php?style=ffffff,5B5A58,0F0F0F,A4FFFF,FFFFFF,A63535,C02424,tahoma,8,468,0F0F0F");
				sport.setText(getResources().getString(R.string.soccer));
			}
		});
		Log.d("TAG", "progress: " + webView.getProgress());
		hockey.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("http://free.scorespro.com/hockey.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.ice_hockey));
			}
		});

		tennis.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("http://free.scorespro.com/tennis.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.tennis));
			}
		});

		basketball.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("http://free.scorespro.com/basketball.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.basketball));
			}
		});

		handball.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("javascript:(function() { "
						+ "document.getElementById(\'div-gpt-ad-1378402944072-3\').style.display=\"none\"; "
						+ "document.getElementsByTagName(\'table\')[0].style.display=\"none\"; "
						+ "})()");

				webView.loadUrl("http://free.scorespro.com/handball.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.handball));
			}
		});

		volleyball.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("javascript:(function() { "
						+ "document.getElementById(\'div-gpt-ad-1378402944072-3\').style.display=\"none\"; "
						+ "document.getElementsByTagName(\'table\')[0].style.display=\"none\"; "
						+ "})()");

				webView.loadUrl("http://free.scorespro.com/volleyball.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.volleyball));
			}
		});

		amFootball.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("javascript:(function() { "
						+ "document.getElementById(\'div-gpt-ad-1378402944072-3\').style.display=\"none\"; "
						+ "document.getElementsByTagName(\'table\')[0].style.display=\"none\"; "
						+ "})()");

				webView.loadUrl("http://free.scorespro.com/football.php?style=ffffff,5B5A58,0F0F0F,A63535,000000,000033,000000,A4FFFF,A3AFD0,FFFF8F,C02424,Tahoma,10,468");
				sport.setText(getResources().getString(R.string.am_football));
			}
		});

		super.onViewCreated(view, savedInstanceState);
	}

	public static WebView getWebView() {
		return webView;
	}
}
