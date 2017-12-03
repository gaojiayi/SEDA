#!/bin/bash
# add by yougw at 2016-8-4

#log4j2 config file
v_log4j_path=${SEDA_HOME}/config/seda/log4j2-server.xml

#lib jar
SEDA_LIB=${SEDA_HOME}/lib

#class path
APPCLASSPATH=$CLASSPATH:$({ \
    \ls -1 "$SEDA_LIB"/*.jar; \
    \ls -1 "$SEDA_LIB"/esblib/*.jar; \
} 2> /dev/null | paste -sd ':' - )

$JAVA_HOME/bin/java -classpath $APPCLASSPATH -Dlog4j.configurationFile=${v_log4j_path} com.ai.aif.seda.service.SedaService &

#see log
#tail -f ${SEDA_HOME}/log/*.ing
