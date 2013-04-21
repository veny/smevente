SCHEMA = [
  ['AbstractEntity', { :abstract => true, :properties => {
      'deleted'  => { :type => :boolean },
      'revision' => { :type => :string }
    }}
  ],
  ['User', { :extends => 'AbstractEntity', :properties => {
      'username'     => { :type => :string, :mandatory => true, :notnull => true },
      'password'     => { :type => :string, :mandatory => true, :notnull => true },
      'fullname'     => { :type => :string, :mandatory => true, :notnull => true },
      'lastLoggedIn' => { :type => :datetime },
      'root'         => { :type => :boolean }
    }}
  ],
  ['Unit', { :extends => 'AbstractEntity', :properties => {
      'name' => { :type =>  :string, :mandatory => true, :notnull => true },
      'description' => { :type =>  :string },
      'type' => { :type =>  :string, :mandatory => true, :notnull => true }
    }}
  ],
  ['Membership', { :extends => 'AbstractEntity', :properties => {
      'user' => { :type => :link, :linked_class => 'User', :mandatory => true },
      'unit' => { :type => :link, :linked_class => 'Unit', :mandatory => true },
      'role' => { :type => :string, :mandatory => true, :notnull => true },
      'significance' => { :type => :integer }
    }}
  ],
  ['Patient', { :extends => 'AbstractEntity', :properties => {
      'unit' => { :type => :link, :linked_class => 'Unit', :mandatory => true },
      'firstname' => { :type => :string },
      'surname' => { :type => :string, :mandatory => true, :notnull => true },
      'asciiFullname' => { :type => :string, :mandatory => true, :notnull => true },
      'phoneNumber' => { :type => :string },
      'birthNumber' => { :type => :string },
      'degree' => { :type => :string },
      'street' => { :type => :string },
      'city' => { :type => :string },
      'zipCode' => { :type => :string },
      'employer' => { :type => :string },
      'careers' => { :type => :string }
    }}
  ],
  ['Procedure', { :extends => 'AbstractEntity', :properties => {
      'unit' => { :type => :link, :linked_class => 'Unit', :mandatory => true },
      'name' => { :type => :string, :mandatory => true, :notnull => true },
      'messageText' => { :type => :string, :mandatory => true, :notnull => true },
      'type' => { :type => :string },
      'color' => { :type => :string },
      'time' => { :type => :integer }
    }}
  ],
  ['Event', { :extends => 'AbstractEntity', :properties => {
      'author' => { :type => :link, :linked_class => 'User', :mandatory => true },
      'patient' => { :type => :link, :linked_class => 'Patient', :mandatory => true },
      'procedure' => { :type => :link, :linked_class => 'Procedure', :mandatory => true },
      'text' => { :type => :string, :mandatory => true, :notnull => true },
      'notice' => { :type => :string },
      'startTime' => { :type => :datetime },
      'length' => { :type => :integer },
      'sent' => { :type => :datetime },
      'sendAttemptCount' => { :type => :integer },
      'type' => { :type => :string }
    }}
  ]
]
