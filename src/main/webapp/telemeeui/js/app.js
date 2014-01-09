'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
   'ngRoute',
   'myApp.filters',
   'myApp.services',
   'myApp.directives',
   'myApp.Admin',
   'myApp.Scatter',
   'myApp.List',
   'myApp.GraphLineChart'
]).
        config(['$routeProvider', function($routeProvider) {
              $routeProvider.when('/view_list', {templateUrl: 'partials/partial_list.html', controller: 'MyListCtrl'});
              $routeProvider.when('/view_graph_scatter', {templateUrl: 'partials/partial_graph_scatter.html', controller: 'MyGraphScatterCtrl'});
              $routeProvider.when('/view_graph_lineChart', {templateUrl: 'partials/partial_graph_lineChart.html', controller: 'MyGraphLineChartCtrl'});
              $routeProvider.when('/view_admin', {templateUrl: 'partials/partial_admin.html', controller: 'MyAdminCtrl'});
              $routeProvider.otherwise({redirectTo: '/view_list'});
           }]);
