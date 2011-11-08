// Initialization

var Pusher = require('com.pusher');
Pusher.setup({
  key: '437c8bf5d9d5529460e9',     // CHANGEME
  appID: '1305',                   // CHANGEME
  secret: '0750515631c6e8300b03',  // CHANGEME
  reconnectAutomaticaly: true
});

var window = Ti.UI.createWindow({
	backgroundColor:'white',
  title: 'Pusher'
});
window.open()

// Handlers
var handleConnected = function() {
  Pusher.addEventListener('connected', function(e) {
    Ti.API.warn("PUSHER CONNECTED");

    // Connect to channel
    window.channel = Pusher.subscribeChannel('test');

    // Bind to all events on this channel
    window.channel.addEventListener('event', handleEvent);

    // Bind to a specific event on this channel
    window.channel.addEventListener('alert', handleAlertEvent);
  });
  Pusher.connect();
};

var handleDisconnected = function() {
  if(window.channel) {
    window.channel.removeEventListener('event', handleEvent);
    window.channel.removeEventListener('alert', handleAlertEvent);
  }

  Pusher.disconnect();
}

var handleEvent = function(e) {
  Ti.API.warn("ATAO 2");

  var label = Ti.UI.createLabel({
    text: "channel:" + e.channel + " event: " + e.name,
    top: 3,
    left: 10,
    height: '23',
    font: {fontSize: 20}
  });

  var sublabel = Ti.UI.createLabel({
    text: JSON.stringify(e.data),
    top: 25,
    left: 10,
    height: '15',
    font: {fontSize:12}
  });

  var tableViewRow = Ti.UI.createTableViewRow({});
  tableViewRow.add(label);
  tableViewRow.add(sublabel);

  Ti.API.warn("ATAO 2");

  tableview.appendRow(tableViewRow, {animated:true});
};

var handleAlertEvent = function(e) {
  alert(JSON.stringify(e.data));
}

var menu;
var CONNECT = 1, DISCONNECT = 2, ADD = 3;
Ti.Android.currentActivity.onCreateOptionsMenu = function(e) {
  menu = e.menu;
  var connect = menu.add({title:'Connect', itemId:CONNECT});
  connect.addEventListener('click', handleConnected);

  var disconnect = menu.add({title:'Disconnect', itemId:DISCONNECT});
  disconnect.addEventListener('click', handleDisconnected);

  var add = menu.add({title:'Add', itemId:ADD});
  add.addEventListener('click', function() {
    var new_window = Ti.UI.createWindow({
      url: 'channel.js',
      backgroundColor: 'white',
      title: 'Send event to channel',
      fullscreen: true
    });
    new_window.pusher = Pusher;
    new_window.open({animated:true});
  });
}

var tableview = Ti.UI.createTableView({
  data: [],
  headerTitle: 'Send events to the test channel'
});
window.add(tableview);

