<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- title, meta tags, list of stylesheets, etc ... -->

    <meta charset="utf-8"/>
    <!-- use the following meta to force IE use its most up to date rendering engine -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <title> SLB Console </title>
    <meta name="description" content="page description"/>

    <!-- stylesheets are put here, refer to files/css documentation -->
    <link href="../dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../assets/css/ace-fonts.css" rel="stylesheet"/>
    <link href="../assets/css/font-awesome.css" rel="stylesheet"/>
    <!-- you can also use google hosted fonts -->

    <link href="../dist/css/ace.min.css" rel="stylesheet" class="ace-main-stylesheet"/>

    <!--[if lte IE 9]>
    <link href="../dist/css/ace-part2.min.css" rel="stylesheet" class="ace-main-stylesheet"/>
    <![endif]-->

    <!--[if lte IE 9]>
    <link href="../assets/css/ace-ie.min.css" rel="stylesheet" />
    <!-- some scripts should be  here, refer to files/javascript documentation -->

    <!--[if !IE]> -->
    <script src="../dist/js/jquery.min.js"></script>
    <!-- <![endif]-->
    <!--[if lte IE 9]>
    <script src="../dist/js/jquery1x.min.js"></script>
    <![endif]-->

    <script src="../dist/js/bootstrap.min.js"></script>

    <!-- ie8 canvas if required for plugins such as charts, etc -->
    <!--[if lte IE 8]>
    <script src="../dist/js/excanvas.min.js"></script>
    <![endif]-->


    <script src="../dist/js/ace-elements.min.js"></script>
    <script src="../dist/js/ace.min.js"></script>

    <script type="text/javascript">
        //If page has any inline scripts, it goes here
    </script>


</head>

<body class="no-skin">
<div class="navbar" id="navbar">
    <!-- navbar goes here -->
    <div id="navbar-container" class="navbar-container">

        <!-- toggle buttons are here or inside brand container -->

        <div class="navbar-header pull-left">
            <!-- brand text here -->
            <a href="#" class="navbar-brand">
                <small>
                    <i class="fa fa-leaf"></i>
                    SLB Console
                </small>
            </a>
        </div>
        <!-- /.navbar-header -->
    </div>
    <!-- /.navbar-container -->
</div>
<!-- /.navbar -->

<div class="main-container" id="main-container">
    <div class="sidebar responsive" id="sidebar">
        <div class="sidebar-shortcuts" id="sidebar-shortcuts">
        </div>
        <!-- /.sidebar-shortcuts -->

        <!-- sidebar goes here -->
        <ul class="nav nav-list">
            <li class="">
                <a href="/slb">
                    <i class="menu-icon fa fa-tachometer"></i>
                    <span class="menu-text"> SLB Cluster </span>
                </a>
                <b class="arrow"></b>
            </li>
            <li class="active">
                <a href="/group" class="">
                    <i class="menu-icon fa fa-desktop"></i>
							<span class="menu-text">
								Application
							</span>
                </a>
                <b class="arrow"></b>
            </li>
            <li class="">
                <a href="/op" class="">
                    <i class="menu-icon fa fa-desktop"></i>
							<span class="menu-text">
								Operation
							</span>
                </a>
                <b class="arrow"></b>
            </li>
            <li class="">
                <a href="/status" class="">
                    <i class="menu-icon fa fa-desktop"></i>
							<span class="menu-text">
								Status
							</span>
                </a>
                <b class="arrow"></b>
            </li>
        </ul>

        <div class="sidebar-toggle sidebar-collapse">
            <i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left"
               data-icon2="ace-icon fa fa-angle-double-right"></i>
        </div>
    </div>

    <div class="main-content">
        <div class="main-content-inner">
            <div class="breadcrumbs" id="breadcrumbs">
                <!-- breadcrumbs here -->
                <ul class="breadcrumb">
                    <li><i class="fa fa-home home-icon"></i> <a href="/">Home</a></li>
                    <li class="active">SLB Cluster</li>
                </ul>

                <!-- searchbox here -->
                <div id="nav-search" class="nav-search">
                    <form class="form-search">
                      <span class="input-icon">
                        <input type="text" class="nav-search-input" id="nav-search-input" autocomplete="off"
                               placeholder="Search ..."/>
                        <i class="ace-icon fa fa-search nav-search-icon"></i>
                      </span>
                    </form>
                </div>

            </div>
            <!-- /.breadcrumb -->

            <div class="page-content">

                <div class="page-header">
                    <!-- page header goes here -->
                    <h1>
                        SLB Cluster
                        <small><i class="ace-icon fa fa-angle-double-right"></i> overview & stats</small>
                    </h1>
                </div>

                <div class="row">
                    <div class="col-xs-12">
                        <!-- page content goes here -->
                        <div ng-app="" ng-controller="mainController">
                            <div>
                                <div style="float: left;margin-right:6px" ng-repeat="x in groups">
                                    <div>
                                        <button ng-click="showa(x)">
                                            {{x.name}}
                                        </button>
                                    </div>
                                    <div>
                                        <button ng-click="activate(x.name)">
                                            Activate {{x.name}}
                                        </button>
                                    </div>
                                </div>
                                <div style="clear:both"></div>
                            </div>

                            <textarea cols="100" rows="20" ng-model="current">

                            </textarea>

                            <div>
                                <button ng-click="save(current)">Submit</button>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <!-- /.page-content -->
        </div>
    </div>
    <!-- /.main-content -->

    <!-- footer area -->
    <div class="footer">
        <div class="footer-inner">
            <div class="footer-content">
                <!-- footer content here -->
                Ctrip &copy; 2015
            </div>
        </div>
    </div>

</div>
<!-- /.main-container -->

<!-- list of script files -->
<script src="../dist/js/angular.min.js"></script>
<script>
    function mainController($scope, $http) {
        $scope.lll = function () {
            $http.get("/api/group").success(
                    function (response) {
                        $scope.groups = response.groups;
                    }
            );
        }
        $scope.lll();
        $scope.showa = function (x) {
            $scope.currentAppName = x.name;
            delete x['$$hashKey'];
            $scope.current = JSON.stringify(x, null, "    ");
        }
        $scope.save = function (content) {
            $http.post("/api/group/add", content).success(
                    function (response) {
                        $scope.lll();
                    }
            );
        }


        $scope.activate = function (groupId) {
            var req = {"conf-group-names": [{"groupId": groupId}]};
            var reqtotle = {"conf-slb-names": [{"slbname": "default"}],"conf-group-names": [{"groupId": groupId}]};

            $http.post("/api/conf/activate",JSON.stringify(req)).success(
                    function (response) {
                    }
            );
        }
    }
</script>

</body>
</html>
