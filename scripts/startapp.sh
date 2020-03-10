#! /bin/bash
touch /tmp/run.out
java -jar /tmp/webapp.jar > /tmp/run.out 2> /dev/null < /dev/null &
echo "application already start"
