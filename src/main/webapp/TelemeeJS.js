/*
 * #%L
 * TelemeeJS
 * %%
 * Copyright (C) 2013 Thomas Struller-Baumann
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

'use strict';

var TelemeeJS = (function(serverURI) {
   var DEFAULT_BASE_URI = 'http://localhost:8080/';
   var baseURI;

   var logLevel;
   var OFF = 999;
   var SEVERE = 600;
   var WARNING = 500;
   var INFO = 400;
   var FINE = 300;
   var FINER = 200;
   var FINEST = 100;
   var ALL = 0;

   var currentApp;
   var currentChannel;
   var currentChannelAttribute;
   var currentLogEntry;
   var telemeeApps = new Array();
   var channels = new Array();
   var channelAttributes = new Array();
   var logEntries = new Array();

   init();

   function init() {
      if (serverURI) {
         baseURI = serverURI;
      } else {
         baseURI = DEFAULT_BASE_URI;
      }
      if (!testConnectionToServer()) {
         var error = "FATAL - Can't connect to telemeeserver (" + baseURI + "telemee). Is the server running?";
         window.console.log(error);
         alert(error);
      }
      loadDataFromServer();
   }

   function testConnectionToServer() {
      var success = false;
      var telemeeAPI = baseURI + 'telemee/resources/monitor/alive';
      $.ajax({url: telemeeAPI,
         type: "HEAD",
         async: false,
         statusCode: {
            200: function(response) {
               success = true;
            },
            400: function(response) {
            },
            0: function(response) {
            }
         }
      });
      return success;
   }

   function loadDataFromServer() {
      // Load Apps, Channels, Attributes from Server
      $.when(getTelemeeApps()).done(function(apps) {
         telemeeApps = apps;
         $.when(getChannels()).done(function(loadedChannels) {
            channels = loadedChannels;
            $.when(getChannelAttributes()).done(function(attr) {
               channelAttributes = attr;
            });
         });
      });
      // Clean Channels Array in Apps
      telemeeApps.forEach(function(telemeeApp) {
         telemeeApp.channels = ensureArray(telemeeApp.channels);
      });
      // Clean ChannelAttributes Array in Channels
      channels.forEach(function(channel) {
         channel.channelAttributes = ensureArray(channel.channelAttributes);
      });
   }

   function ensureArray(arrayOrOneElement) {
      if ($.isArray(arrayOrOneElement)) {
         return arrayOrOneElement;
      }
      return new Array().concat(arrayOrOneElement);
   }

   function getTelemeeApps() {
      var telemeeAPI = baseURI + 'telemee/resources/telemeeapps';
      var telemeeAppsArray = new Array();
      $.ajax({
         type: "GET",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            200: function(json) {
               if (json) {
                  telemeeAppsArray = ensureArray(json.telemeeApp);
               }
            }
         }
      });
      return telemeeAppsArray;
   }

   function getChannels() {
      var telemeeAPI = baseURI + 'telemee/resources/channels';
      var channelsArray = new Array();
      $.ajax({
         type: "GET",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            200: function(json) {
               if (json) {
                  channelsArray = ensureArray(json.channel);
               }
            }
         }
      });
      return channelsArray;
   }

   function getChannelAttributes() {
      var telemeeAPI = baseURI + 'telemee/resources/channelattributes';
      var channelAttributesArray = new Array();
      $.ajax({
         type: "GET",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            200: function(json) {
               if (json) {
                  channelAttributesArray = ensureArray(json.channelAttribute);
               }
            }
         }
      });
      return channelAttributesArray;
   }

   function getLogEntries(channel) {
      var telemeeAPI = baseURI + 'telemee/resources/logentries?channelID=' + channel.id;
      var logEntries = new Array();
      $.ajax({
         type: "GET",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            200: function(json) {
               if (json) {
                  logEntries = ensureArray(json.logEntry);
               }
            }
         }
      });
      return logEntries;
   }

   function createTelemeeApp(newTelemeeApp) {
      var def = $.Deferred();
      if (newTelemeeApp.id < 0) {   //is it already persisted?
         var telemeeAPI = baseURI + 'telemee/resources/telemeeapps';
         $.ajax({
            type: "POST",
            async: false,
            url: telemeeAPI,
            contentType: "application/json; charset=utf-8",
            dataType: "application/json",
            data: JSON.stringify(newTelemeeApp.toJSON()),
            statusCode: {
               201: function(data) {
                  var jsonNewTelemeeApp = JSON.parse(data.responseText);
                  newTelemeeApp.id = jsonNewTelemeeApp.id;
                  def.resolve(newTelemeeApp);
               }
            }
         });
      } else {
         def.resolve(newTelemeeApp);
      }
      return def.promise();
   }

   function createChannel(newChannel) {
      var def = $.Deferred();
      if (newChannel.id < 0) {   //is it already persisted?
         var telemeeAPI = baseURI + 'telemee/resources/channels';
         $.ajax({
            type: "POST",
            async: false,
            url: telemeeAPI,
            contentType: "application/json; charset=utf-8",
            dataType: "application/json",
            data: JSON.stringify(newChannel.toJSON()),
            statusCode: {
               201: function(data) {
                  var jsonNewChannel = JSON.parse(data.responseText);
                  newChannel.id = jsonNewChannel.id;
                  def.resolve(newChannel);
               }
            }
         });
      } else {
         def.resolve(newChannel);
      }
      return def.promise();
   }

   function createChannelAttribute(newChannelAttribute) {
      if (newChannelAttribute.id < 0) {   //is it already persisted?
         var telemeeAPI = baseURI + 'telemee/resources/channelattributes';
         $.ajax({
            type: "POST",
            async: false,
            url: telemeeAPI,
            contentType: "application/json; charset=utf-8",
            dataType: "application/json",
            data: JSON.stringify(newChannelAttribute.toJSON()),
            statusCode: {
               201: function(data) {
                  var jsonNewChannelAttribute = JSON.parse(data.responseText);
                  newChannelAttribute.id = jsonNewChannelAttribute.id;
               }
            }
         });
      }
      return newChannelAttribute;
   }

   function bindChannelToApp(channel, app) {
      var telemeeAPI = baseURI + 'telemee/resources/telemeeapps/' + app.id + "/channel/" + channel.id;
      $.ajax({
         type: "POST",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            500: function(data) {
               throw "Couldn't add ChannelID=" + channel.id + " to telemeeAppID=" + app.id;
            }
         }
      });
   }

   function bindChannelAttributeToChannel(channelAttribute, channel) {
      var telemeeAPI = baseURI + 'telemee/resources/channels/' + channel.id + "/channelattribute/" + channelAttribute.id;
      $.ajax({
         type: "POST",
         async: false,
         url: telemeeAPI,
         contentType: "application/json; charset=utf-8",
         statusCode: {
            500: function(data) {
               throw "Couldn't add ChannelAttributeID=" + channelAttribute.id + " to channelID=" + channel.id;
            }
         }
      });
   }

   function createLogEntry(newLogEntry) {
      var deferredCreateLogEntry = $.Deferred();
      if (newLogEntry.logLevel < TelemeeJS.logLevel) {
         deferredCreateLogEntry.resolve(newLogEntry);
      } else {
         var telemeeAPI = baseURI + 'telemee/resources/logentries';
         $.ajax({
            type: "POST",
            async: false,
            url: telemeeAPI,
            contentType: "application/json; charset=utf-8",
            dataType: "application/json",
            data: JSON.stringify(newLogEntry, newLogEntry.getFilteredFields()),
            statusCode: {
               201: function(data) {
                  var jsonNewLogEntry = JSON.parse(data.responseText);
                  newLogEntry.id = jsonNewLogEntry.id;
                  deferredCreateLogEntry.resolve(newLogEntry);
               }
            }
         });
      }
      return deferredCreateLogEntry.promise();
   }

//#####################################################################
// TelemeeApp Entity
//#####################################################################
   var TelemeeApp = function(name) {
      this.id = -1;
      this.name = name;
      this.channels = new Array();
   };
   TelemeeApp.prototype.toJSON = function() {
      return {name: this.name};
   };
   function containsChannelWithName(app, channelName) {
      var contains = false;
      for (var i = 0; i < app.channels.length; i++) {
         if (app.channels[i] && app.channels[i].name === channelName) {
            contains = true;
            break;
         }
      }
      return contains;
   }

//#####################################################################
// Channel Entity
//#####################################################################
   var Channel = function(name) {
      this.id = -1;
      this.name = name;
      this.telemeeApps = new Array();
      this.boundedTelemeeApps = new Array();  //There are the channels if the channelAttribute is persisted bounded (on the server) to the channel
      this.channelAttributes = new Array();
   };
   Channel.prototype.toJSON = function() {
      return {name: this.name};
   };
   function containsChannelAttributeWithName(channel, channelAttributeName) {
      var contains = false;
      for (var i = 0; i < channel.channelAttributes.length; i++) {
         if (channel.channelAttributes[i] && channel.channelAttributes[i].name === channelAttributeName) {
            contains = true;
            break;
         }
      }
      return contains;
   }

//#####################################################################
// ChannelAttribute Entity
//#####################################################################
   var ChannelAttribute = function(name) {
      this.id = -1;
      this.name = name;
      this.channels = new Array();
      this.boundedChannels = new Array();  //There are the channels if the channelAttribute is persisted bounded (on the server) to the channel
   };
   ChannelAttribute.prototype.toJSON = function() {
      return {name: this.name};
   };

//#####################################################################
// LogEntry Entity
//#####################################################################
   var LogEntry = function(description, channel, level) {
      this.id = -1;
      this.logLevel = level;
      this.description = description;
      this.channel = channel;
      this.channelID;
      this.logValues = new Array();
   };
   LogEntry.prototype.addLogValue = function(value, channelAttribute) {
      var logValue = new LogValue(value, channelAttribute);
      this.logValues.push(logValue);
   };
   LogEntry.prototype.getFilteredFields = function() {
      return ['description', 'channelID', 'logValues', 'value', 'channelAttributeID'];
   };

//#####################################################################
// LogValue Entity
//#####################################################################
   var LogValue = function(value, channelAttribute) {
      this.id = -1;
      this.value = value;
      this.channelAttribute = channelAttribute;
      this.channelAttributeID = channelAttribute.id;
   };

   function forTelemeeApp(appName) {
      var telemeeApp;
      // is it already in the client?
      for (var i = 0; i < telemeeApps.length; i++) {
         if (telemeeApps[i].name === appName) {
            telemeeApp = telemeeApps[i];
         }
      }
      if (!telemeeApp) {
         // It's not yet in the client - create a new one
         telemeeApp = new TelemeeApp(appName);
         telemeeApps.push(telemeeApp);
      }
      currentApp = telemeeApp;
      return this;
   }

   function forChannel(channelName) {
      var channel;
      // is it already in the client?
      for (var i = 0; i < channels.length; i++) {
         if (channels[i].name === channelName) {
            channel = channels[i];
         }
      }
      if (!channel) {
         // It's not yet in the client - create a new one
         channel = new Channel(channelName);
         channels.push(channel);
      }
      channel.telemeeApp = currentApp;
      currentChannel = channel;
      // Only add channel to app if it's not already in the app
      if (!containsChannelWithName(currentApp, currentChannel.name)) {
         currentApp.channels.push(currentChannel);
      }
      return this;
   }

   function forChannelAttribute(channelAttributeName) {
      var channelAttribute;
      // is it already in the client?
      for (var i = 0; i < channelAttributes.length; i++) {
         if (channelAttributes[i].name === channelAttributeName) {
            channelAttribute = channelAttributes[i];
         }
      }
      if (!channelAttribute) {
         // It's not yet in the client - create a new one
         channelAttribute = new ChannelAttribute(channelAttributeName);
         channelAttributes.push(channelAttribute);
      }
      currentChannelAttribute = channelAttribute;
      // Only add channelAttribute to channel if it's not already in the channel
      if (!containsChannelAttributeWithName(currentChannel, channelAttributeName)) {
         currentChannel.channelAttributes.push(channelAttribute);
      }
      return this;
   }

   function startLogEntry(description, level) {
      var newLogEntry = new LogEntry(description, currentChannel, level);
      currentLogEntry = newLogEntry;
      return this;
   }

   function log(value) {
      if (!currentLogEntry) {
         window.console.log("Please define a LogEntry with 'newLogEntry()' before using log()");
      }
      currentLogEntry.addLogValue(value, currentChannelAttribute);
      return this;
   }

   function endLogEntry() {
      logEntries.push(currentLogEntry);
      return this;
   }

   function deleteTelemeeApp(telemeeAppName) {
      var telemeeApp;
      for (var i = 0; i < telemeeApps.length; i++) {
         var arrayApp = telemeeApps[i];
         if (arrayApp.name === telemeeAppName) {
            telemeeApp = arrayApp;
            break;
         }
      }
      if (telemeeApp) {
         if (telemeeApp.id > -1) {
            var telemeeAPI = baseURI + 'telemee/resources/telemeeapps/' + telemeeApp.id;
            $.ajax({
               type: "DELETE",
               async: false,
               url: telemeeAPI,
               contentType: "application/json; charset=utf-8",
               success: function(data) {
                  telemeeApps.splice($.inArray(telemeeApp, telemeeApps), 1);
                  telemeeApp = null;
               }
            });
         } else {
            telemeeApps.splice($.inArray(telemeeApp, telemeeApps), 1);
            telemeeApp = null;
         }
      }
   }

   function deleteChannel(channelName) {
      var channel;
      for (var i = 0; i < channels.length; i++) {
         var arrayChannel = channels[i];
         if (arrayChannel.name === channelName) {
            channel = arrayChannel;
            break;
         }
      }
      if (channel) {
         if (channel.id > -1) {
            var telemeeAPI = baseURI + 'telemee/resources/channels/' + channel.id;
            $.ajax({
               type: "DELETE",
               async: false,
               url: telemeeAPI,
               contentType: "application/json; charset=utf-8",
               success: function(data) {
                  channels.splice($.inArray(channel, channels), 1);
                  channel = null;
               }
            });
         } else {
            channels.splice($.inArray(channel, channels), 1);
            channel = null;
         }
      }
   }

   function deleteChannelAttribute(channelAttributeName) {
      var channelAttribute;
      for (var i = 0; i < channelAttributes.length; i++) {
         var arrayChannelAttribute = channelAttributes[i];
         if (arrayChannelAttribute.name === channelAttributeName) {
            channelAttribute = arrayChannelAttribute;
            break;
         }
      }
      if (channelAttribute) {
         if (channelAttribute.id > -1) {
            var telemeeAPI = baseURI + 'telemee/resources/channelattributes/' + channelAttribute.id;
            $.ajax({
               type: "DELETE",
               async: false,
               url: telemeeAPI,
               contentType: "application/json; charset=utf-8",
               success: function(data) {
                  channelAttributes.splice($.inArray(channelAttribute, channelAttributes), 1);
                  channelAttribute = null;
               }
            });
         } else {
            channelAttributes.splice($.inArray(channelAttribute, channelAttributes), 1);
            channelAttribute = null;
         }
      }
   }

   function deleteLogEntries(channelName) {
      var channel;
      for (var i = 0; i < channels.length; i++) {
         var arrayChannel = channels[i];
         if (arrayChannel.name === channelName) {
            channel = arrayChannel;
            break;
         }
      }

      if (channel) {
         if (channel.id > -1) {
            var telemeeAPI = baseURI + 'telemee/resources/logentries/deleteByChannelId/' + channel.id;
            $.ajax({
               type: "DELETE",
               async: false,
               url: telemeeAPI,
               contentType: "application/json; charset=utf-8",
               success: function(data) {
                  // todo  should something deleted?
               }
            });
         } else {
            // todo  should something deleted?
         }
      }
   }

   function send() {
      var def = $.Deferred();
      $.when(sendAll()).done(function() {
         def.resolve();
         return;
      });
      return def.promise();
   }

   function sendAll() {
      var def = $.Deferred();
      $.when(sendTelemeeApps()).done(function(persistedTelemeeApp) {
         $.when(sendChannels()).done(function(persistedChannel) {
            $.when(sendBindChannelsToApps()).done(function() {
               $.when(sendChannelAttributes()).done(function() {
                  $.when(sendBindChannelAttributesToChannels()).done(function() {
                     $.when(sendLogEntries()).done(function() {
                        clearLogEntries();
                        def.resolve();
                     });
                  });
               });
            });
         });
      });
      return def.promise();
   }

   function clearLogEntries() {
      logEntries = new Array();
      currentLogEntry = null;
   }

   function sendTelemeeApps() {
      var def = $.Deferred();
      var count = 0;
      for (var i = 0; i < telemeeApps.length; i++) {
         var telemeeApp = telemeeApps[i];
         $.when(createTelemeeApp(telemeeApp)).done(function(newTelemeeApp) {
            telemeeApp.id = newTelemeeApp.id;
            count++;
            if (count === telemeeApps.length) {
               def.resolve(telemeeApp);
            }
         });
      }
      return def.promise();
   }

   function sendChannels() {
      var def = $.Deferred();
      var count = 0;
      channels.forEach(function(channel) {
         $.when(createChannel(channel)).done(function(newChannel) {
            channel.id = newChannel.id;
            count++;
            if (count === channels.length) {
               def.resolve(channel);
            }
         });
      }
      );
      return def.promise();
   }

   function sendBindChannelsToApps() {
      var def = $.Deferred();
      var count = 0;
      for (var i2 = 0; i2 < telemeeApps.length; i2++) {
         var telemeeApp = telemeeApps[i2];
         if (telemeeApp.channels) {
            for (var i = 0; i < telemeeApp.channels.length; i++) {
               var channel = telemeeApp.channels[i];
               if (channel) {
                  if ($.inArray(telemeeApp, channel.boundedTelemeeApps)) {
                     $.when(bindChannelToApp(channel, telemeeApp)).done(function() {
                        if (!channel.boundedTelemeeApps) {
                           channel.boundedTelemeeApps = [];
                        }
                        channel.boundedTelemeeApps.push(telemeeApp);
                     });
                  }
               }
            }
         }
         count++;
         if (count === telemeeApps.length) {
            def.resolve();
         }
      }
      return def.promise();
   }

   function sendChannelAttributes() {
      var def = $.Deferred();
      var count = 0;
      channelAttributes.forEach(function(channelAttribute) {
         $.when(createChannelAttribute(channelAttribute)).done(function(newChannelAttribute) {
            channelAttribute.id = newChannelAttribute.id;
            count++;
            if (count === channelAttributes.length) {
               def.resolve();
            }
         });
      }
      );
      return def.promise();
   }

   function sendBindChannelAttributesToChannels() {
      var def = $.Deferred();
      var count = 0;
      for (var i2 = 0; i2 < channels.length; i2++) {
         var channel = channels[i2];
         for (var i = 0; i < channel.channelAttributes.length; i++) {
            var channelAttribute = channel.channelAttributes[i];
            if (channelAttribute) {
               if ($.inArray(channel, channelAttribute.boundedChannels)) {
                  $.when(bindChannelAttributeToChannel(channelAttribute, channel)).done(function() {
                     if (!channelAttribute.boundedChannels) {
                        channelAttribute.boundedChannels = [];
                     }
                     channelAttribute.boundedChannels.push(channel);
                  });
               }
            }
         }
         count++;
         if (count === channels.length) {
            def.resolve();
         }
      }
      return def.promise();
   }

   function sendLogEntries() {
      var count = 0;
      for (var i = 0; i < logEntries.length; i++) {   //for-loop because this must run in sync
         logEntries[i].channelID = logEntries[i].channel.id;
         logEntries[i].logValues.forEach(function(value) {
            value.channelAttributeID = value.channelAttribute.id;
         });
         $.when(createLogEntry(logEntries[i])).done(function(newLogEntry) {
            count++;
            if (count === logEntries.length - 1) {
               return;
            }
         });
      }
   }

   function getTelemeeApp(telemeeAppName) {
      var telemeeApp;
      for (var i = 0; i < telemeeApps.length; i++) {
         if (telemeeApps[i].name === telemeeAppName) {
            telemeeApp = telemeeApps[i];
         }
      }
      return telemeeApp;
   }

//#####################################################################
// Exposed API
//#####################################################################
   return{
      init: init,
      testConnectionToServer: testConnectionToServer,
      loadDataFromServer: loadDataFromServer,
      forTelemeeApp: forTelemeeApp,
      forChannel: forChannel,
      startLogEntry: startLogEntry,
      forChannelAttribute: forChannelAttribute,
      log: log,
      endLogEntry: endLogEntry,
      send: send,
      getTelemeeApps: getTelemeeApps,
      getTelemeeApp: getTelemeeApp,
      getChannels: getChannels,
      getChannelAttributes: getChannelAttributes,
      getLogEntries: getLogEntries,
      deleteTelemeeApp: deleteTelemeeApp,
      deleteChannel: deleteChannel,
      deleteChannelAttribute: deleteChannelAttribute,
      deleteLogEntries: deleteLogEntries,
      baseURI: baseURI,
      logLevel: logLevel,
      OFF: OFF,
      SEVERE: SEVERE,
      WARNING: WARNING,
      INFO: INFO,
      FINE: FINE,
      FINER: FINER,
      FINEST: FINEST,
      ALL: ALL
   };
})();