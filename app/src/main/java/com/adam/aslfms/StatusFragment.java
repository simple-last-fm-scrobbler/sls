package com.adam.aslfms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.AuthStatus;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.enums.SubmissionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bryan on 6/6/15.
 */
public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private static final String EXTRA_NETAPP = "StatusFragment.NETAPP";

    private NetApp mNetApp;

    private AppSettings settings;
    private ScrobblesDatabase mDb;

    private int mProfilePageLinkPosition = -1;

    private ListView mListView;

    public static StatusFragment newInstance(int netApp) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NETAPP, netApp);

        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        mListView = (ListView) rootView.findViewById(R.id.stats_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mProfilePageLinkPosition
                        && settings.getAuthStatus(mNetApp) == AuthStatus.AUTHSTATUS_OK) {
                    String url = mNetApp.getProfileUrl(settings);
                    Log.d(TAG, "Clicked link to profile page, opening: " + url);
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browser);
                }
            }
        });

        fillData();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int snapp = (int) getArguments().getSerializable(EXTRA_NETAPP);
        if (snapp < 1) {
            Log.e(TAG, "Got null snetapp");
            getActivity().finish();
        }
        mNetApp = NetApp.fromValue(snapp);

        settings = new AppSettings(getActivity());

        // TODO: remove
        mDb = new ScrobblesDatabase(getActivity());
        mDb.open();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mDb.close();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(onChange);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
        ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);
        getActivity().registerReceiver(onChange, ifs);

        fillData();
    }

    protected void fillData() {
        List<Pair> list = new ArrayList<Pair>();
        int numInCache = mDb.queryNumberOfScrobbles(mNetApp);

        // auth
        Pair auth = new Pair();
        if (settings.getAuthStatus(mNetApp) == AuthStatus.AUTHSTATUS_OK) {
            auth.setKey(getString(R.string.logged_in_just));
            auth.setValue(settings.getUsername(mNetApp));
        } else {
            auth.setKey(getString(R.string.not_logged_in));
            auth
                    .setValue(Util.getStatusSummary(getActivity(), settings, mNetApp,
                            false));
        }
        list.add(auth);

        // link to profile
        Pair prof_link = new Pair();
        prof_link.setKey(getString(R.string.profile_page));
        if (settings.getAuthStatus(mNetApp) == AuthStatus.AUTHSTATUS_OK) {
            prof_link.setValue(mNetApp.getProfileUrl(settings));
        } else {
            prof_link.setValue(getString(R.string.not_logged_in));
        }
        list.add(prof_link);
        mProfilePageLinkPosition = list.size() - 1;

        // scrobble
        Pair scrobble = new Pair();
        scrobble.setKey(getSubmissionStatusKey(SubmissionType.SCROBBLE));
        scrobble.setValue(getSubmissionStatusValue(SubmissionType.SCROBBLE));
        list.add(scrobble);

        // np
        Pair np = new Pair();
        np.setKey(getSubmissionStatusKey(SubmissionType.NP));
        np.setValue(getSubmissionStatusValue(SubmissionType.NP));
        list.add(np);

        // scrobbles in cache
        Pair cache = new Pair();
        cache.setKey(getString(R.string.scrobbles_cache_nonum));
        cache.setValue(Integer.toString(numInCache));
        list.add(cache);

        // scrobble stats
        Pair scstats = new Pair();
        scstats.setKey(getString(R.string.stats_scrobbles));
        scstats.setValue(Integer.toString(settings.getNumberOfSubmissions(
                mNetApp, SubmissionType.SCROBBLE)));
        list.add(scstats);

        // np stats
        Pair npstats = new Pair();
        npstats.setKey(getString(R.string.stats_nps));
        npstats.setValue(Integer.toString(settings.getNumberOfSubmissions(
                mNetApp, SubmissionType.NP)));
        list.add(npstats);

        ArrayAdapter<Pair> adapter = new MyArrayAdapter(getActivity(),
                R.layout.status_info_row, R.id.key, list);

        mListView.setAdapter(adapter);
    }

    private String getSubmissionStatusKey(SubmissionType stype) {
        if (settings.wasLastSubmissionSuccessful(mNetApp, stype)) {
            return sGetLastAt(stype);
        } else {
            return sGetLastFailAt(stype);
        }
    }

    private String getSubmissionStatusValue(SubmissionType stype) {
        long time = settings.getLastSubmissionTime(mNetApp, stype);
        String when;
        String what;
        if (time == -1) {
            when = getString(R.string.never);
            what = "";
        } else {
            when = Util.timeFromLocalMillis(getActivity(), time);
            what = "\n" + settings.getLastSubmissionInfo(mNetApp, stype);
        }

        return when + what;
    }

    private String sGetLastAt(SubmissionType stype) {
        if (stype == SubmissionType.SCROBBLE) {
            return getString(R.string.scrobble_last_at);
        } else {
            return getString(R.string.nowplaying_last_at);
        }
    }

    private String sGetLastFailAt(SubmissionType stype) {
        if (stype == SubmissionType.SCROBBLE) {
            return getString(R.string.scrobble_last_fail_at);
        } else {
            return getString(R.string.nowplaying_last_fail_at);
        }
    }

    private BroadcastReceiver onChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
                String snapp = getActivity().getIntent().getExtras().getString("netapp");
                if (snapp == null) {
                    Log.e(TAG, "Got null snetapp from broadcast");
                    return;
                }
                NetApp napp = NetApp.valueOf(snapp);
                if (napp == mNetApp) {
                    StatusFragment.this.fillData();
                }
            }
        }
    };

    protected synchronized NetApp getNetApp() {
        return mNetApp;
    }

    private static class Pair {
        private String key;
        private String value;

        private Pair() {
            super();
        }

        private Pair(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private class MyArrayAdapter extends ArrayAdapter<Pair> {

        public MyArrayAdapter(Context context, int resource,
                              int textViewResourceId, List<Pair> list) {
            super(context, resource, textViewResourceId, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.status_info_row, parent, false);

            Pair item = this.getItem(position);

            TextView keyView = (TextView) view.findViewById(R.id.key);
            keyView.setText(item.getKey());

            TextView valueView = (TextView) view.findViewById(R.id.value);
            valueView.setText(item.getValue());

            return view;
        }

    }
}
