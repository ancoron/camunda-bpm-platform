"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, EngineApi, Authentication) {

    $scope.taskList = {};

    function loadTasks(filter, search) {
      if (!Authentication.current()) {
        return;
      }

      var queryObject = {};

      switch (filter) {
        case "mytasks":
          queryObject.assignee = Authentication.current();
          break;
        case "colleague":
          queryObject.assignee = search;
          break;
        case "group":
          queryObject.candidateGroup = search;
          break;
      }

      $scope.taskList.tasks = EngineApi.getTasklist().query(queryObject);

      /*$scope.taskList.tasks = allTasks[filter + (search ? "-" + search : "")];
      $scope.taskList.view = { filter: filter, search: search };
      $scope.taskList.selection = [];*/
    }

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      loadTasks(newValue.filter || "mytasks", newValue.search);
    });

    $scope.startTask = function(task) {

    };

    $scope.claimTask = function(task) {
      EngineApi.getTasklist().$claim({id: task.id});
    };

    $scope.delegateTask = function(task) {

    };

    $scope.isSelected = function(task) {
      return $scope.taskList.selection.indexOf(task) != -1;
    };

    $scope.selectAllTasks = function() {

      $scope.deselectAllTasks();

      var selection = $scope.taskList.selection,
          tasks = $scope.taskList.tasks;

      angular.forEach(tasks, function(task) {
        selection.push(task);
      });
    };

    $scope.deselectAllTasks = function() {
      return $scope.taskList.selection = [];
    };

    $scope.taskList = {
      tasks: [],
      selection: []
    };
  };

  Controller.$inject = ["$scope", "$location", "EngineApi", "Authentication"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("OverviewController", Controller);
});