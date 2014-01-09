'use strict';
/* Services */

var telemeeAppServices = angular.module('myApp.services', ['ngResource']);
telemeeAppServices.factory('Configuration', function() {
   var configuration = {};
   configuration.baseURI = 'http://localhost:8080';
   configuration.interval = 1000;
   return configuration;
});
telemeeAppServices.service('GraphSize', function() {
   return {
      fitSize: function($scope) {
         var graphHeight = nv.utils.windowSize().height - $("#selection").position().top - $("#selection").outerHeight() - 180;
         if (!$scope.configOpen) {
            var increaseHeight = $("#selection").height();
            $("#selection").slideUp(350);
            $("#btnToggleSelection").html("Open selections");
            $('#chart svg').animate({
               height: graphHeight + increaseHeight
            }, 650, function() {
               $scope.updateGraph(false, false);
            });
         } else {
            $('#chart svg').animate({
               height: graphHeight
            }, 250, function() {
               $scope.updateGraph(false, false);
            });
            $("#selection").slideDown(350);
            $("#btnToggleSelection").html("Close selections");
         }
      }
   };
});
telemeeAppServices.service('LogValue', function() {
   return {
      getLogValueForAttr: function(logentry, channelAttribute) {
         var retLogValue = null;
         if (channelAttribute) {
            if ($.isArray(logentry.logValues)) {
               logentry.logValues.forEach(function(logValue) {
                  var channelAttributeID = logValue.channelAttributeID;
                  if (channelAttributeID === channelAttribute.id) {
                     retLogValue = logValue.value;
                  }
               });
            } else {
               var logValue = logentry.logValues;
               var channelAttributeID = logValue.channelAttributeID;
               if (channelAttributeID === channelAttribute.id) {
                  retLogValue = logValue.value;
               }
            }
         }
         return retLogValue;
      }
   };
});
telemeeAppServices.service('TelemeeApps', function($resource, Configuration) {
   return $resource(Configuration.baseURI + '/telemee/resources/telemeeapps/:id', {id: '@id'}, {
      'query': {
         method: 'GET',
         params: {},
         format: 'json',
         isArray: true,
         transformResponse: function(rawData, headersGetter) {
            var json = JSON.parse(rawData);
            if (json !== null) {
               // Check if object is an array, because the Rest-service delivers an Object if it's only one Object
               if (angular.isArray(json.telemeeApp)) {
                  return json.telemeeApp;
               } else {
                  //It's an Object (one Entry) => convert it to an array
                  return [].concat(json.telemeeApp);
               }
            } else {
               return [];
            }

         }
      },
      'save': {method: 'POST', params: {}, format: 'json', isArray: true},
      'get': {method: 'GET', params: {}, format: 'json', isArray: false},
      'update': {method: 'PUT', params: {id: '@id'}, format: 'json', isArray: true},
      'delete': {method: 'DELETE', params: {id: '@id'}, format: 'json', isArray: false}
   });
});
telemeeAppServices.service('Channels', function($resource, Configuration) {
   return $resource(Configuration.baseURI + '/telemee/resources/channels/:id', {id: '@id'}, {
      'query': {
         method: 'GET',
         params: {},
         format: 'json',
         isArray: true,
         transformResponse: function(rawData, headersGetter) {
            var json = JSON.parse(rawData);
            // Check if object is an aarray, because the Rest-service delivers an Object if it's only one Object
//            if (json.channel instanceof Array) {
            if (angular.isArray(json.channel)) {
               return json.channel;
            } else {
               //It's an Object (one Entry) => convert it to an array
               return [].concat(json.channel);
            }
         }
      },
      'save': {method: 'POST', params: {}, format: 'json', isArray: true},
      'get': {method: 'GET', params: {}, format: 'json', isArray: false},
      'update': {method: 'PUT', params: {id: '@id'}, format: 'json', isArray: true},
      'delete': {method: 'DELETE', params: {id: '@id'}, format: 'json', isArray: false}
   });
});
telemeeAppServices.service('ChannelAttributes', function($resource, Configuration) {
   return $resource(Configuration.baseURI + '/telemee/resources/channelattributes/:id', {id: '@id'}, {
      'query': {
         method: 'GET',
         params: {},
         format: 'json',
         isArray: true,
         transformResponse: function(rawData, headersGetter) {
            var json = JSON.parse(rawData);
            // Check if object is an aarray, because the Rest-service delivers an Object if it's only one Object
//            if (json.channelAttribute instanceof Array) {
            if (angular.isArray(json.channelAttribute)) {
               return json.channelAttribute;
            } else {
               //It's an Object (one Entry) => convert it to an array
               return [].concat(json.channelAttribute);
            }
         }
      },
      'save': {method: 'POST', params: {}, format: 'json', isArray: true},
      'get': {method: 'GET', params: {}, format: 'json', isArray: false},
      'update': {method: 'PUT', params: {id: '@id'}, format: 'json', isArray: true},
      'delete': {method: 'DELETE', params: {id: '@id'}, format: 'json', isArray: false}
   });
});
telemeeAppServices.service('Logentries', function($resource, Configuration) {
   return $resource(Configuration.baseURI + '/telemee/resources/logentries?appID=:appID&channelID=:channelID&lastCount=:lastCount', {}, {
      'query': {
         method: 'GET',
         params: {},
         format: 'json',
         isArray: true,
         transformResponse: function(rawData, headersGetter) {
            var json = JSON.parse(rawData);
            // Check if object is an aarray, because the Rest-service delivers an Object if it's only one Object
            if (angular.isArray(json.logEntry)) {
               return json.logEntry;
            } else {
               //It's an Object (one Entry) => convert it to an array
               return [].concat(json.logEntry);
            }
         }
      },
      'save': {method: 'POST', params: {}, format: 'json', isArray: true},
      'get': {method: 'GET', params: {}, format: 'json', isArray: false},
      'update': {method: 'PUT', params: {id: '@id'}, format: 'json', isArray: true},
      'delete': {method: 'DELETE', params: {id: '@id'}, format: 'json', isArray: false}
   });
});
        