var exec = require('cordova/exec');

exports.setAppName = function(appName, success, error) {
    exec(success, error, "CordovaCall", "setAppName", [appName]);
};

// TODO: add call id to be able manage exact connection

exports.setIcon = function(iconName, success, error) {
    exec(success, error, "CordovaCall", "setIcon", [iconName]);
};

exports.setRingtone = function(ringtoneName, success, error) {
    exec(success, error, "CordovaCall", "setRingtone", [ringtoneName]);
};

exports.setIncludeInRecents = function(value, success, error) {
    if(typeof value == "boolean") {
      exec(success, error, "CordovaCall", "setIncludeInRecents", [value]);
    } else {
      error("Value Must Be True Or False");
    }
};

exports.setDTMFState = function(value, success, error) {
    if(typeof value == "boolean") {
      exec(success, error, "CordovaCall", "setDTMFState", [value]);
    } else {
      error("Value Must Be True Or False");
    }
};

exports.setVideo = function(value, success, error) {
    if(typeof value == "boolean") {
      exec(success, error, "CordovaCall", "setVideo", [value]);
    } else {
      error("Value Must Be True Or False");
    }
};

// done

exports.receiveCall = function(from, id, success, error) {
    if(typeof id == "function") {
      error = success;
      success = id;
      id = null;
    } else if(id) {
      id = id.toString();
    }
    exec(success, error, "CordovaCall", "receiveCall", [from, id]);
};

exports.sendCall = function(to, id, success, error) {
    if(typeof id == "function") {
      error = success;
      success = id;
      id = null;
    } else if(id) {
      id = id.toString();
    }
    exec(success, error, "CordovaCall", "sendCall", [to, id]);
};

exports.connectCall = function(id, success, error) {
    if(typeof id == "function") {
        id = null;
    }
    exec(success, error, "CordovaCall", "connectCall", [id]);
};

exports.endCall = function(id, success, error) {
    if(typeof id == "function") {
        id = null;
    }

    exec(success, error, "CordovaCall", "endCall", [id]);
};

exports.callNumber = function(to, success, error) {
    exec(success, error, "CordovaCall", "callNumber", [to]);
};

exports.on = function(e, f) {
    var success = function(message) {
      f(message);
    };
    var error = function() {
    };
    exec(success, error, "CordovaCall", "registerEvent", [e]);
};

// Audio API

exports.getAudioModes = function(success, error) {
    exec(success, error, "CordovaCall", "getAudioModes", []);
}

exports.setAudioMode = function(mode, success, error) {
    exec(success, error, "CordovaCall", "setAudioMode", [mode]);
}

exports.getAudioMode = function(mode, success, error) {
    exec(success, error, "CordovaCall", "getAudioMode", []);
}