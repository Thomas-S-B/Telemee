'use strict';

// look at https://github.com/novus/nvd3/blob/master/src/models/scatterChart.js
angular.module('myApp.Scatter', [])
        .controller('MyGraphScatterCtrl', function($scope, $timeout, Configuration, GraphSize, LogValue, TelemeeApps) {
           $scope.configuration = Configuration;
           //Init scope
           $scope.choose_app = "-- please choose app --";
           $scope.choose_channel = "";
           $scope.choose_channelattribute = "";

           $scope.apps = TelemeeApps.query();
           $scope.apps.sort(function(appA, appB) {
              return d3.ascending(appA.name, appB.name);
           });
           $scope.appSelected = undefined;
           $scope.channels = [];
           $scope.channelSelected = undefined;
           $scope.channelAttributes = [];
           $scope.lastCount = 0;
           $scope.fromID = 0;
           $scope.cache_logEntries = [];

           $scope.configOpen = true;
           $scope.toggleSelection = function() {
              $scope.configOpen = !$scope.configOpen;
              GraphSize.fitSize($scope);
           };

           // Autoupdate
           var mytimeout;
           $scope.btnStart_disabled = false;
           $scope.btnStop_disabled = true;
           $scope.onTimeout = function() {
              mytimeout = $timeout($scope.onTimeout, $scope.configuration.interval);
              $scope.updateGraph(false, false);
           };
           $scope.startAutoUpdate = function() {
              $scope.btnStart_disabled = true;
              $scope.btnStop_disabled = false;
              mytimeout = $timeout($scope.onTimeout, $scope.configuration.interval);
              $scope.updateGraph(false, false);
           };
           $scope.stopAutoUpdate = function() {
              $scope.btnStart_disabled = false;
              $scope.btnStop_disabled = true;
              $timeout.cancel(mytimeout);
              $scope.updateGraph(false, false);
           };
           $scope.$on(
                   "$destroy",
                   function(event) {
                      $timeout.cancel(mytimeout);
                   }
           );

           function ensureArray(arrayOrOneElement) {
              if ($.isArray(arrayOrOneElement)) {
                 return arrayOrOneElement;
              }
              return new Array().concat(arrayOrOneElement);
           }

           // Graph
           $scope.updateGraph = function(deleteCache, init) {
              $scope.init = init;
              if (deleteCache) {
                 $scope.cache_logEntries = [];
                 $scope.fromID = 0;
              }
              if ($scope.appSelected) {
                 $scope.choose_channel = "-- please choose channel --";
                 $scope.channels = ensureArray($scope.appSelected.channels);
                 $scope.channels.sort(function(channelA, channelB) {
                    return d3.ascending(channelA.name, channelB.name);
                 });
                 if ($scope.channelSelected) {
                    $scope.choose_channelattribute = "-- please choose channelattribute --";
                    $scope.channelAttributes = ensureArray($scope.channelSelected.channelAttributes);
                    $scope.channelAttributes.sort(function(attrA, attrB) {
                       return d3.ascending(attrA.name, attrB.name);
                    });
                    $scope.choose_groupBy = "-- you could group by channelattribute --";
                 }
              } else {
                 $scope.channels = [];
                 $scope.xChannelAttributeSelect = undefined;
                 $scope.yChannelAttributeSelect = undefined;
                 $scope.zChannelAttributeSelect = undefined;
                 $scope.choose_app = "-- please choose app --";
                 $scope.choose_channel = "";
                 $scope.choose_channelattribute = "";
                 $scope.choose_groupBy = "";
              }
              if (!$scope.appSelected || !$scope.channelSelected || !$scope.xChannelAttributeSelect || !$scope.yChannelAttributeSelect) {
                 return;
              }

              var lastCount = $scope.lastCount === undefined ? 0 : $scope.lastCount;
              if ($scope.cache_logEntries.length < 1 || $scope.fromID === undefined) {
                 $scope.fromID = 0;
              }

              var channel = $scope.channelSelected.id;
              var xAttr = $scope.xChannelAttributeSelect;
              var yAttr = $scope.yChannelAttributeSelect;
              var zAttr = $scope.zChannelAttributeSelect;
              var groupByAttr = $scope.groupBySelect;
              d3.json($scope.configuration.baseURI + '/telemee/resources/channels/' + channel, function(channel) {
                 var dataPath = $scope.configuration.baseURI + '/telemee/resources/logentries?channelID=' + channel.id + '&fromID=' + $scope.fromID + '&lastCount=' + lastCount;
                 d3.json(dataPath, function(dataLogEntries) {
                    // Load data - everything must in this body due asynchronous
                    if (!(angular.isUndefined(dataLogEntries) || dataLogEntries === null)) {
                       if ($.isArray(dataLogEntries.logEntry)) {
                          // push new logentries into the cache
                          $scope.cache_logEntries.push.apply($scope.cache_logEntries, dataLogEntries.logEntry);
                          // get highest ID of all logEntries in the cache
                          if (dataLogEntries.logEntry.length > 0) {
                             for (var iNewLogEntry in dataLogEntries.logEntry) {
                                var logEntry = dataLogEntries.logEntry[iNewLogEntry];
                                if ($scope.fromID < logEntry.id) {
                                   $scope.fromID = parseInt(logEntry.id);
                                }
                             }
                          }
                       } else {
                          // Not an array with Logentries with the Name LogEntry, only one logEntry without name LogEntry
                          // push new logentry into the cache
                          $scope.cache_logEntries.push(dataLogEntries.logEntry);
                          // get highest ID of all logEntries in the cache
                          if ($scope.fromID < dataLogEntries.logEntry.id) {
                             $scope.fromID = parseInt(dataLogEntries.logEntry.id);
                          }
                       }
                    }
                    // Order logEntries by xAttr / is executed synchron
                    $scope.cache_logEntries.sort(function(logEntryA, logEntryB) {
// TODO what's about not numeric xAttr?
                       return d3.ascending(parseFloat(LogValue.getLogValueForAttr(logEntryA, xAttr)), parseFloat(LogValue.getLogValueForAttr(logEntryB, xAttr)));
                    });

                    // delete all old logEntries on the left
                    if ($scope.cache_logEntries.length > $scope.lastCount && $scope.lastCount > 0) {
                       var deleteCount = $scope.cache_logEntries.length - $scope.lastCount;
                       $scope.cache_logEntries = $scope.cache_logEntries.slice(deleteCount);
                    }


                    $scope.data = [];
                    if (groupByAttr) {
                       // Get all distinct values of the groupByAttr stored in the logentries
                       var groups = [];
                       for (var iNewLogEntry in $scope.cache_logEntries) {
                          var logEntry = $scope.cache_logEntries[iNewLogEntry];
                          for (var iLogValue = 0; iLogValue < logEntry.logValues.length; iLogValue++) {
                             var logValue = logEntry.logValues[iLogValue];
                             if (logValue.channelAttributeID === groupByAttr.id) {
                                var alreadyInGroups = false;
                                for (var iGroup = 0; iGroup < groups.length; iGroup++) {
                                   if (groups[iGroup] === logValue.value) {
                                      alreadyInGroups = true;
                                      break;
                                   }
                                }
                                if (!alreadyInGroups) {
                                   groups.push(logValue.value);
                                }
                             }
                          }
                       }
                       //Sort groups for better UI
                       groups.sort(function(groupA, groupB) {
                          return d3.ascending(groupA, groupB);
                       });
                       //Put logEntries in it's group
                       for (var iGroup = 0; iGroup < groups.length; iGroup++) {
                          $scope.data.push({
                             key: groups[iGroup],
                             values: []
                          });
                          for (var iNewLogEntry in $scope.cache_logEntries) {
                             var logEntry = $scope.cache_logEntries[iNewLogEntry];
                             for (var iLogValue = 0; iLogValue < logEntry.logValues.length; iLogValue++) {
                                var logValue = logEntry.logValues[iLogValue];
                                if (logValue.channelAttributeID === groupByAttr.id) {
                                   if (logValue.value === groups[iGroup]) {
                                      $scope.data[iGroup].values.push({
                                         x: parseInt(LogValue.getLogValueForAttr(logEntry, xAttr))
                                         , y: parseInt(LogValue.getLogValueForAttr(logEntry, yAttr))
                                         , size: parseInt(LogValue.getLogValueForAttr(logEntry, zAttr))
                                         , description: logEntry.description
                                      });
                                   }
                                }
                             }
                          }
                       }
                    } else {
                       $scope.data.push({
                          key: channel.name,
                          values: []
                       });
                       for (var iNewLogEntry in $scope.cache_logEntries) {
                          var logEntry = $scope.cache_logEntries[iNewLogEntry];
                          $scope.data[0].values.push({
                             x: parseInt(LogValue.getLogValueForAttr(logEntry, xAttr))
                             , y: parseInt(LogValue.getLogValueForAttr(logEntry, yAttr))
                             , size: parseInt(LogValue.getLogValueForAttr(logEntry, zAttr))
                             , description: logEntry.description
                          });
                       }
                    }

                    // Initialize graph?
                    if ($scope.init) {
                       var xName = $scope.xChannelAttributeSelect.name; //TODO why is it working only this way?
                       var yName = $scope.yChannelAttributeSelect.name;
                       nv.addGraph(function() {
                          $scope.chart = nv.models.scatterChart();
                          $scope.chart.showLegend(true);
                          $scope.chart.tooltips(true);
                          $scope.chart.showDistX(true);
                          $scope.chart.showDistY(true);
                          $scope.chart.xAxis.axisLabel(xName);
                          $scope.chart.yAxis.axisLabel(yName);
                          $scope.chart.xAxis.rotateLabels(10);
                          $scope.chart.noData("Waiting for data from " + channel.name);
                          $scope.chart.transitionDuration(250);
                          $scope.chart.tooltipContent(function(key, x, y, graphElement) {
                             return key
                                     + "<br/>x=" + xAttr.name + ': ' + x
                                     + '<br/>y=' + yAttr.name + ': ' + y
                                     + '<br/>s=' + zAttr.name + ': ' + graphElement.point.size
                                     + '<br/>Description: ' + graphElement.point.description;
                          });
                          $('.nvtooltip').css({"margin": "-1px"});
                          nv.utils.windowResize(function() {
                             d3.select('#chart svg').call($scope.chart);
                             GraphSize.fitSize($scope);
                          });
                          d3.select('#chart svg')
                                  .datum($scope.data)
                                  .call($scope.chart);
                          $scope.init = false;
                          GraphSize.fitSize($scope);
                       });
                    } else {
                       d3.select('#chart svg')
                               .datum($scope.data)
                               .transition().duration(1) //for focuschart
                               .call($scope.chart);
                    }
                 });
              });
           };

        })
        ;