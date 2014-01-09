'use strict';

angular.module('myApp.List', [])
        .controller('MyListCtrl', function($scope, $timeout, Configuration, TelemeeApps, LogValue, Logentries) {
           $scope.configuration = Configuration;
           $scope.LogValue = LogValue;

           $scope.btnStart_disabled = false;
           $scope.btnStop_disabled = true;
           var mytimeout;
           $scope.start = function() {
              $scope.btnStart_disabled = true;
              $scope.btnStop_disabled = false;
              mytimeout = $timeout($scope.onTimeout, $scope.configuration.interval);
           };
           $scope.onTimeout = function() {
              mytimeout = $timeout($scope.onTimeout, $scope.configuration.interval);
              $scope.showLogentries();
           };
           $scope.stop = function() {
              $scope.btnStart_disabled = false;
              $scope.btnStop_disabled = true;
              $timeout.cancel(mytimeout);
           };
           $scope.$on(
                   "$destroy",
                   function(event) {
                      $timeout.cancel(mytimeout);
                   }
           );

           $scope.filter = '';
           $scope.predicate = '';
           $scope.choose_channel = "";
           $scope.apps = TelemeeApps.query();
           $scope.channels = [];
           $scope.channelAttributes = [];
           $scope.logentries = [];

           function ensureArray(arrayOrOneElement) {
              if ($.isArray(arrayOrOneElement)) {
                 return arrayOrOneElement;
              }
              return new Array().concat(arrayOrOneElement);
           }

           $scope.showLogentries = function() {
              if ($scope.appSelected) {
                 $scope.choose_channel = "-- please choose channel --";
                 $scope.channels = ensureArray($scope.appSelected.channels);
              } else {
                 $scope.channels = [];
                 $scope.choose_channel = "";
              }
              if ($scope.appSelected && $scope.channelSelected) {
                 $scope.channelAttributes = ensureArray($scope.channelSelected.channelAttributes);
                 var lastCount = $scope.lastCount === undefined ? 0 : $scope.lastCount;
                 var logentries = Logentries.query({appID: $scope.appSelected.id, channelID: $scope.channelSelected.id, lastCount: lastCount});
                 $scope.logentries = logentries;
              }
           };

           $scope.sortFunction = function(logentry) {
              if ($scope.predicate.name) {   //is a channelAttribute to sort? Only channelAttributes have a name property
                 if (isNaN(LogValue.getLogValueForAttr(logentry, $scope.predicate))) {
                    return LogValue.getLogValueForAttr(logentry, $scope.predicate);
                 }
                 return parseInt(LogValue.getLogValueForAttr(logentry, $scope.predicate));
              } else {
                 if (isNaN(logentry[$scope.predicate])) {
                    return logentry[$scope.predicate];
                 }
                 return parseInt(logentry[$scope.predicate]);
              }
           };

           $scope.setPredicateAndReverse = function(channelAttribute) {
              $scope.predicate = channelAttribute;
              $scope.reverse = !$scope.reverse;
           };

           $scope.filterLogEntries = function(logEntry) {
              var found = false;
              if ($scope.filter.description) {
                 if (logEntry.description.indexOf($scope.filter.description) !== -1) {
                    found = true;
                 }
              }
              if ($scope.filter.anyAttribute) {
                 if ($.isArray(logEntry.logValues)) {
                    angular.forEach(logEntry.logValues, function(logValue) {
                       var strValue = '' + logValue.value;
                       var strFilterToken = '' + $scope.filter.anyAttribute;
                       if (strValue.indexOf(strFilterToken) !== -1) {
                          found = true;
                          return;
                       }
                    });
                 } else {
                    var strLogValue = '' + logEntry.logValues.logValue;
                    var strFilterToken = '' + $scope.filter.anyAttribute;
                    if (strLogValue.indexOf(strFilterToken) !== -1) {
                       found = true;
                    }
                 }
              }
              if (!$scope.filter.description && !$scope.filter.anyAttribute) {
                 found = true;
              }
              return found;
           };

        })
        ;