require 'csv' # lib: fastercsv
require 'orientdb4r'
require 'optitron'
require 'paint/pa' # lib: paint

# file Sms.cvs adapted to remove multi-line messages and notices; find it fit Regexp I-search: [^0,]$

#SNAPSHOT_DATE='130407'
#SNAPSHOT_DATE='140425'
SNAPSHOT_DATE='140628'

UNITS = {}
USERS = {}
MEMBS = {}
PROCEDURES = {}
CUSTOMERS = {}
EVENTS = {}


class Loader < Optitron::CLI

  desc 'Imports entities'
  arg_types :boolean
  def import(with_delete=true)
    connect if @client.nil?
    delete if with_delete
    imp_units
    imp_users
    imp_membs
    imp_procedures
    imp_customers
    imp_events
  end

  desc 'Deletes all tables'
  def delete
    connect if @client.nil?
    ['Event', 'Customer', 'Procedure', 'Membership', 'User', 'Unit'].each do |ent|
        rslt = @client.command "delete from #{ent}"
        puts "delete #{ent} => #{rslt['result'][0]['value']}"
    end
  end


  private

    def connect
      @client = Orientdb4r.client
      @client.connect :database => 'smevente', :user => 'admin', :password => 'admin'
    end

    def read_csv entity
      data = CSV.read("./gae-snapshot-#{SNAPSHOT_DATE}/#{entity}.csv")
      data.shift # ignore headers on first line
      data
    end

    def imp_units
      head = { 'name'=>2, 'key'=>3, 'limitedSmss'=>1 }
      data = read_csv 'Unit'

      data.each do |row|
        doc = { '@class' => 'Unit', 'type' => 'PATIENT', 'revision' => 0, 'deleted' => false }
        head.each { |k,v| doc[k] = row[v] }
        key = doc.delete 'key'
        doc['limitedSmss'] = 0 unless doc['limitedSmss'].nil? # set all limit to '0', they are too old
        metadata = row[4].split /[&=]/
        doc['smsGateway'] = "type=sms.sluzba.cz&username=#{metadata[1]}&password=#{metadata[3]}"
        created = @client.create_document doc
        UNITS[key] = created.doc_rid
      end
      puts "--- UNITs: #{UNITS.size}"
    end


    def imp_users
      head = { 'username'=>0, 'lastLoggedIn'=>1, 'deleted'=>2, 'password'=>6, 'fullname'=>5, 'key'=>4 }
      data = read_csv 'User'

      data.each do |row|
        doc = { '@class' => 'User', 'revision' => 0 }
        head.each { |k,v| doc[k] = row[v] }
        doc['deleted'] = (doc['deleted']=='True')
        doc['password'] = "SHA:#{doc['password']}"
        # expected format: yyyy-MM-dd HH:mm:ss
        doc['lastLoggedIn'] = doc['lastLoggedIn'].gsub('T', ' ') unless doc['lastLoggedIn'].nil?
        doc['timezone'] = 'Europe/Prague'
        key = doc.delete 'key'
        created = @client.create_document doc
        USERS[key] = created.doc_rid
      end
      puts "--- USERs: #{USERS.size}"
    end

    def imp_membs
      head = { 'userId' => 0, 'unitId' => 1, 'role' => 2, 'significance' => 4, 'key' => 3 }
      data = read_csv 'Membership'

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
        created = @client.create_document doc
        MEMBS[key] = created.doc_rid
      end
      puts "--- MEMBs: #{MEMBS.size}"
    end

    def imp_procedures
      head = { 'name'=>1, 'color'=>2, 'deleted'=>4, 'time'=>7, 'typeInt'=>8, 'messageText'=>9, 'unitId'=>3, 'key'=>6 }
      data = read_csv 'MedicalHelpCategory'

      data.each do |row|
        doc = { '@class' => 'Procedure', 'revision' => 0 }
        head.each { |k,v| doc[k] = row[v] }
        doc['deleted'] = (doc['deleted']=='True')
        doc['type'] = ('1' == doc.delete('typeInt') ? 'IMMEDIATE_MESSAGE' : 'IN_CALENDAR')
        doc['messageText'] = '' if doc['messageText'].nil?

        key = doc.delete('unitId')
        rid = UNITS[key]
        raise "unit not found, key=#{key}, row=#{row}" if rid.nil?
        doc['unit'] = rid.to_s

        key = doc.delete 'key'
        created = @client.create_document doc
        PROCEDURES[key] = created.doc_rid
      end
      puts "--- PROCEDUREs: #{PROCEDURES.size}"
    end

    def imp_customers
      head = {
        'firstname'=>4, 'surname'=>3, 'upperSurname'=>2, 'deleted'=>6, 'upperFirstname'=>8, 'unitId'=>7, 'key'=>13,
        'phoneNumber'=>12, 'birthNumber'=>14, 'degree'=>5, 'street'=>11, 'city'=>1, 'zipCode'=>10,
        'employer'=>9, 'careers'=>16
      }
      data = read_csv 'Patient'

      data.each do |row|
        doc = { '@class' => 'Customer', 'revision' => 0 }
        head.each { |k,v| doc[k] = row[v] }
        doc['deleted'] = (doc['deleted']=='True')
        doc['asciiFullname'] = "#{doc.delete('upperFirstname')} #{doc.delete('upperSurname')}"

        key = doc.delete('unitId')
        rid = UNITS[key]
        raise "unit not found, key=#{key}, row=#{row}" if rid.nil?
        doc['unit'] = rid.to_s

        key = doc.delete 'key'
        created = @client.create_document doc
        CUSTOMERS[key] = created.doc_rid
      end
      puts "--- CUSTOMERs: #{CUSTOMERS.size}"
    end

    def imp_events
      head = {
        'text' => 8, 'startTime' => 10, 'length' => 2, 'key' => 7, 'notice'=>1,
        'sent'=>6, 'sendAttemptCount'=>9, 'status'=>0,
        'userId' => 3, 'customerId' => 4,  'procedureId' => 5
      }
      data = read_csv 'Sms'

      begin
        data.each do |row|
          doc = { '@class' => 'Event', 'revision' => 0, 'deleted' => false, 'type' => 'IN_CALENDAR' }
          head.each { |k,v| doc[k] = row[v] }
          status = doc.delete('status')
          doc['deleted'] = true if not status.nil? and (status.to_i & 16) > 0
          doc['type'] = 'IMMEDIATE_MESSAGE' if not status.nil? and (status.to_i & 32) > 0
          # expected format: yyyy-MM-dd HH:mm:ss
          doc['startTime'] = doc['startTime'].gsub('T', ' ') unless doc['startTime'].nil?
          doc['sent'] = doc['sent'].gsub('T', ' ') unless doc['sent'].nil?

          key = doc.delete('userId')
          rid = USERS[key]
          puts "UUU user not found, key=#{key}, row=#{row}" if rid.nil?
          next if rid.nil?
          doc['author'] = rid.to_s
          key = doc.delete('customerId')
          rid = CUSTOMERS[key]
          puts "CCC customer not found, key=#{key}, row=#{row}" if rid.nil?
          next if rid.nil?
          doc['customer'] = rid.to_s
          key = doc.delete('procedureId')
          rid = PROCEDURES[key]
          puts "PPP procedure not found, key=#{key}, row=#{row}" if rid.nil?
          next if rid.nil?
          doc['procedure'] = rid.to_s

          key = doc.delete 'key'
#puts "DOC=#{doc}"
          created = @client.create_document doc
          EVENTS[key] = created.doc_rid
        end
      rescue => e
        puts "RRRRRRRRR #{e}"
        puts e.message
        puts e.backtrace
      end
      puts "--- EVENTs: #{EVENTS.size}"
    end

end # class


Loader.dispatch
pa '[OK]', :green
