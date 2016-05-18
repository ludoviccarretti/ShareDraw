package umlv.fr.sharedraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserConnectedActivity extends Fragment implements NotifyService {
    private List<String> connected = new ArrayList<>();

    public UserConnectedActivity() {

    }

    public static UserConnectedActivity newInstance() {
        return new UserConnectedActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_connected, container, false);
    }

    @SuppressWarnings("all")
    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            connected = savedInstanceState.getStringArrayList("connected");
        }
    }

    @SuppressWarnings("all")
    private void updateAndSetAdapter(List<String> items) {
        ListView lv = (ListView) getActivity().findViewById(R.id.listView_users);
        assert lv != null;

        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {

                        convertView = getActivity().getLayoutInflater().inflate(R.layout.activity_user_connected_item_list_view, null);
                    }
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position));
                    return convertView;
                }
            };
            lv.setAdapter(adapter);
        }
        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) adapter;
        arrayAdapter.clear();
        arrayAdapter.addAll(items);
    }

    @Override
    public void notifyServiceConnected() {
        if (connected.isEmpty()) {
            connected = MainFragmentActivity.HTTP_SERVICE.getListOfUsersConnected();
        }
        updateAndSetAdapter(connected);
    }
}