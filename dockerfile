FROM ubuntu:14.04 

RUN \ 
	apt-get update && \ 
	apt-get install -y software-properties-common && \ 
	echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
	add-apt-repository -y ppa:webupd8team/java && \
	apt-get update && \
	apt-get install -y oracle-java8-installer && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer 

WORKDIR /data
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN apt-get update

WORKDIR /root
RUN wget -o my_log http://mirrors.sonic.net/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
RUN tar xzvf apache-maven-3.3.9-bin.tar.gz
ENV M2_HOME=/root/apache-maven-3.3.9
ENV PATH=$PATH:$M2_HOME/bin

RUN apt-get -y install lsof

WORKDIR /root
RUN wget -o my_log http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz
RUN tar xzf solr-6.2.1.tgz solr-6.2.1/bin/install_solr_service.sh --strip-components=2
RUN ./install_solr_service.sh solr-6.2.1.tgz
RUN rm -f solr-6.2.1.tgz && rm -f install_solr_service.sh
	
EXPOSE 8983
EXPOSE 8084

ADD src /root/w6-access/src
ADD pom.xml /root/w6-access

WORKDIR /root/w6-access
RUN mvn -q package

WORKDIR /opt/solr-6.2.1/bin
CMD ./solr start -p 8983 && ./solr create -c core && ./solr create -c events && java -jar /root/w6-access/target/w6.war
