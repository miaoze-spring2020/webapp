#! /bin/bash
touch /tmp/webapp.out
touch /tmp/webapp.pid
java -jar /tmp/webapp.jar > /tmp/run.out 2> /dev/null < /dev/null &
echo $! > /tmp/webapp.pid
echo "application already start"
