package qz;

import com.intel.bluetooth.BluetoothConsts;

import javax.bluetooth.*;
import javax.bluetooth.UUID;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class BluetoothUtilities {

    /**
     * Returns a CSV format of printer names, convenient for JavaScript
     * @return
     */
    public static String getPrinterListing() throws IOException {
        String printerListing = "";
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        RemoteDevice[] remoteDevices = localDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
        for (int i = 0; i < remoteDevices.length; i++) {
            RemoteDevice remoteDevice = remoteDevices[i];
            String deviceName = remoteDevice.getFriendlyName(false).trim();
            printerListing  = printerListing  + deviceName;
            if (i != (remoteDevices.length - 1)) {
                printerListing  = printerListing  + ",";
            }
            System.out.print("Device Name ::: " + deviceName);
            System.out.println(" , Bluetooth Address ::: " + remoteDevice.getBluetoothAddress());
        }
        //System.out.println("generateBluetoothString() \n" + generateBluetoothString() );
        return printerListing;
    }


    public static HashMap<String, RemoteDevice> refreshBluetoothDevices() throws IOException, InterruptedException {
        HashMap<String, RemoteDevice> bluetoothPrinters = new HashMap<>();
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        RemoteDevice[] remoteDevices = localDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
        for (RemoteDevice btDevice : remoteDevices) {
            String deviceName = btDevice.getFriendlyName(false).trim();
            bluetoothPrinters.put(deviceName, btDevice);
            System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
            System.out.println("Bluetooth Printer added to list: " + deviceName);
            System.out.println("     isTrustedDevice =  " + btDevice.isTrustedDevice());
            System.out.println("     isEncrypted =  " + btDevice.isEncrypted());
            System.out.println("     isAuthenticated =  " + btDevice.isAuthenticated());
        }
        return bluetoothPrinters;
    }

    public static HashMap<String, RemoteDevice> discoveryBluetoothDevices() throws IOException, InterruptedException {

        final HashMap<String, RemoteDevice> bluetoothPrinters = new HashMap<>();
        final Object inquiryCompletedEvent = new Object();
        //bluetoothPrinters.clear();

        DiscoveryListener listener = new DiscoveryListener() {
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass deviceClass) {
                boolean deviceIsPrinter = false;
                int majDeviceCl = deviceClass.getMajorDeviceClass(),
                        deviceCl = deviceClass.getMinorDeviceClass();
                if (majDeviceCl == BluetoothConsts.DeviceClassConsts.MAJOR_IMAGING && ((deviceCl & BluetoothConsts.DeviceClassConsts.IMAGING_MINOR_PRINTER_MASK) != 0)) {
                    deviceIsPrinter = true;
                }
                if (deviceIsPrinter) {
                    System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                    try {
                        String deviceName = btDevice.getFriendlyName(false).trim();
                        System.out.println("Bluetooth Printer added to list: " + deviceName);
                        System.out.println("     info " + BluetoothConsts.DeviceClassConsts.toString(deviceClass));
                        bluetoothPrinters.put(deviceName, btDevice);
                    } catch (IOException cantGetDeviceName) {
                    }
                }
            }
            public void inquiryCompleted(int discType) {
                System.out.println("Device Inquiry completed!");
                synchronized(inquiryCompletedEvent){
                    inquiryCompletedEvent.notifyAll();
                }
            }
            public void serviceSearchCompleted(int transID, int respCode) {
            }
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            }
        };

        synchronized(inquiryCompletedEvent) {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
            if (started) {
                System.out.println("wait for device inquiry to complete...");
                inquiryCompletedEvent.wait();
                System.out.println(bluetoothPrinters.size() +  " device(s) found");
            }
        }
        return bluetoothPrinters;
    }



    /**
     * Assembles a text string containing the device's bluetooth information
     *
     * @return String containing the device's bluetooth information
     */
    public static String generateBluetoothString() {
        LocalDevice lc;
        try {
            // Get the LocalDevice
            lc = LocalDevice.getLocalDevice();
        } catch (final Exception ex) {
            return "Failed to initialize Bluetooth";
        }
        final StringBuffer sb = new StringBuffer();
        // Get the device's Bluetooth address
        sb.append("Bluetooth Address: ");
        sb.append(lc.getBluetoothAddress());
        sb.append('\n');
        // Get the device's Bluetooth friendly name
        sb.append("Bluetooth friendly name: ");
        sb.append(lc.getFriendlyName());
        sb.append('\n');
        // Get the device's discovery mode
        sb.append("Discovery Mode: ");
        switch(lc.getDiscoverable()) {
            case DiscoveryAgent.GIAC:
                sb.append("General/Unlimited Inquiry Access");
                break;
            case DiscoveryAgent.LIAC:
                sb.append("Limited Dedicated Inquiry Access");
                break;
            case DiscoveryAgent.NOT_DISCOVERABLE:
                sb.append("Not discoverable");
                break;
            default:
                sb.append("Unknown");
                break;
        }
        sb.append('\n');
        // Get the Bluetooth API version
        sb.append("API Version: ");
        sb.append(LocalDevice.getProperty("bluetooth.api.version"));
        sb.append('\n');
        // Get the Bluetooth master switch setting
        sb.append("Master/Slave Switch Allowed: ");
        sb.append(LocalDevice.getProperty("bluetooth.master.switch"));
        sb.append('\n');
        // Get the maximum number of service attributes per second
        sb.append("Max number of service attributes retrieved per record: ");
        sb.append(LocalDevice.getProperty("bluetooth.sd.attr.retrievable.max"));
        sb.append('\n');
        // Get the maximum number of connected devices
        sb.append("Max number of supported connected devices at one time: ");
        sb.append(LocalDevice.getProperty("bluetooth.connected.devices.max"));
        sb.append('\n');
        // Get the maximum receiveMTU size
        sb.append("Max receiveMTU size in bytes supported in L2CAP: ");
        sb.append(LocalDevice.getProperty("bluetooth.l2cap.receiveMTU.max"));
        sb.append('\n');
        // Get the maximum number of concurrent service discovery transactions
        sb.append("Maximum number of concurrent service discovery transactions: ");
        sb.append(LocalDevice.getProperty("bluetooth.sd.trans.max"));
        sb.append('\n');
        // Inquiry scanning allowed during connection setting
        sb.append("Inquiry scanning allowed during connection: ");
        sb.append(LocalDevice.getProperty("bluetooth.connected.inquiry.scan"));
        sb.append('\n');
        // Page scanning allowed during connection setting
        sb.append("Page scanning allowed during connection: ");
        sb.append(LocalDevice.getProperty("bluetooth.connected.page.scan"));
        sb.append('\n');
        // Inquiry allowed during a connection
        sb.append("Inquiry allowed during a connection: ");
        sb.append(LocalDevice.getProperty("bluetooth.connected.inquiry"));
        sb.append('\n');
        // Get the paging allowed during a connection setting
        sb.append("Paging allowed during a connection: ");
        sb.append(LocalDevice.getProperty("bluetooth.connected.page"));
        sb.append('\n');
        // Return the string with the Bluetooth info
        return sb.toString();
    }

}
