'use strict';

angular.module('myApp.Admin', [])
        .controller('MyAdminCtrl', function($scope, Configuration, $http, TelemeeApps, Channels, ChannelAttributes) {
           $scope.connectionMessage = "";
           $scope.configuration = Configuration;

           //Testing connection...
           $scope.testConnection = function() {
              $scope.connectionMessage = "Testing connection...";
              $http({method: 'HEAD', url: $scope.configuration.baseURI + '/telemee/resources/monitor/alive', timeout: 1000}).
                      success(function(data, status, headers, config) {
                         $scope.connectionMessage = "Connection is ok";
                      }).
                      error(function(data, status, headers, config) {
                         $scope.connectionMessage = "Can't connect to server";
                      });
           };
           $scope.clearConnectionMessage = function() {
              $scope.connectionMessage = "*** Please test connection ***";
           };

           updateAppList();

           function ensureArray(arrayOrOneElement) {
              if ($.isArray(arrayOrOneElement)) {
                 return arrayOrOneElement;
              }
              return new Array().concat(arrayOrOneElement);
           }

           $scope.getChannelsOfApp = function(app) {
              return ensureArray(app.channels);
           };

           $scope.allChannels = Channels.query();

           $scope.allChannelAttributes = ChannelAttributes.query();

           $scope.getChannelAttributesOfChannel = function(channel) {
              return ensureArray(channel.channelAttributes);
           };

           $scope.getCountsByApp = function(app) {
              var telemeeAPI = $scope.configuration.baseURI + '/telemee/resources/logentries/countByAppId/' + app.id;
              var count = 0;
              $.ajax({
                 type: "GET",
                 async: false,
                 url: telemeeAPI,
                 success: function(data) {
                    count = data;
                 }
              });
              return count;
           };

           $scope.getCountsByChannel = function(channel) {
              var telemeeAPI = $scope.configuration.baseURI + '/telemee/resources/logentries/countByChannelId/' + channel.id;
              var count = 0;
              $.ajax({
                 type: "GET",
                 async: false,
                 url: telemeeAPI,
                 success: function(data) {
                    count = data;
                 }
              });
              return count;
           };

           $scope.deleteAllLogEntries = function(channel) {
              var telemeeAPI = $scope.configuration.baseURI + '/telemee/resources/logentries/deleteByChannelId/' + channel.id;
              $.ajax({
                 type: "DELETE",
                 async: false,
                 url: telemeeAPI,
                 success: function(data) {
                 }
              });
           };

           $scope.deleteApp = function(app) {
              var telemeeAPI = $scope.configuration.baseURI + '/telemee/resources/telemeeapps/' + app.id;
              $.ajax({
                 type: "DELETE",
                 async: false,
                 url: telemeeAPI,
                 success: function(data) {
                    updateAppList();
                 }
              });
           };

           function updateAppList() {
              $scope.apps = TelemeeApps.query();
              $scope.apps.sort(function(appA, appB) {
                 return d3.ascending(appA.name, appB.name);
              });
           }

        })
        ;