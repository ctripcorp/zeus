#!/bin/bash

APP_NAME="SlbAdmin"
SHUTDOWN_WAIT=10
MAIN_CLASS="com.ctrip.zeus.SlbAdminMain"
ENVIRONMENT_NAME="local"
CONF_BASE_URL="http://localhost:8080/config/api"

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/../" >&-
APP_HOME="`pwd -P`"
cd "$SAVED" >&-


JAVA_OPS="
-DAPP_HOME=$APP_HOME
-Darchaius.deployment.environment=${ENVIRONMENT_NAME}
-Darchaius.configurationSource.additionalUrls=${CONF_BASE_URL}/${APP_NAME}/${ENVIRONMENT_NAME}
-Dserver.www.base-dir=$APP_HOME/www
-Dserver.temp-dir=$APP_HOME/temp
-DCONF_DIR=$APP_HOME/conf/${ENVIRONMENT_NAME}
-Xms256m
-Xmx256m
-Xmn64m
-Xss256k
-XX:PermSize=64m
-XX:MaxPermSize=64m
-XX:MaxDirectMemorySize=128m

-XX:SurvivorRatio=8
-XX:-DisableExplicitGC
-XX:+UseConcMarkSweepGC
-XX:CMSInitiatingOccupancyFraction=70
-XX:+UseCMSCompactAtFullCollection

-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-XX:+PrintHeapAtGC
-Xloggc:$APP_HOME/logs/gc.log
-XX:-OmitStackTraceInFastThrow
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp

-Dfile.encoding=utf-8

-Dcom.sun.management.jmxremote.port=8082
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dsun.net.client.defaultConnectTimeout=10000
-Dsun.net.client.defaultReadTimeout=30000
"

_pid() {
    #echo `ps aux | grep $MAIN_CLASS | grep -v grep | awk '{ print $2 }'`
    echo `jps | grep $(expr $MAIN_CLASS : ".*\.\(.*\)$")| grep -v Jps | awk '{ print $1 }'`
}
 
start(){
    pid=$(_pid)
    if [ -n "$pid" ]
    then
         echo "$APP_NAME is already running (pid: $pid)"
    else
         #Start Program
         echo "Starting $APP_NAME"
         java $JAVA_OPS -classpath $(echo $APP_HOME/lib/* | tr ' ' ':') $MAIN_CLASS 1>$APP_HOME/logs/log.txt 2>$APP_HOME/logs/err.txt &
        pid=$(_pid)
        if [ -n "$pid" ]
        then
            echo "$APP_NAME started: $pid"
        else
            echo "$APP_NAME failed to start"
        fi
    fi
    return 0
}
 
stop(){
    pid=$(_pid)
    if [ -n "$pid" ]
    then
        echo "Stoping $APP_NAME"
        kill $pid
 
        let kwait=$SHUTDOWN_WAIT
        count=0;
        until [ `ps -p $pid | grep -c $pid` = '0' ] || [ $count -gt $kwait ]
        do
            echo -n -e "\nwaiting for processes to exit";
            sleep 1
            let count=$count+1;
        done
 
        if [ $count -gt $kwait ]; then
            echo -n -e "\nkilling processes which didn't stop after $SHUTDOWN_WAIT seconds"
            kill -9 $pid
        fi
    else
        echo "$APP_NAME is not running"
    fi
    return 0
}
 
case $1 in
start)
    start
    ;;
stop)
    stop
    ;;
restart)
    stop
    start
    ;;
status)
    pid=$(_pid)
    if [ -n "$pid" ]
    then
        echo "$APP_NAME is running with pid: $pid"
    else
        echo "$APP_NAME is not running"
    fi
    ;;
esac
exit 0
