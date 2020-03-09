#! /bin/bash
touch /tmp/run.out
java -jar /tmp/RunUt-1.0-SNAPSHOT.jar > /tmp/run.out 2> /dev/null < /dev/null &
echo "application already start"
