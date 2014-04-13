require 'csv' # lib: fastercsv
require 'orientdb4r'

SNAPSHOT_DATE='130407'

client = Orientdb4r.client
client.connect :database => 'smevente', :user => 'admin', :password => 'admin'


# Unit
head = { 'name' => 2, 'key' => 3 }
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/Unit.csv")
data.shift # ignore headers on first line

UNITS = {}
data.each do |row|
  doc = { '@class' => 'Unit', 'type' => 'PATIENT', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }
  key = doc.delete 'key'
  created = client.create_document doc
  UNITS[key] = created.doc_rid
end
puts "--- UNITs: #{UNITS.size}"


# User
head = { 'username' => 0, 'password' => 6, 'fullname' => 5, 'key' =>  4 }
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/User.csv")
data.shift # ignore headers on first line

USERS = {}
data.each do |row|
  doc = { '@class' => 'User', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }
  key = doc.delete 'key'
  created = client.create_document doc
  USERS[key] = created.doc_rid
end
puts "--- USERs: #{USERS.size}"


# Memberships
head = { 'userId' => 0, 'unitId' => 1, 'role' => 2, 'significance' => 4, 'key' => 3 }
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/Membership.csv")
data.shift # ignore headers on first line

MEMBS = {}
data.each do |row|
  doc = { '@class' => 'Membership', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }

  key = doc.delete('userId')
  rid = USERS[key]
  raise "user not found, key=#{key}, row=#{row}" if rid.nil?
  doc['user'] = rid.to_s
  key = doc.delete('unitId')
  rid = UNITS[key]
  raise "unit not found, key=#{key}, row=#{row}" if rid.nil?
  doc['unit'] = rid.to_s

  key = doc.delete 'key'
  created = client.create_document doc
  MEMBS[key] = created.doc_rid
end
puts "--- MEMBs: #{MEMBS.size}"


# Procedures
head = { 'name' => 1, 'color' => 2, 'time' => 7, 'typeInt' => 8, 'messageText' => 9, 'unitId' => 3, 'key' =>  6 }
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/MedicalHelpCategory.csv")
data.shift # ignore headers on first line

PROCEDURES = {}
data.each do |row|
  doc = { '@class' => 'Procedure', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }
  doc['type'] = (1 == doc.delete('typeInt') ? 'IMMEDIATE_MESSAGE' : 'IN_CALENDAR')
  doc['messageText'] = '' if doc['messageText'].nil?

  key = doc.delete('unitId')
  rid = UNITS[key]
  raise "unit not found, key=#{key}, row=#{row}" if rid.nil?
  doc['unit'] = rid.to_s

  key = doc.delete 'key'
  created = client.create_document doc
  PROCEDURES[key] = created.doc_rid
end
puts "--- PROCEDUREs: #{PROCEDURES.size}"


# Patients
head = {
  'firstname' => 4, 'surname' => 3, 'upperSurname' => 2, 'upperFirstname' => 8, 'unitId' => 7, 'key' => 13,
  'phoneNumber' => 12, 'birthNumber' => 14 # TODO
}
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/Patient.csv")
data.shift # ignore headers on first line

PATIENTS = {}
data.each do |row|
  doc = { '@class' => 'Patient', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }
  doc['asciiFullname'] = "#{doc.delete('upperFirstname')} #{doc.delete('upperSurname')}"

  key = doc.delete('unitId')
  rid = UNITS[key]
  raise "unit not found, key=#{key}, row=#{row}" if rid.nil?
  doc['unit'] = rid.to_s

  key = doc.delete 'key'
  created = client.create_document doc
  PATIENTS[key] = created.doc_rid
end
puts "--- PATIENTs: #{PATIENTS.size}"


# Events
head = {
  'text' => 8, 'startTime' => 10, 'length' => 2, 'key' => 7, # TODO
  'userId' => 3, 'patientId' => 4,  'procedureId' => 5
}
data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/Sms.csv")
data.shift # ignore headers on first line

EVENTS = {}
begin
data.each do |row|
#puts "row=#{row}"
  doc = { '@class' => 'Event', 'revision' => 0, 'deleted' => false }
  head.each { |k,v| doc[k] = row[v] }

  unless doc['startTime'].nil?
    time = DateTime.strptime(doc['startTime'], '%Y-%m-%dT%H:%M:%S')
    doc['startTime'] = time.strftime('%Y-%m-%d %H:%M:%S:000')
  end

  key = doc.delete('userId')
  rid = USERS[key]
  puts "WAWARN, user not found, key=#{key}, row=#{row}" if rid.nil?
  doc['author'] = rid.to_s
  key = doc.delete('patientId')
  rid = PATIENTS[key]
  puts "WAWARN, patient not found, key=#{key}, row=#{row}" if rid.nil?
  doc['patient'] = rid.to_s
  key = doc.delete('procedureId')
  rid = PROCEDURES[key]
  puts "WAWARN, procedure not found, key=#{key}, row=#{row}" if rid.nil?
  doc['procedure'] = rid.to_s

  key = doc.delete 'key'
#puts "DOC=#{doc}"
  created = client.create_document doc
  EVENTS[key] = created.doc_rid
end
rescue => e
  puts "RRRRRRRRR"
  puts e.message
  puts e.backtrace
end
puts "--- EVENTs: #{EVENTS.size}"
