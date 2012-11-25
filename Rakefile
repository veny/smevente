require 'rake/clean'

PROJECT_NAME = 'smevente'

JAVA_HOME=ENV['JAVA_HOME']
ORIENTDB_HOME='/opt/orientdb-svn/releases/orientdb-1.3.0-SNAPSHOT/lib'
GWT_HOME='/opt/eclipse-jee-indigo-SR1-linux-gtk-x86_64/plugins/com.google.gwt.eclipse.sdkbundle_2.4.0.v201206290132-rel-r37/gwt-2.4.0'
TOMCAT_HOME='/opt/tomcat'

srcDir = 'src'
buildDir = 'rbuild'
distDir = "#{buildDir}" # TODO delete?
classesDir = "#{distDir}/war/WEB-INF/classes"

buildtimeLibs = FileList['war/WEB-INF/lib/**/*.jar', "#{GWT_HOME}/gwt-user.jar", "#{GWT_HOME}/gwt-dev.jar", "#{ORIENTDB_HOME}/*jar"]
RUNTIME_LIB_DIR = 'lib/runtime'


# use the built in CLEAN/CLOBBER tasks
CLEAN = FileList[classesDir]
CLOBBER = FileList[buildDir]


directory classesDir
directory distDir

task :default => [:compile]


desc 'Compiles all Java sources files in the project.'
task :compile => [classesDir] do
  sources = FileList["#{srcDir}/**/*.java"]

  # see the javac command line docs regarding @ syntax for javac
  File.open("#{buildDir}/sources.txt", 'w+') do |f|
    sources.each { |t| f.puts "#{t}" }
  end

  cp = buildtimeLibs.join(":")
  command =<<EOF
#{JAVA_HOME}/bin/javac -cp #{cp} -deprecation -d #{classesDir} -sourcepath #{srcDir} @#{buildDir}/sources.txt
EOF
  sh command
end


desc 'Create a war file to be deployed.'
task :war => [:compile, distDir] do
  inp = FileList['war/**/*.*', ""]
  inp.exclude('war/WEB-INF/classes/**/*.*', 'war/WEB-INF/deploy/**/*.*')
  inp.exclude('war/WEB-INF/lib/gwt-servlet-deps.jar')
  cp_rr(inp, "#{buildDir}")

  cp_rr(FileList["#{srcDir}/appctx-*.xml"], "#{distDir}/war/WEB-INF/classes", true)
  cp("#{srcDir}/log4j-rte.properties", "#{distDir}/war/WEB-INF/classes/log4j.properties", :verbose => true)

  cp("#{ORIENTDB_HOME}/orient-commons-1.3.0-SNAPSHOT.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)
  cp("#{ORIENTDB_HOME}/orientdb-core-1.3.0-SNAPSHOT.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)
  cp("#{ORIENTDB_HOME}/orientdb-object-1.3.0-SNAPSHOT.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)
  cp("#{ORIENTDB_HOME}/orientdb-client-1.3.0-SNAPSHOT.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)
  cp("#{ORIENTDB_HOME}/orientdb-enterprise-1.3.0-SNAPSHOT.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)
  cp("#{ORIENTDB_HOME}/javassist.jar", "#{distDir}/war/WEB-INF/lib", :verbose => true)

  command =<<EOF
#{JAVA_HOME}/bin/jar -cf #{buildDir}/#{PROJECT_NAME}.war -C #{buildDir}/war/ .
EOF
  sh command
end


desc "Deploy source to server"
task :deploy => :war do
  rm_r "#{TOMCAT_HOME}/webapps/#{PROJECT_NAME}/" if File.directory? "#{TOMCAT_HOME}/webapps/#{PROJECT_NAME}/"
  cp("#{buildDir}/#{PROJECT_NAME}.war", "#{TOMCAT_HOME}/webapps/")
end


# ---------------------------------------------------------------- Helper Stuff

def cp_rr(fileList, targetBaseDir, flat = false)
  fileList.each do |file|
    into = (flat ? targetBaseDir : "#{targetBaseDir}/#{File.dirname(file)}")
    mkdir_p into unless File.exist? into
    cp(file, into, :verbose => true) if File.file? file
  end
end

#<target name="gwtc" depends="javac" description="GWT compile to JavaScript (production mode)">
#   <java failonerror="true" fork="true" classname="com.google.gwt.dev.Compiler">
#     <classpath>
#       <pathelement location="src"/>
#       <pathelement path="src:/opt/orientdb-svn/trunk/core/src/main/java"/>
#       <pathelement path="/tmp/src.jar"/>
#       <path refid="project.class.path"/>
#       <pathelement location="/opt/eclipse-jee-indigo-SR1-linux-gtk-x86_64/plugins/com.google.gwt.eclipse.sdkbundle_2.4.0.v201201120043-rel-r37/gwt-2.4.0/validation-api-1.0.0.GA.jar" />
#       <pathelement location="/opt/eclipse-jee-indigo-SR1-linux-gtk-x86_64/plugins/com.google.gwt.eclipse.sdkbundle_2.4.0.v201201120043-rel-r37/gwt-2.4.0/validation-api-1.0.0.GA-sources.jar" />
#     </classpath>
#     <!-- add jvmarg -Xss16M or similar if you see a StackOverflowError -->
#     <jvmarg value="-Xmx256M"/>
#     <arg line="-war"/>
#     <arg value="war"/>
#     <!-- Additional arguments like -style PRETTY or -logLevel DEBUG -->
#     <arg line="${gwt.args}"/>
#     <arg value="veny.smevente.Smevente"/>
#   </java>
# </target>
