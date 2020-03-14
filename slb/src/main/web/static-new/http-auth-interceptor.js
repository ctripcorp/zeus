/*global angular:true, browser:true */

/**
 * @license HTTP Auth Interceptor Module for AngularJS
 * (c) 2012 Witold Szczerba
 * License: MIT
 */
angular.module('http-auth-interceptor', ['ngCookies'])
    .factory('authService', ['$rootScope', '$q','$cookies', function ($rootScope, $q,$cookies) {
        return {
            request: function (config) {
                if(config.withCredentials==undefined){
                    config.withCredentials=true;
                }
                var configUrl = config.url;
                if(!configUrl){
                   return config;
                }else{
                    configUrl = configUrl.toLowerCase();
                    if(configUrl.indexOf('fraaws')==-1){
                        return config;
                    }
                }
                config.headers = config.headers || {};
                var allCookies = $cookies.getAll();
                if (allCookies && allCookies['_stok']) {
                    var stok = allCookies['_stok'];
                    if(stok){
                        stok = stok.replace(/ /g,'+');
                    }
                    config.headers['_stok'] = stok;
                }
                return config;
            },
            requestError: function (rejection) {
                return rejection;
            },
            response: function (response) {
                return response;
            },
            responseError: function (rejection) {
                return rejection;
            }
        };
    }])
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push("authService");
    }]);

