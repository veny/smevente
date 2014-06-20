# Set a root password
## $ passwd
# Update system
## $ yum update
# copy Oracle JDK
## $ mkdir /opt/download
## $ scp ~/Downloads/jdk-8u5-linux-x64.tar.gz root@smevente.me:/opt/download/

require 'openssl'

RUBY_VERSION = '2.1.1'
JAVA_VERSION = ['8u5', '1.8.0_05']
TOMCAT_VERSION = '7.0.54'
ORIENTDB_VERSION = '1.7.3'
ORIENTDB_ROOT_PASSWD = "\xB9\xB8]\x013SM\x87U96\rIu\xF6\xA8>\x0Fq\x05\xF0\xB3#\x1C\xC31?L\xBD{\xE6="
TOMCAT_ADMIN_PASSWD = "\xE4\x03\xDC\xE4r> u\x06K\xE4\xBDK\xCF:\nd\xBFk\xB1\x11y\xF6\xF4\xAA\x83jt2\x1E\x94\xF1"


##################################################
# HELPER STUFF

module Helper
  def Helper.ask_for_password(prompt = "Enter password: ")
    raise 'Could not ask for password because there is no interactive terminal (tty)' unless $stdin.tty?
    unless prompt.nil?
      $stderr.print prompt 
      $stderr.flush
    end
    raise 'Could not disable echo to ask for password security' unless system 'stty -echo -icanon'
    password = $stdin.gets
    password.chomp! if password
    password
  ensure
    raise 'Could not re-enable echo while securely asking for password' unless system 'stty echo icanon'
  end

  def Helper.decrypt(encrypted_data, key)
    aes = OpenSSL::Cipher::Cipher.new 'AES-128-ECB'
    aes.decrypt
    aes.key = key * 3
    aes.update(encrypted_data) + aes.final
  end
end

MASTER_PASSWD = Helper.ask_for_password


##################################################
# GENERATED FILES

TOMCAT_INIT_SCRIPT = <<TIS
#!/bin/bash
# chkconfig: 2345 80 20
# description: This shell script takes care of starting and stopping Tomcat
 
export JAVA_HOME=/opt/java
export JAVA_OPTS="-Dfile.encoding=UTF-8 -XX:MaxPermSize=128m -Xms512m -Xmx512m"
export PATH=$JAVA_HOME/bin:$PATH
TOMCAT_HOME=/opt/tomcat
TOMCAT_OWNER=java
SHUTDOWN_WAIT=20

tomcat_pid() {
  echo `ps aux | grep org.apache.catalina.startup.Bootstrap | grep -v grep | awk '{ print $2 }'`
}

start() {
  pid=$(tomcat_pid)
  if [ -n "$pid" ] 
  then
    echo "Tomcat is already running (pid: $pid)"
  else
    # Start tomcat
    echo "Starting tomcat"
    ulimit -n 100000
    umask 007
    /bin/su -p -s /bin/sh $TOMCAT_OWNER $TOMCAT_HOME/bin/startup.sh
  fi
 
 
  return 0
}
 
stop() {
  pid=$(tomcat_pid)
  if [ -n "$pid" ]
  then
    echo "Stoping Tomcat"
    /bin/su -p -s /bin/sh $TOMCAT_OWNER $TOMCAT_HOME/bin/shutdown.sh
 
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
    echo "Tomcat is not running"
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
  pid=$(tomcat_pid)
  if [ -n "$pid" ]
  then
    echo "Tomcat is running with pid: $pid"
  else
    echo "Tomcat is not running"
  fi
;; 
*)
  echo "Usage: $0 {start|stop|restart|status}"
  exit 1
esac    
exit 0
TIS

SINATRA_INIT_SCRIPT = <<SIS
#!/bin/bash
# chkconfig: 2345 80 20
# description: Startup script for the Smevente presentation.

MUSER=ruby
DIR=/home/$MUSER/smevente_www
CMD=/home/$MUSER/.rvm/bin/bootup_smevente_www

start(){
	cd $DIR
	su -c "$CMD smevente.rb >> /var/log/smevente_www/app.log 2>&1 &" $MUSER
}
stop(){
	RUBYPID=`ps aux | grep "ruby smevente.rb" | grep -v grep | awk '{print $2}'`
	if [ "x$RUBYPID" != "x" ]; then
		kill -2 $RUBYPID
	fi	
}
status(){
	RUBYPID=`ps aux | grep "ruby smevente.rb" | grep -v grep | awk '{print $2}'`
	if [ "x$RUBYPID" = "x" ]; then
		echo "* Smevente_WWW is NOT running"
	else
		echo "* Smevente_WWW is running"
	fi
}
case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	status)
		status
		;;
	restart|force-reload)
		stop
		start
		;;
	*)
		echo "Usage: $0 {start|stop|restart|force-reload|status}"
		exit 1
esac
SIS

ORIENTDB_INIT_SCRIPT = <<OIS
#!/bin/bash
# chkconfig: 2345 80 20
# description: Startup script for the OrientDB database.

ORIENTDB_DIR=/opt/orientdb
ORIENTDB_USER=java
export JAVA_HOME=/opt/java
export JAVA_OPTS="-Dfile.encoding=UTF-8 -XX:MaxPermSize=128m -Xms512m -Xmx512m"
export PATH=$JAVA_HOME/bin:$PATH

usage() {
	echo "Usage: `basename $0`: <start|stop|status>"
	exit 1
}

start() {
	status
	if [ $PID -gt 0 ]
	then
		echo "OrientDB server daemon was already started. PID: $PID"
		return $PID
	fi
	echo "Starting OrientDB server daemon..."
	cd $ORIENTDB_DIR/bin
	su -c "cd $ORIENTDB_DIR/bin; /usr/bin/nohup ./server.sh 1>../log/orientdb.log 2>../log/orientdb.err &" $ORIENTDB_USER
}

stop() {
	status
	if [ $PID -eq 0 ]
	then
		echo "OrientDB server daemon is already not running"
		return 0
	fi
	echo "Stopping OrientDB server daemon..."
	cd $ORIENTDB_DIR/bin
	su -c "cd $ORIENTDB_DIR/bin; /usr/bin/nohup ./shutdown.sh 1>>../log/orientdb.log 2>>../log/orientdb.err &" $ORIENTDB_USER
}

status() {
	PID=`ps -ef | grep 'orientdb.www.path' | grep java | grep -v grep | awk '{print $2}'`
	if [ "x$PID" = "x" ]
	then
		PID=0
	fi
	
	# if PID is greater than 0 then OrientDB is running, else it is not
	return $PID
}

if [ "x$1" = "xstart" ]
then
	start
	exit 0
fi

if [ "x$1" = "xstop" ]
then
	stop
	exit 0
fi

if [ "x$1" = "xstatus" ]
then
	status
	if [ $PID -gt 0 ]
	then
		echo "OrientDB server daemon is running with PID: $PID"
	else
		echo "OrientDB server daemon is NOT running"
	fi
	exit $PID
fi

usage
OIS


##################################################
# SPRINKLE DEFINITION

package :system do
  description 'Configures system'

  # time zone
  runner 'rm /etc/localtime'
  runner 'ln -s /usr/share/zoneinfo/Europe/Prague /etc/localtime'

  # download directory
  runner 'mkdir -p /opt/download'
  runner 'chmod o+rwx /opt/download'

  # EPEL repository
  file = 'epel-release-6-8.noarch.rpm'
  runner "if [ ! -f '/opt/download/#{file}' ]; then cd /opt/download; wget http://dl.fedoraproject.org/pub/epel/6/x86_64/#{file}; fi"
  runner "rpm -Uvh --force /opt/download/#{file}"

  # tools
  yum 'nano tree'

  # user 'veny'
  runner 'id -u veny &>/dev/null || useradd -g users -m -s /bin/bash veny'
  # user 'ruby'
  runner 'groupadd -f ruby'
  runner 'id -u ruby &>/dev/null || useradd -g ruby -m -s /bin/bash ruby'
  # user 'java'
  runner 'groupadd -f java'
  runner 'id -u java &>/dev/null || useradd -g java -m -s /bin/bash java'

  # ntp
  #yum 'ntp'
  #runner 'chkconfig ntpd on'

  # SSH login without password
  runner 'mkdir -p /root/.ssh'
  runner 'chmod 700 /root/.ssh'
  transfer '/home/veny/.ssh/smevente-vps_rsa.pub', '/root/.ssh/'
  runner 'cat /root/.ssh/smevente-vps_rsa.pub > /root/.ssh/authorized_keys'
  runner 'chmod 400 /root/.ssh/*'
end

package :apache do
  description 'Installs Apache2 stuff'
  requires :system

  yum 'httpd'
  runner 'chkconfig httpd on'
  # remove welcome page
  runner 'rm -f /etc/httpd/conf.d/welcome.conf'
  # remove default error page
  runner 'rm -f /var/www/error/noindex.html'

  # configuration (http://www.server-world.info/en/note?os=CentOS_6&p=httpd)
  replace_text '^KeepAlive Off.*', "KeepAlive On", '/etc/httpd/conf/httpd.conf'
  # turns on name-based host resolution
  replace_text '^#NameVirtualHost.*', "NameVirtualHost *:80", '/etc/httpd/conf/httpd.conf'
end

##################################### PRESENTATION

package :ruby do
  description 'Installs Ruby'
  requires :system

  # RVM
  # install if not installed
  runner "su - ruby -c \"[[ -f '/home/ruby/.rvm/bin/rvm' ]] || curl -L get.rvm.io | bash -s stable\""
  # upgrade if installed
  runner "su - ruby -c \"if [ -f '/home/ruby/.rvm/bin/rvm' ]; then rvm get stable; fi\""

  # Ruby (https://gist.github.com/mustafaturan/2584126)
  runner 'yum -y groupinstall "Development Tools"'
  yum 'libxslt-devel libyaml-devel libxml2-devel gdbm-devel libffi-devel zlib-devel openssl-devel libyaml-devel readline-devel curl-devel openssl-devel pcre-devel git memcached-devel valgrind-devel mysql-devel ImageMagick-devel ImageMagick'

  runner "su - ruby -c 'rvm install #{RUBY_VERSION}'"
  runner "su - ruby -c 'rvm use #{RUBY_VERSION}@www --create'"

  # Gems
  runner "su - ruby -c 'rvm use #{RUBY_VERSION}@www; gem update'"
  runner "su - ruby -c 'rvm use #{RUBY_VERSION}@www; gem install sinatra --no-ri --no-rdoc'"
end

package :presentation do
  description 'Sets up the WWW presentation'
  requires :apache
  requires :ruby

  # copy presentation
  transfer '/home/veny/projects/smevente_www/', '/home/ruby'
  runner 'chown -R ruby:ruby /home/ruby/smevente_www/'
  runner 'find /home/ruby/smevente_www/ -type d -exec chmod 750 {} \;'
  runner 'find /home/ruby/smevente_www/ -type f -exec chmod 640 {} \;'
  # test it
  #  ruby> rvm use x.y.z@www
  #  ruby> cd ~/smevente_www/; ruby smevente.rb
  #  ruby> curl http://localhost:4567 | grep "title.*SmeventE"


  # virtual host
  conf = <<CONF
<VirtualHost *:80>
    ServerName www.smevente.com
    ServerAlias smevente.com
    ErrorLog /var/log/httpd/www.smevente.com-error.log
    CustomLog /var/log/httpd/www.smevente.com-access.log combined
    ProxyRequests Off
    ProxyPass / http://127.0.0.1:4567/
    ProxyPassReverse / http://127.0.0.1:4567/
</VirtualHost>
CONF
  file '/etc/httpd/conf.d/www_smevente_com.conf', :content=>conf

  # Ruby App init script
  runner "su - ruby -c \"rvm wrapper #{RUBY_VERSION}@www bootup ruby\""
  runner 'su - ruby -c "mv /home/ruby/.rvm/bin/bootup_ruby /home/ruby/.rvm/bin/bootup_smevente_www"'

  file '/etc/init.d/smevente_www', :content=>SINATRA_INIT_SCRIPT
  runner 'chmod +x /etc/init.d/smevente_www'
  runner 'chkconfig smevente_www on'
  runner 'mkdir -p /var/log/smevente_www'
  runner 'chown -R ruby:ruby /var/log/smevente_www'
end

############################################## APP

package :java do
  description 'Installs Ruby'
  requires :system

  runner "if [ ! -d '/opt/jdk#{JAVA_VERSION[1]}' ]; then tar xfvz /opt/download/jdk-#{JAVA_VERSION[0]}-linux-x64.tar.gz -C /opt/; fi"
  runner "ln -f -s /opt/jdk#{JAVA_VERSION[1]} /opt/java"
  push_text "JAVA_HOME=/opt/java\nPATH=$PATH:$JAVA_HOME/bin\nexport PATH JAVA_HOME", '/etc/profile'

  # Tomcat
  runner "if [ ! -f '/opt/download/apache-tomcat-#{TOMCAT_VERSION}.zip' ]; then cd /opt/download; wget http://mirror.hosting90.cz/apache/tomcat/tomcat-7/v#{TOMCAT_VERSION}/bin/apache-tomcat-#{TOMCAT_VERSION}.zip; fi"
  runner "if [ ! -d '/opt/apache-tomcat-#{TOMCAT_VERSION}' ]; then cd /opt; unzip /opt/download/apache-tomcat-#{TOMCAT_VERSION}.zip; fi"
  runner "chown -R java:java /opt/apache-tomcat-#{TOMCAT_VERSION}"
  runner "chmod u+x /opt/apache-tomcat-#{TOMCAT_VERSION}/bin/*.sh"
  runner "ln -f -s /opt/apache-tomcat-#{TOMCAT_VERSION} /opt/tomcat"
  # setup user accounts
  conf = %Q{<!-- AAA REPLACED --><tomcat-users>
  <role rolename="manager-gui" />
  <user username="manager" password="#{Helper.decrypt(TOMCAT_ADMIN_PASSWD, MASTER_PASSWD)}" roles="manager-gui" />
  <role rolename="admin-gui" />
  <user username="admin" password="#{Helper.decrypt(TOMCAT_ADMIN_PASSWD, MASTER_PASSWD)}" roles="manager-gui,admin-gui" />}
  replace_text '^<tomcat-users>', conf, '/opt/tomcat/conf/tomcat-users.xml'

  # TODO: logging

  # virtual host
  conf = <<CONF
<VirtualHost *:80>
    ServerName app.smevente.com
    ErrorLog /var/log/httpd/app.smevente.com-error.log
    CustomLog /var/log/httpd/app.smevente.com-access.log combined
    ProxyRequests Off
    ProxyPass / http://127.0.0.1:8080/
    ProxyPassReverse / http://127.0.0.1:8080/
</VirtualHost>
CONF
  file '/etc/httpd/conf.d/app_smevente_com.conf', :content=>conf

  # Tomcat init script
  file '/etc/init.d/tomcat', :content=>TOMCAT_INIT_SCRIPT
  runner 'chmod +x /etc/init.d/tomcat'
  runner 'chkconfig tomcat on'

  # undeploy inbound application
  runner 'rm -rf /opt/tomcat/webapps/ROOT'
  runner 'rm -rf /opt/tomcat/webapps/docs'
  runner 'rm -rf /opt/tomcat/webapps/examples'
end

package :orientdb do
  description 'Installs OrientDB'
  requires :java

  runner "if [ ! -f '/opt/download/orientdb-community-#{ORIENTDB_VERSION}.tar.gz' ]; then cd /opt/download; wget http://www.orientdb.org/portal/function/portal/download/unknown@unknown.com/-/-/-/-/-/orientdb-community-#{ORIENTDB_VERSION}.tar.gz/false/false/linux -O orientdb-community-#{ORIENTDB_VERSION}.tar.gz; fi"
  runner "if [ ! -d '/opt/orientdb-community-#{ORIENTDB_VERSION}' ]; then cd /opt; tar xvfz /opt/download/orientdb-community-#{ORIENTDB_VERSION}.tar.gz; fi"
  runner "chown -R java:java /opt/orientdb-community-#{ORIENTDB_VERSION}"
#  runner "chmod u+x /opt/apache-tomcat-#{TOMCAT_VERSION}/bin/*.sh"
  runner "ln -f -s /opt/orientdb-community-#{ORIENTDB_VERSION} /opt/orientdb"

  # init script
  file '/etc/init.d/orientdb', :content=>ORIENTDB_INIT_SCRIPT
  runner 'chmod +x /etc/init.d/orientdb'
  runner 'chkconfig orientdb on'

  # setup account
  conf = "<user resources=\"*\" password=\"#{Helper.decrypt(ORIENTDB_ROOT_PASSWD, MASTER_PASSWD)}\" name=\"root\"/><!-- AAA REPLACED -->"
  replace_text '<user .*name="root".*', conf, '/opt/orientdb/config/orientdb-server-config.xml'
end

package :app do
  description 'Sets up the Smevente application'
  requires :java
  requires :orientdb
end


policy :desktop, :roles=>:rte do
  requires :presentation
  requires :app
end


deployment do
  delivery :ssh do
    user 'root'
    password MASTER_PASSWD

    role :rte, 'smevente.me'
  end
end


# TODO:
# * backups
# * firewall
# * SSH configuration
# * monitoring
# * Tomcat security settings
