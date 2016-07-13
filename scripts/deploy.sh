#!/usr/bin/env bash

TIMEOUT_LIMIT=100
TOMCAT_WEBAPPS_DIR="/var/lib/tomcat8/webapps"
CONSOLE_STATUS_CHECK="localhost:8080/console/api/status"

function stop_tomcat() {
    echo "Shutting down tomcat..."
    sudo systemctl stop tomcat8
    while ps aux | grep "[c]atalina.base=/var/lib/tomcat8"; do
        sleep 1;
    done

    echo "Clearing webapps..."
    sudo rm -rfv ${TOMCAT_WEBAPPS_DIR}/console
    sudo rm -fv ${TOMCAT_WEBAPPS_DIR}/console.war
}

function start_tomcat() {
    echo "Starting tomcat..."
    sudo systemctl start tomcat8

    export http_proxy=''; # disable proxy for session
    status="NOT OK"
    count=0
    count_by=5
    while [[ (${status} != OK)  && (${count} -lt ${TIMEOUT_LIMIT}) ]]; do
        status=`curl --silent --max-time 1 --header "content-type: application/json" ${CONSOLE_STATUS_CHECK}`
        echo "Will wait for tomcat to start in " `expr ${TIMEOUT_LIMIT} - ${count}` " s..."
        sleep ${count_by}
        let count=${count}+${count_by};
    done

    if [[ ${count} -eq ${TIMEOUT_LIMIT} ]]; then
        echo "Tomcat did not start in allowed limit!"
        return -1;
    fi

    echo "Tomcat is up and running."
}

function deploy_console() {
    echo "Deploying console..."
    cp -v "console.war" -t ${TOMCAT_WEBAPPS_DIR}
}

stop_tomcat
deploy_console
start_tomcat
