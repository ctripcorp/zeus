#!/bin/bash
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


export APP_NAME="SlbAdmin"
export SHUTDOWN_WAIT=10
export MAIN_CLASS="com.ctrip.zeus.SlbAdminMain"
export JAVA_PATH="/usr/java/jdk1.8/bin/java"
export API_BASE_URL="http://127.0.0.1:8099"
export ENVIRONMENT_NAME="open-source"

chmod +x $APP_HOME/scripts/slb-admin-open-source.sh
$APP_HOME/scripts/slb-admin-open-source.sh start