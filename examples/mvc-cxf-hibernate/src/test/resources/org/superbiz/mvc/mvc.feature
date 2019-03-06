Feature: Add User

  Scenario: Add User Happy Path

    Given I navigate to the new user screen
    When I submit a new user with name: Joe Bloggs age: 42 country: United States state: California server: TomEE description: This is a test
    Then The user should be added: Joe Bloggs age: 42 country: United States state: California server: TomEE description: This is a test
