package com.aingerusanchez.healthybeat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Set;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener {

    private ArrayList<DeviceItem> deviceItemList;
    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter bTAdapter;
    private static String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static int requestCodePermission = 1;
    private static int requestPermissionOK = 1;
    private static int requestPermissionFAIL = -1;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<DeviceItem> mAdapter;

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Crear un nuevo objeto DISPOSITIVO
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Añadirlo al adaptador
                mAdapter.add(newDevice);
                mAdapter.notifyDataSetChanged();
            }
        }
    };
    private int navigationMenuItemId;

    // TODO: Rename and change types of parameters
    public static DeviceListFragment newInstance(BluetoothAdapter adapter) {
        DeviceListFragment fragment = new DeviceListFragment();
        bTAdapter = adapter;
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DeviceListFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");
        deviceItemList = new ArrayList<DeviceItem>();

        pedirPermisos();

        Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(), device.getAddress(), "false");
                deviceItemList.add(newDevice);
            }
        }

        // En caso de no encontrar dispositivos, añadir un item que lo muestre.
        if(deviceItemList.size() == 0) {
            deviceItemList.add(new DeviceItem("No Devices", "", "false"));
        }

        Log.d("DEVICELIST", "DeviceList populated\n");

        mAdapter = new DeviceListAdapter(getActivity(), deviceItemList, bTAdapter);

        Log.d("DEVICELIST", "Adapter created\n");

    }

    public void pedirPermisos() {

        // Petición de permisos de localización, en caso de no haberlo aceptado anteriormente
        if(ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, requestCodePermission);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        // Check which request we're responding to
        if (requestCode == requestCodePermission) {

            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale( permission );
                    if (! showRationale) {
                        new android.app.AlertDialog.Builder(getActivity())
                            .setTitle("Información importante")
                            .setMessage("Debe aceptar los permisos para el correcto funcionamiento de la aplicación, si ya los ha rechazado antes vaya a Ajustes")
                            .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(), "Vaya a Ajustes para aceptar los permisos", Toast.LENGTH_LONG).show();
                                    System.exit(0);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                        /*.setNeutralButton("Ir a Ajustes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: Crear diferente URI dependiendo del Fork de Android
                                // Ejemplo: para MIUI 7:
                                *//*Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                intent.putExtra("extra_pkgname", context.getPackageName());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*//*

                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("com.aingerusanchez.healthybeat", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                getActivity().startActivity(intent);
                        }
                    })*/
                    } else if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                        new android.app.AlertDialog.Builder(getActivity())
                            .setTitle("Información importante")
                            .setMessage("Debe aceptar los permisos para el correcto funcionamiento de la aplicación")
                            .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    pedirPermisos();
                                }
                            })
                            .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deviceitem_list, container, false);
        ToggleButton scan = (ToggleButton) view.findViewById(R.id.scan);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                // Variables del Toast
                Context context = getContext();
                String tstScanText = "";
                int duracion = Toast.LENGTH_SHORT;
                Toast tstScan;

                if (isChecked) {
                    // TODO: dejar de escanear al transcurrir 30 segundos
                    mAdapter.clear();
                    getActivity().registerReceiver(bReciever, filter);
                    bTAdapter.startDiscovery();
                    tstScanText = "Buscando dispositivos Bluetooth";
                    tstScan = Toast.makeText(context, tstScanText, duracion);
                    tstScan.show();
                } else {
                    getActivity().unregisterReceiver(bReciever);
                    bTAdapter.cancelDiscovery();
                    tstScanText = "Parando busqueda";
                    tstScan = Toast.makeText(context, tstScanText, duracion);
                    tstScan.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d("DEVICELIST", "onItemClick position: " + position +
                " id: " + id + " name: " + deviceItemList.get(position).getDeviceName() + "\n");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(deviceItemList.get(position).getDeviceName());
            // Vincular con el dispositivo pulsado
            BluetoothDevice btDevice = bTAdapter.getRemoteDevice(deviceItemList.get(position).getAddress());
            btDevice.createBond();
            // Detener el escanear una vez vinculado a un dispositivo
            getActivity().unregisterReceiver(bReciever);
            bTAdapter.cancelDiscovery();
        }

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public int getNavigationMenuItemId() {
        return navigationMenuItemId;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Reimplementar OnFragmentInteractionListener para que la clase Bluetooth reaccione a las pulsaciones del Menú de navegación
        public void onFragmentInteraction(String id);
    }

}
