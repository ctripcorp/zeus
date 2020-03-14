#!/bin/bash

## [START] APP_HOME initialization
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
## [END] APP_HOME initialization

## [START] JPS link initialization
# ln jps if necessary
if ! [ -L /usr/bin/jps ] ; then
    if ! [ -e /usr/bin/jps ] ; then
        sudo ln -s /usr/java/default/bin/jps /usr/bin/jps
    fi
fi
## [END] JPS link initialization


## [START] Construct JVM OPS
#Default JVM Options
if [[ -z $JAVA_OPT_Xms ]]; then
    JAVA_OPT_Xms="5g"
fi
if [[ -z $JAVA_OPT_Xmx ]]; then
    JAVA_OPT_Xmx="5g"
fi
if [[ -z $JAVA_OPT_Xmn ]]; then
    JAVA_OPT_Xmn="2g"
fi
if [[ -z $JAVA_OPT_MaxDirectMemorySize ]]; then
    JAVA_OPT_MaxDirectMemorySize="1024m"
fi
if [[ -z $JAVA_PATH ]]; then
    JAVA_PATH="java"
fi

JAVA_OPS="
-DAPP_HOME=$APP_HOME
-Dagent.api.host=${API_BASE_URL}
-Dslb.config.url=${API_BASE_URL}/api/config/all
-Dserver.www.base-dir=$APP_HOME/web
-Dserver.temp-dir=$APP_HOME/temp
-DCONF_DIR=$APP_HOME/conf/${ENVIRONMENT_NAME}
-Xms${JAVA_OPT_Xms}
-Xmx${JAVA_OPT_Xmx}
-Xmn${JAVA_OPT_Xmn}
-Xss256k
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=256m
-XX:MaxDirectMemorySize=${JAVA_OPT_MaxDirectMemorySize}

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
-Dslb.role=all

-Dcom.sun.management.jmxremote.port=8082
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dsun.net.client.defaultConnectTimeout=10000
-Dsun.net.client.defaultReadTimeout=30000
-Dsun.net.http.allowRestrictedHeaders=true
"
## [END] Construct JVM OPS

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
        $JAVA_PATH $JAVA_OPS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8056 -classpath $(echo $APP_HOME/lib/* | tr ' ' ':') $MAIN_CLASS 1>$APP_HOME/logs/log.txt 2>$APP_HOME/logs/err.txt &
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
