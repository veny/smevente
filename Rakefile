require 'rake/clean'

PROJECT_NAME = 'smevente'

JAVA_HOME=ENV['JAVA_HOME']
ORIENTDB_HOME='/opt/orientdb-svn/releases/orientdb-1.3.0-SNAPSHOT/lib/'
GWT_HOME='/opt/eclipse-jee-indigo-SR1-linux-gtk-x86_64/plugins/com.google.gwt.eclipse.sdkbundle_2.4.0.v201206290132-rel-r37/gwt-2.4.0/'

srcDir = 'src'
buildDir = 'rbuild'
distDir = "#{buildDir}/dist"
classesDir = "#{buildDir}/classes"

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
    sources.each {|t| f.puts "#{t}" }
  end

  cp = buildtimeLibs.join(":")
  command =<<EOF
#{JAVA_HOME}/bin/javac -cp #{cp} -deprecation -d #{classesDir} -sourcepath #{srcDir} @#{buildDir}/sources.txt
EOF
  puts `#{command}`
end


desc 'Create a war file to be deployed.'
task :war => [:compile, distDir] do
  command =<<EOF
#{JAVA_HOME}/bin/jar -cf #{buildDir}/dist/#{PROJECT}.war
EOF

end


desc "Deploy source to server"
task :Deploy => :classes do
  puts "Deploying!!!"
end


# ---------------------------------------------------------------- Helper Stuff

def copyToDir(fileArray, outputDir)
  fileArray.each { |file| cp file, outputDir }
end
