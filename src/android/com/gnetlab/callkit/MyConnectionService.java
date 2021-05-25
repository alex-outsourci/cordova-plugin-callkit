package com.gnetlab.callkit;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;
import android.os.Handler;
import android.net.Uri;

import java.util.ArrayList;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class MyConnectionService extends ConnectionService {
    private static final String TAG = "MyConnectionService";
    private static final ArrayList<Connection> connections = new ArrayList<Connection>();
    private static Connection conn;

    public static Connection getConnection(String id) {
        for (final Connection item : connections) {
            String connId = item.getExtras().getString("id");
            if (connId != null && connId.equals(id)) {
                return item;
            }
        }

        return null;
    }

    public static Connection getConnectionOrActive(String id) {
        return id != null ? getConnection(id) : getActiveConnection();
    }

    public static void setActiveConnection(String id) {
        if (id == null) {
            conn = null;
        } else {
            conn = getConnection(id);
        }
    }

    public static Connection getActiveConnection() {
        return conn;
    }

    public static void closeActiveConnection() {
        if (conn != null) {
            String connId = conn.getExtras().getString("id");
            closeConnection(connId);
        }
    }

    public static void closeConnection(String id) {
        Connection item = getConnection(id);
        if (item != null) {
            connections.remove(item);
            if (conn == item) {
                conn = null;
            }
        }
    }

    public static JSONObject getConnectionResult(String connId) {
        JSONObject data = new JSONObject();
        try {
            data.put("callId", connId);
            return data;
        } catch (JSONException e) {
            return data;
        }
    }

    @Override
    public Connection onCreateIncomingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        final Connection connection = new Connection() {
            @Override
            public void onAnswer() {
                this.setActive();

                final String connId = this.getExtras().getString("id");
                setActiveConnection(connId);

                Intent intent = new Intent(CordovaCall.getCordova().getActivity().getApplicationContext(), CordovaCall.getCordova().getActivity().getClass());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                CordovaCall.getCordova().getActivity().getApplicationContext().startActivity(intent);
                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("answer");
                for (final CallbackContext callbackContext : callbackContexts) {
                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                        public void run() {
                            JSONObject data = getConnectionResult(connId);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    });
                }
            }

            @Override
            public void onReject() {
                final String connId = this.getExtras().getString("id");
                closeConnection(connId);

                DisconnectCause cause = new DisconnectCause(DisconnectCause.REJECTED);
                this.setDisconnected(cause);
                this.destroy();

                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("reject");
                for (final CallbackContext callbackContext : callbackContexts) {
                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                        public void run() {
                            JSONObject data = getConnectionResult(connId);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    });
                }
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {
                final String connId = this.getExtras().getString("id");
                closeConnection(connId);

                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();

                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("hangup");
                for (final CallbackContext callbackContext : callbackContexts) {
                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                        public void run() {
                            JSONObject data = getConnectionResult(connId);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    });
                }
            }
        };

        connection.setAddress(Uri.parse(request.getExtras().getString("from")), TelecomManager.PRESENTATION_ALLOWED);

        // Set connection custom values
        final String connId = request.getExtras().getString("id");
        Bundle connBundle = new Bundle();
        connBundle.putString("id", connId);
        connection.putExtras(connBundle);

        Log.v(TAG, "onCreateIncomingConnection: " + connId);

        Icon icon = CordovaCall.getIcon();
        if (icon != null) {
            StatusHints statusHints = new StatusHints((CharSequence) "", icon, new Bundle());
            connection.setStatusHints(statusHints);
        }

        // add to connections
        connections.add(connection);

        // select as active if need
        if (conn == null) {
            setActiveConnection(connId);
        }

        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("receiveCall");
        for (final CallbackContext callbackContext : callbackContexts) {
            CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                public void run() {
                    JSONObject data = getConnectionResult(connId);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                }
            });
        }
        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        final Connection connection = new Connection() {
            @Override
            public void onAnswer() {
                super.onAnswer();
            }

            @Override
            public void onReject() {
                super.onReject();
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {

                final String connId = this.getExtras().getString("id");
                closeConnection(connId);

                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();

                ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("hangup");
                for (final CallbackContext callbackContext : callbackContexts) {
                    CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                        public void run() {
                            JSONObject data = getConnectionResult(connId);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    });
                }
            }

            @Override
            public void onStateChanged(int state) {
                if (state == Connection.STATE_DIALING) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(CordovaCall.getCordova().getActivity().getApplicationContext(), CordovaCall.getCordova().getActivity().getClass());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            CordovaCall.getCordova().getActivity().getApplicationContext().startActivity(intent);
                        }
                    }, 500);
                }
            }
        };

        connection.setAddress(Uri.parse(request.getExtras().getString("to")), TelecomManager.PRESENTATION_ALLOWED);

        // Set connection custom values
        final String connId = request.getExtras().getString("id");
        Bundle connBundle = new Bundle();
        connBundle.putString("id", connId);
        connection.putExtras(connBundle);

        Log.v(TAG, "onCreateOutgoingConnection: " + connId);

        Icon icon = CordovaCall.getIcon();
        if (icon != null) {
            StatusHints statusHints = new StatusHints((CharSequence) "", icon, new Bundle());
            connection.setStatusHints(statusHints);
        }

        // Could setActive();
        connection.setDialing();

        // add to connections
        connections.add(connection);

        // select as active if need
        if (conn == null) {
            setActiveConnection(connId);
        }

        ArrayList<CallbackContext> callbackContexts = CordovaCall.getCallbackContexts().get("sendCall");
        if (callbackContexts != null) {
            for (final CallbackContext callbackContext : callbackContexts) {
                CordovaCall.getCordova().getThreadPool().execute(new Runnable() {
                    public void run() {
                        JSONObject data = getConnectionResult(connId);
                        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    }
                });
            }
        }
        return connection;
    }

    public Connection onCreateUnknownConnection(PhoneAccountHandle connectionManagerPhoneAccount,
                                                ConnectionRequest request) {
        final String connId = request.getExtras().getString("id");
        Log.v(TAG, "Create unknown call: " + connId);
        return null;
    }
}
