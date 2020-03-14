#!/bin/sh

red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
endColor='\e[0m'
SLB_ENV="open-source"
SLB_ADMIN_START="./scripts/startup-open-source.sh"
SLB_ADMIN_START_SCRIPT="startup-open-source.sh"
SLB_ADMIN_PACKAGE_NAME="slb-1.0.1.zip"
SLB_ADMIN_JAR_PATH="/opt"
SLB_ADMIN_INSTALL_PATH="/opt/slb"
pid=0

MAIN_CLASS="com.ctrip.zeus.SlbAdminMain"
_jps() {
     #echo `ps aux | grep $MAIN_CLASS | grep -v grep | awk '{ print $2 }'`
     v=$(jps | grep $(expr $MAIN_CLASS : ".*\.\(.*\)$") | grep -v Jps | awk '{ print $1 }')
     if [ ! -z "$v" ]; then
          echo $v
     else
          echo 0
     fi
}

_pid() {
     id=$1
     count="$(ps -fe | grep $id | grep -v grep | wc -l)"
     if [ $count -eq 0 ]; then
          echo 0
     else
          echo 1
     fi

}
_nginx_precheck() {
     jemalloc_count=$(yum list installed | grep jemalloc-devel | wc -l)
     pcre_count=$(yum list installed | grep pcre-devel | wc -l)
     dos2unix_count=$(yum list installed | grep dos2unix | wc -l)
     if [[ $jemalloc_count -eq 0 || $pcre_count -eq 0 || $dos2unix_count -eq 0 ]]; then
          echo 0
     else
          echo 1
     fi
}

_nginx_installed() {
     nginx_count=$(rpm -qa | grep nginx | wc -l)
     if [ $nginx_count -eq 0 ]; then
          echo 0
     else
          echo 1
     fi
}

install_nginx() {
     precheck=$(_nginx_precheck)
     # Install Nginx Pre Required Components
     if [[ precheck -eq 0 ]]; then
          install="$(yum install -y jemalloc-devel jemalloc pcre-devel dos2unix)"
     fi
     if [[ precheck -eq 0 ]]; then
          echo 0
          return
     fi
     # Install Nginx
     nginx_precheck=$(_nginx_installed)
     if [ $nginx_precheck -eq 0 ]; then
         install="$(rpm -i ./startup/slb-nginx-1.14.1-3.el7.x86_64.rpm)"
     else
          echo 1
          return
     fi
     $(sleep 0.2m)
     nginx_precheck=$(_nginx_installed)
     if [ $nginx_precheck -eq 0 ]; then
          echo 2
          return
     fi
     echo 3
}

start_nginx() {
     log_path="/opt/logs/nginx/error.log"
     process_path="/opt/app/nginx/sbin/nginx"
     # Precheck log file exited
     file_count=$(ls $log_path | wc -l)
     if [ $file_count -ne 1 ]; then
          "$($(toch -p $log_path))"
     fi

     process=$(_pid "nginx")
     if [ $process -eq 0 ]; then
          "$($process_path)"
     fi

     process=$(_pid "nginx")
     if [ $process -eq 0 ]; then
          echo 0
     else
          echo 1
     fi
}

function replace_slb_startup() {
     j="$(which java)"
     hostip="$(ifconfig | grep "eth0" -A 2 | awk '/inet /{print $2;exit}')"
     t="http:\\/\\/$hostip:8099"
     $(sed -i "s@export API_BASE_URL=.*@export API_BASE_URL=$t@" $SLB_ADMIN_INSTALL_PATH/scripts/$SLB_ADMIN_START_SCRIPT)
     $(sed -i "s@export JAVA_PATH=.*@export JAVA_PATH=$j@" $SLB_ADMIN_INSTALL_PATH/scripts/$SLB_ADMIN_START_SCRIPT)
}
function prepare_slb_admin() {
     pathexisted=$(ls $SLB_ADMIN_INSTALL_PATH | wc -l)
     if [ $pathexisted -eq 0 ]; then
          $(mkdir $SLB_ADMIN_INSTALL_PATH -p)
     else
          $(rm -rf $SLB_ADMIN_INSTALL_PATH)
          $(mkdir $SLB_ADMIN_INSTALL_PATH -p)
     fi

     # change db
     read -p "Where is your DB Server?" dbserver
     read -p "DB Server Port?" dbport
     read -p "DB Name?" db
     read -p "DB User Name?" dbuser
     read -p "DB password?" dbpwd

     url=""
     username=""
     password=""
     if [[ -z "$dbserver" || -z "$db" || -z "$dbuser" || -z "$dbpwd" || -z "$dbport" ]]; then
          printf "DB Server, DB Name, User Name and Password are required \n"
          echo 0
          return
     else
          url="jdbc:mysql://$dbserver:$dbport/$db"
          username=$dbuser
          password=$dbpwd
     fi

     # update the connection string
     # todo: ~ is not dependable
     $(sed -i "s~url=\(.*\)\(\?.*\)~url=$url\2~" ./conf/$SLB_ENV/db.properties)
     $(sed -i "s~username=\(.*\)~username=$username~" ./conf/$SLB_ENV/db.properties)
     $(sed -i "s~password=\(.*\)~password=$dbpwd~" ./conf/$SLB_ENV/db.properties)
     #$(mvn clean package)

     count=$(ls ./target/$SLB_ADMIN_PACKAGE_NAME | wc -l)
     if [ $count -eq 1 ]; then
          $(rm -rf $SLB_ADMIN_JAR_PATH/SLB_ADMIN_PACKAGE_NAME)
          $(cp ./target/$SLB_ADMIN_PACKAGE_NAME $SLB_ADMIN_JAR_PATH/)
          $(unzip -q $SLB_ADMIN_JAR_PATH/$SLB_ADMIN_PACKAGE_NAME -d $SLB_ADMIN_INSTALL_PATH/)
          $(dos2unix -q $SLB_ADMIN_INSTALL_PATH/scripts/*)
          echo 1
     else
          echo 0
     fi
}

function start_slb_admin() {
     $(sh $SLB_ADMIN_INSTALL_PATH/scripts/$SLB_ADMIN_START_SCRIPT)
}

function java_installed() {
     p=$(which java)
     if [ ! -f "$p" ]; then
          echo 0
          return
     fi
     version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f2)
     if [ $version -lt "8" ]; then
          echo 1
          return
     fi

     echo 2
     return
}

function mvn_installed() {
     p=$(which mvn)
     if [ ! -f "$p" ]; then
          echo 0
          return
     fi
     echo 1
     return
}

function jps_installed() {
     p=$(which jps)
     if [ ! -f "$p" ]; then
          echo 0
          return
     fi
     echo 1
     return
}

j=$(java_installed)
#m=$(mvn_installed)
s=$(jps_installed)
pid=$(_jps)

case $j in
0)
     printf "Java not installed \n"
     exit 1
     ;;
1)
     printf "Java 8 is required \n"
     exit 1
     ;;
*)
     printf 'Java check passed \n'
     ;;
esac

# if [ $m -eq 0 ]; then
#      printf "Maven not installed \n"
#      exit 1
# else
#      printf "Maven check passed \n"
# fi

if [ $s -eq 0 ]; then
     printf "Jps not installed \n"
     exit 1
else
     printf "Jps check passed \n"
fi

if [ $pid -gt 0 ]; then
     echo "Slb Admin already running, PID: $pid. Would you reinstall it? 'y' / 'n'"
     read next

     case $next in
     'y')
          $(kill -9 $pid)
          ;;
     'n')
          exit 1
          ;;
     *)
          echo "No need to do anything. Exit"
          exit 1
          ;;
     esac

fi
install_nginx_success=$(install_nginx)

case $install_nginx_success in
0 | 2)
     printf "Install nginx failed \n"
     exit 1
     ;;
*)
     printf "Nginx check passed \n"
     start_nginx_success=$(start_nginx)
     if [ $start_nginx_success -eq 1 ]; then
          printf "Success to start nginx \n"
          # Package SLB and copy output
          prepare=$(prepare_slb_admin)
          # Start replace SLB Admin startup file
          $(replace_slb_startup)
          if [ $prepare -eq 1 ]; then
               $(start_slb_admin)
          else
               printf "Fail to prepare slb admin \n"
               exit 1
          fi

          # Started ?
          pid=$(_jps)
          if [ ! -n "$pid" ]; then
               $pid=0
          fi

          if [ $pid -ne 0 ]; then
               printf "Successfully Install SLB Admin \n"
          else
               exit 1
          fi
     else
          printf "Failed to start nginx \n"
     fi
     ;;
esac
