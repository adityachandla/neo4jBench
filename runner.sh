rm -rf /tmp/jars
mkdir /tmp/jars
mv /var/lib/neo4j/plugins/*.jar /tmp/jars

mvn clean compile

mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a BFS -m lowLevel" 1>> ll_bfs.txt
mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a DFS -m lowLevel" 1>> ll_dfs.txt
echo "Finished low level"

mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a BFS -m traversal" 1>> traversal_bfs.txt
mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a DFS -m traversal" 1>> traversal_dfs.txt
echo "Finished traversal"

mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a BFS -m cypher" 1>> cypher.txt
echo "Finished cypher"

mv /tmp/jars/*.jar /var/lib/neo4j/plugins/

neo4j start
sleep 5
mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a BFS -m gds" 1>> gds_bfs.txt
mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a DFS -m gds" 1>> gds_dfs.txt
echo "Finished GDS"

mvn exec:java -Dexec.mainClass='org.tue.neobench.Main' -Dexec.args="-a BFS -m pregel" 1>> pregel.txt
echo "Finished pregel"
neo4j stop
