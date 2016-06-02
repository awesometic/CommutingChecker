package com.example.taek.commutingchecker;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

/**
 * Created by Taek on 2016-04-15.
 */
public class SocketIO {
    //com.github.nkzawa.socketio.client.Socket mSocket;
    //io.socket.client.Socket mSocket;
    io.socket.client.Socket mSocket;

    // 생성자
    public SocketIO(){
        try{
            mSocket = IO.socket(Constants.SERVER_URL);
        }catch (URISyntaxException e){
            e.printStackTrace();
            Log.d("connectSocket", "Error");
        }
    }

    // 소켓 연결
    public void connect(){
        mSocket.connect();
    }

    // 이벤트 보내기
    public void sendEvent(Map<String, String> data) {
        JSONObject obj = new JSONObject();
        try {
            if (mSocket.connected()) {
                Log.d("Socket", "connected");
                /*
                obj.put("DeviceAddress", data.get("DeviceAddress"));
                obj.put("UUID", data.get("UUID"));
                obj.put("Major", data.get("Major"));
                obj.put("Minor", data.get("Minor"));
                obj.put("SmartphoneAddress", data.get("SmartphoneAddress"));
                obj.put("Datetime", data.get("Datetime"));
                mSocket.emit("call", obj);
                Log.d("sendSocketData", "true");*/

                obj.put("BeaconDeviceAddress1", "00:1A:7D:DA:71:07");
                obj.put("BeaconDeviceAddress2", "00:1A:7D:DA:71:03");
                obj.put("BeaconDeviceAddress3", "B8:27:EB:E1:47:EB");
                /*
                Gateway 1 (pi2): f0 f4 c1 76 a6 23 42 ef ac 3a 66 f2 1a 11 99 3e 00 02 00 01
Gateway 2 (pi2): bf 0c c4 a4 eb 9f 4f 06 b7 16 1f 5f f4 9a 8f 47 00 02 00 02
Gateway 3 (pi3): 70 9b d6 40 42 d1 4b 1a 99 0a 36 d4 a1 e5 27 d8 00 03 00 01
Gateway 4 (pi3): b1 2a 7a b6 d0 12 49 92 88 09 43 4d d1 34 30 19 00 03 00 02
                 */
                obj.put("BeaconData1", "f0 f4 c1 76 a6 23 42 ef ac 3a 66 f2 1a 11 99 3e 00 02 00 01");
                obj.put("BeaconData2", "bf 0c c4 a4 eb 9f 4f 06 b7 16 1f 5f f4 9a 8f 47 00 02 00 02");
                obj.put("BeaconData3", "70 9b d6 40 42 d1 4b 1a 99 0a 36 d4 a1 e5 27 d8 00 03 00 01");
                obj.put("SmartphoneAddress", BLEScanService.myMacAddress);
                obj.put("DateTime", CurrentTime.currentTime());
                mSocket.emit("circumstance", obj);
                Log.d("sendSocketData", "true");

                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "서버에 데이터 전송", "서버에 데이터를 전송하였습니다.", "");
                //MainActivity.sendData = true;

                this.close();
            } else {
                Log.d("sendSocketData", "false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("sendEventError(JSON)", e.getMessage());
        }
    }

    // More than api ver.19
    @SuppressLint("NewApi")
    public void requestEssentialData(){
        JSONObject obj = new JSONObject();
        try{
            if(mSocket.connected()){
                /*
                requestEssentialData
                {
                     SmartphoneAddress: '00:00:00:00:00:00',
                     DateTime: '0000/00/00 00:00:00'
                }
                 */
                obj.put("SmartphoneAddress", BLEScanService.myMacAddress);
                obj.put("DateTime", CurrentTime.currentTime());
                mSocket.emit("requestEssentialData", obj);
                Log.d("requestEssentialData", "true");

                mSocket.on("data", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            final JSONArray jarray = new JSONArray(args[0].toString());
                            Log.d("request", args[0].toString());
                            for(int i = 0; i < jarray.length(); i++){
                                JSONObject obj_listen = jarray.getJSONObject(i);
                                String str = obj_listen.getString("beacon_address");
                                String[] str_arr = str.split("-");
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("id_workplace", obj_listen.getString("id_workplace"));
                                map.put("coordinateX", obj_listen.getString("coordinateX"));
                                map.put("coordinateY", obj_listen.getString("coordinateY"));
                                map.put("coordinateZ", obj_listen.getString("coordinateZ"));
                                map.put("beacon_address1", str_arr[0]);
                                map.put("beacon_address2", str_arr[1]);
                                map.put("beacon_address3", str_arr[2]);
                                BLEScanService.EssentialDataArray.add(map);
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Receive Request_data", "fail");
                        }
                    }
                });
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    // 소켓 닫기
    public void close(){
        mSocket.close();
    }
}