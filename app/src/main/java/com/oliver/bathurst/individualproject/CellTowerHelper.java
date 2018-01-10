package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Oliver on 26/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class CellTowerHelper {
    private final Context c;
    private TelephonyManager tel;

    CellTowerHelper(Context context) {
        this.c = context;
    }

    @SuppressWarnings("unused")
    String getAll() {
        tel = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        String result;
        if (tel != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    result = allTowers(tel.getAllCellInfo());
                }else{
                    result = c.getString(R.string.perform_permissions_checkup);
                }
            }else{
                result = c.getString(R.string.build_number_low);
            }
        }else{
            result = c.getString(R.string.no_phone);
        }
        return result;
    }

    private String allTowers(List<CellInfo> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getString(R.string.cell_tower_info)).append("\n");
        for (CellInfo cell : list) {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cell instanceof CellInfoGsm) {
                    CellIdentityGsm identity = ((CellInfoGsm) cell).getCellIdentity();
                    sb.append(c.getString(R.string.typeCell)).append(c.getString(R.string.gcm))
                            .append(c.getString(R.string.CID)).append(identity.getCid()).append("\n")
                            .append(c.getString(R.string.lac)).append(identity.getLac()).append("\n")
                            .append(c.getString(R.string.MCC)).append(identity.getMcc()).append("\n")
                            .append(c.getString(R.string.MNC)).append(identity.getMnc()).append("\n");

                    sb.append(validateDoubles(callOpenCell(identity.getCid(), identity.getLac(), identity.getMcc(), identity.getMnc())));

                } else if (cell instanceof CellInfoWcdma && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    CellIdentityWcdma identityWcdma = ((CellInfoWcdma) cell).getCellIdentity();
                    sb.append(c.getString(R.string.typeCell)).append(c.getString(R.string.wcdma))
                            .append(c.getString(R.string.CID)).append(identityWcdma.getCid()).append("\n")
                            .append(c.getString(R.string.lac)).append(identityWcdma.getLac()).append("\n")
                            .append(c.getString(R.string.MCC)).append(identityWcdma.getMcc()).append("\n")
                            .append(c.getString(R.string.MNC)).append(identityWcdma.getMnc()).append("\n");

                    sb.append(validateDoubles(callOpenCell(identityWcdma.getCid(), identityWcdma.getLac(), identityWcdma.getMcc(), identityWcdma.getMnc())));


                } else if (cell instanceof CellInfoLte) {
                    CellIdentityLte identityLte = ((CellInfoLte) cell).getCellIdentity();
                    sb.append(c.getString(R.string.typeCell)).append(c.getString(R.string.lte))
                            .append(c.getString(R.string.CID)).append(identityLte.getCi()).append("\n")
                            .append(c.getString(R.string.lac)).append(identityLte.getTac()).append("\n")
                            .append(c.getString(R.string.MCC)).append(identityLte.getMcc()).append("\n")
                            .append(c.getString(R.string.MNC)).append(identityLte.getMnc()).append("\n");

                    sb.append(validateDoubles(callOpenCell(identityLte.getCi(), identityLte.getTac(), identityLte.getMcc(), identityLte.getMnc())));


                } else if (cell instanceof CellInfoCdma) {
                    CellIdentityCdma identityCdma = ((CellInfoCdma) cell).getCellIdentity();
                    sb.append(c.getString(R.string.cdma)).append("\n")
                            .append(c.getString(R.string.basestation_id)).append(identityCdma.getBasestationId()).append("\n")
                            .append(c.getString(R.string.network_id)).append(identityCdma.getNetworkId()).append("\n")
                            .append(c.getString(R.string.system_id)).append(identityCdma.getSystemId()).append("\n")
                            .append(c.getString(R.string.latitude)).append(identityCdma.getLatitude()).append("\n")
                            .append(c.getString(R.string.longitude)).append(identityCdma.getLongitude()).append("\n");
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<NeighboringCellInfo> neighboringCells = tel.getNeighboringCellInfo();
            if(!neighboringCells.isEmpty()){
                for (NeighboringCellInfo neighboringCellInfo : neighboringCells) {
                    sb.append(c.getString(R.string.neighbour)).append("\n")
                            .append(c.getString(R.string.CID)).append(neighboringCellInfo.getCid()).append("\n")
                            .append(c.getString(R.string.LAC)).append(neighboringCellInfo.getLac()).append("\n")
                            .append(c.getString(R.string.signalStrength)).append(neighboringCellInfo.getRssi()).append("\n")
                            .append(c.getString(R.string.typeCell)).append(neighboringCellInfo.getNetworkType()).append("\n\n");
                }
            }
        }
        return sb.toString();
    }
    private String validateDoubles(Double[] dubs){
        String returnStr = "";
        if(dubs != null && dubs[0] != null && dubs[1] != null && dubs[2] != null) {
            returnStr += (c.getString(R.string.latitude) + dubs[0] + "\n" + c.getString(R.string.longitude) + dubs[1] + "\n" + c.getString(R.string.range) + dubs[2] + "\n");
        }
        return returnStr;
    }
    Double[] callOpenCell(final int cid, final int lac, final int mcc, final int mnc) {
        Double[] arrayInit = null;
        try {
            arrayInit = new getResult().execute(new int[]{cid, lac, mcc, mnc}).get();
        }catch(Exception ignored){}
        return arrayInit;
    }
    private static class getResult extends AsyncTask<int[], Void, Double[]> {
        @Override
        protected Double[] doInBackground(int[]... integers) {
            int[] data = integers[0];
            Double[] doubleArr = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://www.opencellid.org/cell/get?key=978e483439b03f" + "&mcc=" + data[2] + "&mnc=" + data[3] + "&cellid=" + data[0] + "&lac=" + data[1]).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/xml");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(connection.getInputStream());
                doc.normalize();
                NodeList nodeList = doc.getElementsByTagName("cell");
                if (nodeList.getLength() >= 1) {
                    NamedNodeMap attributes = nodeList.item(0).getAttributes();
                    doubleArr = new Double[]{Double.parseDouble(attributes.getNamedItem("lat").getNodeValue()), Double.parseDouble(attributes.getNamedItem("lon").getNodeValue()), Double.parseDouble(attributes.getNamedItem("range").getNodeValue())};
                }
            } catch (Exception ignored) {}
            return doubleArr;
        }
    }
}
