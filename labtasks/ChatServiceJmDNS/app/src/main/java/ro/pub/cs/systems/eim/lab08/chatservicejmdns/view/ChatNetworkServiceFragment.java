package ro.pub.cs.systems.eim.lab08.chatservicejmdns.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import ro.pub.cs.systems.eim.lab08.chatservicejmdns.R;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.controller.NetworkServiceAdapter;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations;

public class ChatNetworkServiceFragment extends Fragment {

    private EditText servicePortEditText = null;

    private Button serviceRegistrationStatusButton = null;
    private Button serviceDiscoveryStatusButton = null;

    private ChatActivity chatActivity = null;
    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private View view = null;

    private NetworkServiceAdapter discoveredServicesAdapter = null;
    private NetworkServiceAdapter conversationsAdapter = null;

    private ListView discoveredServicesListView = null;
    private ListView conversationsListView = null;

    private ServiceRegistrationStatusButtonListener serviceRegistrationStatusButtonListener = new ServiceRegistrationStatusButtonListener();
    private class ServiceRegistrationStatusButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            if (!chatActivity.getServiceRegistrationStatus()) {
                String port = servicePortEditText.getText().toString();
                if (port.isEmpty()) {
                    Toast.makeText(getActivity(), "Field service port should be filled!", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    int p = Integer.parseInt(port);
                    if (p < 1024) {
                        Toast.makeText(getActivity(), "Field service port is below 1024!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    networkServiceDiscoveryOperations.registerNetworkService(p);
                } catch (Exception exception) {
                    Log.e(Constants.TAG, "Could not register network service: " + exception.getMessage());
                    if (Constants.DEBUG) {
                        exception.printStackTrace();
                    }
                    return;
                }
                startServiceRegistration();
            } else {
                networkServiceDiscoveryOperations.unregisterNetworkService();
                stopServiceRegistration();
            }
            chatActivity.setServiceRegistrationStatus(!chatActivity.getServiceRegistrationStatus());
        }

    }

    private ServiceDiscoveryStatusButtonListener serviceDiscoveryStatusButtonListener = new ServiceDiscoveryStatusButtonListener();
    private class ServiceDiscoveryStatusButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            if (!chatActivity.getServiceDiscoveryStatus()) {
                ((ChatActivity)getActivity()).getNetworkServiceDiscoveryOperations().startNetworkServiceDiscovery();
                startServiceDiscovery();
            } else {
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                stopServiceDiscovery();
            }
            chatActivity.setServiceDiscoveryStatus(!chatActivity.getServiceDiscoveryStatus());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        Log.i(Constants.TAG, "ChatNetworkServiceFragment -> onCreateView() callback method was invoked");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_chat_network_service, parent, false);
        }
	// Question 5c
	// get a list of network interfaces available on the phone
        Enumeration interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (IOException e){
            Log.e(Constants.TAG, "Could not query interface list: " + e.getMessage());
            if (Constants.DEBUG) {
                e.printStackTrace();
            }
        }
	// get a list of IPv4 addresses associated with these interfaces
        String IPs = "";
        while(interfaces.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) interfaces.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                if (i instanceof Inet4Address) {
                    if(IPs.length() > 0)
                        IPs += ", ";
                    IPs += i.getHostAddress().toString();
                }
            }
        }
	// Update the textview with the local addresses
        TextView LocalIPs = (TextView)view.findViewById(R.id.service_discovery_local_addr);
        LocalIPs.setText(IPs);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        Log.i(Constants.TAG, "ChatNetworkServiceFragment -> onActivityCreated() callback method was invoked");

        servicePortEditText = (EditText)getActivity().findViewById(R.id.port_edit_text);

        serviceRegistrationStatusButton = (Button)getActivity().findViewById(R.id.service_registration_status_button);
        serviceRegistrationStatusButton.setOnClickListener(serviceRegistrationStatusButtonListener);

        serviceDiscoveryStatusButton = (Button)getActivity().findViewById(R.id.service_discovery_status_button);
        serviceDiscoveryStatusButton.setOnClickListener(serviceDiscoveryStatusButtonListener);

        chatActivity = (ChatActivity)getActivity();
        networkServiceDiscoveryOperations = chatActivity.getNetworkServiceDiscoveryOperations();

        discoveredServicesListView = (ListView)getActivity().findViewById(R.id.discovered_services_list_view);
        discoveredServicesAdapter = new NetworkServiceAdapter(chatActivity, chatActivity.getDiscoveredServices(), chatActivity.getNetworkServiceDiscoveryOperations());
        discoveredServicesListView.setAdapter(discoveredServicesAdapter);

        conversationsListView = (ListView)getActivity().findViewById(R.id.conversations_list_view);
        conversationsAdapter = new NetworkServiceAdapter(chatActivity, chatActivity.getConversations(), chatActivity.getNetworkServiceDiscoveryOperations());
        conversationsListView.setAdapter(conversationsAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startServiceRegistration() {
        serviceRegistrationStatusButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGreen));
        serviceRegistrationStatusButton.setText(getResources().getString(R.string.unregister_service));
    }

    public void stopServiceRegistration() {
        serviceRegistrationStatusButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
        serviceRegistrationStatusButton.setText(getResources().getString(R.string.register_service));
    }

    public void startServiceDiscovery() {
        serviceDiscoveryStatusButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGreen));
        serviceDiscoveryStatusButton.setText(getResources().getString(R.string.stop_service_discovery));
    }

    public void stopServiceDiscovery() {
        serviceDiscoveryStatusButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
        serviceDiscoveryStatusButton.setText(getResources().getString(R.string.start_service_discovery));
    }

    public void setDiscoveredServicesAdapter(NetworkServiceAdapter discoveredServicesAdapter) {
        this.discoveredServicesAdapter = discoveredServicesAdapter;
    }

    public NetworkServiceAdapter getDiscoveredServicesAdapter() {
        return discoveredServicesAdapter;
    }

    public void setConversationsAdapter(NetworkServiceAdapter conversationsAdapter) {
        this.conversationsAdapter = conversationsAdapter;
    }

    public NetworkServiceAdapter getConversationsAdapter() {
        return conversationsAdapter;
    }

}
